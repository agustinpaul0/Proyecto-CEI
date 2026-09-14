# Decisiones de detección de errores del Analizador Sintáctico

Qué se considera un error sintáctico, en qué punto exacto se lo detecta, qué se reporta y qué
chequeos se hacen en el analizador aunque la gramática no pueda expresarlos.

El mecanismo general de reporte —común a todas las etapas— está en
[`../compartido/manejo-de-errores.md`](../compartido/manejo-de-errores.md).

## 1. Los dos puntos donde el análisis falla

El Esquema Simple de la cátedra sólo produce error en dos lugares, y no hay ningún tercero.

**En `match`,** cuando el token actual no es el terminal que la producción exige:

```java
void match(TipoDeToken esperado) {
    if (tokenActual.tipo() == esperado) tokenActual = lexico.proximoToken();
    else throw new ExcepcionSintactica(tokenActual, esperado);
}
```

**Al elegir producción,** cuando el token actual no está en los Primeros de ninguna alternativa
**y** el no terminal no es anulable:

```java
void unNoTerminal() {
    if (PRIMEROS_A.contiene(tokenActual))      { /* traducción de α */ }
    else if (PRIMEROS_B.contiene(tokenActual)) { /* traducción de β */ }
    else throw new ExcepcionSintactica(tokenActual, PRIMEROS_A.unido(PRIMEROS_B));
}
```

Si el no terminal **sí** es anulable, el `else` **no hace nada**: eso significa que derivó a vacío,
y el error —si lo hay— lo va a encontrar el método que llamó, cuando intente su propio `match`.

## 2. Qué se reporta

Formato fijado por las Pautas, slide 25:

```
Error Sintactico en linea N: se esperaba X se encontro "Y"

[Error:Y|N]
```

donde `Y` es el lexema del token con el que se produjo el error y `N` su línea. El lexema del
token `eof` es `$` (Pautas, slide 26).

> **Verificado contra los casos de la cátedra.** Los cuatro fuentes con error de
> `vendor/testersSintactico/` declaran en su primera línea el código que esperan. Simulando el
> Esquema Simple sobre la gramática, el rechazo cae en el token exacto de los cuatro:
> `[Error:,|3]`, `[Error:clasesita|3]`, `[Error:hola|5]`, `[Error:else|7]`. Reproducible con
> `python3 herramientas/aceptar_fuente.py ../vendor/testersSintactico/resources/*/*.java`.

## 3. Chequeos que hace el analizador y la gramática no puede expresar

Una gramática libre de contexto decide **formas**: secuencias de tokens. No tiene memoria de qué
se declaró antes ni de dónde está parada. Hay restricciones del enunciado que caen fuera de eso, y
para cada una hubo que decidir **en qué etapa se verifica**.

El criterio que se usó:

> Si la restricción se puede verificar con información **que el analizador acaba de leer y sigue
> teniendo a mano**, se verifica en el analizador sintáctico. Si requiere saber a qué se refiere un
> nombre —o sea, una tabla de símbolos con alcances—, se delega al analizador semántico.

### 3.1. `this` dentro de una lambda

El logro *MiniLambdas E2* dice: *«No se permite la captura de variables locales, parámetros, ni el
uso de `this` dentro de una Lambda»*.

**No es expresable en la gramática.** Una gramática libre de contexto no puede condicionar la
presencia de un símbolo a *en qué parte del árbol* está: no hay forma de escribir «`this` es un
`<Primario>` válido, salvo debajo de un `<CuerpoLambda>`». Escribirlo exigiría duplicar toda la
jerarquía de expresiones en una variante «sin `this`», que es justamente lo que la cátedra
desaconseja (Clase 4, slide 40).

**Sí es verificable en el analizador, y de la forma más barata posible: con un booleano.** El
analizador sabe si está dentro de una lambda porque acaba de entrar; alcanza con prender un
indicador al empezar el cuerpo y apagarlo al salir. Está implementado en
`sintactico/RestriccionesDeLambda`, que guarda ese estado y expone los cuatro chequeos; el alcance se
abre y se cierra alrededor del cuerpo:

```java
RestriccionesDeLambda.Alcance anterior = restricciones.entrar(parametros);
try {
    expresiones().expresion();
} finally {
    restricciones.salir(anterior);
}
```

Se restaura al salir y no simplemente se apaga, porque después de la lambda el código vuelve a poder
usar `this`.

**Hay un segundo caso, implícito, y ése queda para el semántico.** Llamar a un método propio sin
escribir `this` parece un uso de `this` igual: `x -> metodo(x)` sería `x -> this.metodo(x)`. Pero
**sólo si `metodo` es de instancia**. Si es `static` de la misma clase no hay ningún `this`, y Java
acepta la lambda sin objeción. Distinguir los dos casos exige resolver el nombre contra su
declaración —una tabla de símbolos— y por lo tanto cae del otro lado del criterio: rechazarlo acá
sería rechazar un programa válido, que es peor que dejarlo pasar a la etapa que sí puede decidirlo.

### 3.2. Captura de variables

Misma cláusula del logro. Es un chequeo sobre nombres, pero el conjunto de nombres legales es
inusualmente chico y **local**: como el cuerpo de una lambda es *una única expresión*, adentro no
se puede declarar nada, así que los únicos nombres válidos son **sus propios parámetros**, que el
analizador acaba de leer.

La gramática ya distingue los tres usos de un `idMetVar`, así que se sabe cuál es un acceso a
variable y cuál no:

| Forma | Producción | ¿Se chequea? |
|---|---|---|
| `x` solo | `AccesoLlamada → ε` | **sí**: tiene que estar en los parámetros |
| `x(…)` | `AccesoLlamada → ArgsActuales` | no: podría ser un método `static` (3.1) |
| `.campo` | `Encadenado → . idMetVar …` | no: es un campo de otro objeto, no una variable |
| `C.m(…)` | `LlamadaMetodoEstatico` | no: es estático, no captura nada |

Las lambdas anidadas no necesitan una pila de alcances: como usar el parámetro de la lambda de
afuera **también** está prohibido, alcanza con el conjunto de la más interna.

Ejemplos:

Todo esto está verificado en `LambdasTest`, con el par completo por caso.

| Entrada | Veredicto | Por qué |
|---|---|---|
| `x -> x + 1` | acepta | `x` es su parámetro |
| `(a, b) -> a * b` | acepta | los dos son parámetros |
| `x -> x.campo` | acepta | `campo` va después de un `.` |
| `x -> C.metodo(x)` | acepta | llamada estática, no captura |
| `x -> x + n` | **rechaza** | `n` no está entre los parámetros |
| `x -> this.campo` | **rechaza** | `this` explícito |
| `x -> metodo(x)` | acepta | podría ser un método `static`: lo decide el semántico |
| `x -> y -> x + y` | **rechaza** | la interna usa `x`, que es de la externa |
| `x -> (y) -> y` | acepta | `y` es el parámetro de la interna, aunque venga entre paréntesis |
| `x -> (y) -> x` | **rechaza** | el cuerpo usa `x`, que es de la externa |
| `x -> (a + b)` | **rechaza** | el paréntesis era una expresión: `a` es una captura |
| `(a + 1) -> a` | **rechaza** | el parámetro no es un identificador pelado |
| `(a, a) -> a` | **rechaza** | el parámetro está repetido |
| `f((x -> x))` | acepta | la lambda parentizada es toda la expresión |
| `(x -> x).m()` | **rechaza** | parentizarla no la convierte en operando |

### 3.2.1. El parámetro tiene que ser un identificador pelado

Hay un tercer chequeo, que sale del mismo lugar. `(a) -> a` es una lambda y `(a + 1) -> a` no lo es,
pero hasta ver el `->` los dos son una `<Expresion>` entre paréntesis: la gramática no puede
distinguirlos sin un lookahead ilimitado.

El analizador lee el contenido como expresión y **recuerda si consumió exactamente un `idMetVar`**;
si al ver el `->` resulta que no, lo rechaza. Es la única información extra que hace falta, y sale
de contar los tokens que ya se consumieron (`EntradaDeTokens.consumidos()`).

> **El chequeo de captura queda en duda mientras dura esa lectura.** Leer el contenido como
> expresión lo hace pasar por el chequeo de captura, y ahí todavía rige el alcance de la lambda de
> afuera: en `x -> (y) -> y` el `y` se leería como una captura de `x`, y se rechazaría una lambda
> que escrita `x -> y -> y` se acepta. Por eso, mientras el paréntesis está abierto, los nombres
> que no son parámetros no se rechazan sino que se **anotan en duda**; al cerrar se resuelve:
> si sigue un `->` eran declaraciones y se descartan, y si no, eran capturas y se reporta la
> primera. El marco se cierra pase lo que pase, porque con Modo Pánico el contenido puede fallar y
> el análisis seguir.

Se rechaza también el parámetro repetido, `(a, a) -> a`, por la misma vía.

### 3.2.2. Una lambda parentizada no lleva cola de expresión

El cuarto chequeo, del mismo origen. Una lambda no es un operando: eso lo impone la gramática, que
la produce en el nivel de `<Expresion>` y no en `<Primario>`
([`decisiones-de-logica.md`](decisiones-de-logica.md)). Pero queda un hueco: el contenido de
`<ContenidoDeParentesis>` es una `<Expresion>`, y **tiene que serlo**, porque `f((x -> x))` es
válido en Java. Entonces `(x -> x).m()` y `(x -> x) + 1` también atraviesan la gramática, y `javac`
los rechaza con *«lambda expression not expected here»*.

Cerrarlo en la gramática exigiría clonar la cadena de colas en su variante «el contenido del
paréntesis fue una lambda» — unos siete no terminales —, así que lo cierra el analizador: recuerda
si lo que acaba de leer entre paréntesis fue una lambda y, si lo fue, exige que no siga ninguno de
los tokens que abren una cola (`.`, `[`, `++`, `--`, los trece binarios, `?`).

Lo que el indicador dice es si la **raíz** de esa expresión es una lambda, no si se vio una lambda
en algún lado. La diferencia importa:

| Entrada | Raíz | Veredicto |
|---|---|---|
| `(x -> x).m()` | lambda | **rechaza** |
| `((x -> x)).m()` | lambda, con paréntesis de más | **rechaza** |
| `(a = x -> x).m()` | asignación | acepta — `javac` acepta la forma |
| `(a ? b : x -> x).m()` | ternario | acepta — ver el límite de abajo |

Por eso las dos producciones que pueden poner otra cosa en la raíz por encima de una lambda
—`RestoExpresion` con su `=` y `RestoCondicional` con su `?`— apagan el indicador al terminar.

> **Límite declarado.** `javac` igual rechaza `(a ? b : x -> x).m()`, pero no por la forma: un
> ternario con una rama lambda es una *poly expression* (JLS 15.25) y eso lo decide la
> **clasificación** de la expresión, que depende de sus operandos y de su tipo destino. Es
> exactamente la clase de chequeo que este proyecto delega al analizador semántico, así que acá se
> acepta y queda anotado.

### 3.3. Por qué no se posterga al semántico

Es una decisión de frontera y conviene dejar dicho por qué cayó de este lado.

- **El logro es de la Etapa 2.** La cátedra corrige esta etapa por separado; postergarlo dejaría el
  logro a medias en la entrega que se está evaluando.
- **El costo es un booleano y un conjunto de strings.** No hay tabla de símbolos, no hay alcances
  anidados, no hay resolución de nombres: el conjunto se arma con los parámetros que el propio
  analizador acaba de leer y se descarta al salir de la lambda.
- **No contamina la gramática.** Ninguna producción cambia; es un chequeo del analizador sobre lo
  que ya parseó, del mismo tipo que la regla que resuelve el *dangling else*.
- **Clase 4, slide 49 lo autoriza expresamente**: cuando algo no se puede resolver reescribiendo
  la gramática, se agrega una regla en la implementación del analizador. Es la misma vía que el
  proyecto ya usa para el `else`.

Lo que **sí** queda para el semántico es todo lo que dependa de resolver un nombre contra una
declaración: que el nombre exista, que el tipo cierre, que la aridad coincida.

## 4. Ambigüedades resueltas por regla de implementación

La gramática tiene tres ambigüedades declaradas, y ninguna se resuelve reescribiéndola: las tres
se resuelven con el **orden de las ramas** en el analizador, que es la segunda de las dos vías que
ofrece Clase 4, slide 49. En los tres casos la regla es la misma —**voraz**— y coincide con Java.

| Ambigüedad | Regla | Efecto |
|---|---|---|
| *dangling else* | tomar la rama `else` apenas se ve el token | liga al `if` más cercano |
| `new int[3][4]` | consumir el `[` como otra dimensión de la creación | es una creación 2-D, no un acceso |
| cuerpo de lambda | dejar que `Expresion()` consuma todo lo que pueda | `x -> a = b` es `x -> (a = b)` |

Eran siete mientras la lambda se producía a nivel `<Primario>`: ahí la cola de la expresión que la
contenía competía con la cola de su cuerpo, y la misma ambigüedad aparecía en los cinco no
terminales de cola. Ponerla en el nivel de `<Expresion>`, como el JLS, **elimina cuatro de raíz**;
la que queda es la del `=`, la única cola externa que todavía compite con el cuerpo.

Las tres están implementadas: el `else` en `AnalizadorDeSentencias`, el `new int[3][4]` en
`AnalizadorDeCreacion` y el cuerpo de la lambda en `AnalizadorDeLambdas`. El detalle de cada una,
con las derivaciones que las exhiben, está en
[`transformacion-a-ll1-paso-a-paso.md`](transformacion-a-ll1-paso-a-paso.md). El listado se
mantiene verificado: `python3 herramientas/primeros.py gramatica/minijava-ll1-logros.g
--ambiguedades` recorre las 1024 configuraciones de logros y **falla** si aparece una ambigüedad
que no esté declarada con su regla.

> **Los Siguientes no son parte del método.** Ese diagnóstico los calcula para poder detectar
> ambigüedades, que es un ítem del checklist de Clase 4, slide 41. Pero el analizador decide
> **sólo con Primeros**, como enseña la cátedra: los Siguientes no aparecen en la gramática, ni en
> la elección de producciones, ni en una línea del compilador.

## 5. Recuperación en Modo Pánico

Logro de la etapa, **implementado**. Al detectar un error, en lugar de terminar, el analizador
descarta tokens hasta uno donde pueda reanudar y sigue, de modo que un fuente con varios errores
los reporte todos. Va detrás de su propio interruptor: **apagado, el compilador corta en el primer
error**, que es la convención base de Pautas slide 25.

### 5.1. Dónde se recupera

En **tres lugares y sólo tres**: las tres listas de la gramática que repiten un elemento
—`ListaClases`, `ListaMiembros` (y `ListaMetodosInterfaz`) y `ListaSentencias`—. Es donde tiene
sentido descartar un elemento roto y seguir con el siguiente, y mantenerlo ahí evita que el
`try/catch` se desparrame por las diez secciones del analizador.

### 5.2. Con qué se sincroniza

Pautas slide 40 nombra el punto y coma, la llave de cierre y la llave que abre «según el contexto».
A esos se les suman **los Primeros del elemento de la lista**, y hacen falta:

```java
class C1 {
  int x        // falta el `;`
  void m(){ }  // este método es correcto
}
```

El análisis falla en `void`, que es justamente donde empieza el miembro siguiente y correcto. Sin
los Primeros como punto de reanudación, la recuperación se llevaría puesto el método entero.

Las **llaves se cuentan** mientras se descarta: un `{` que no arranca un elemento de la lista abre
algo anidado —el cuerpo del método cuya firma está rota— y se saltea entero, en vez de leer cada
sentencia de adentro como si fuera otro miembro y producir un error inventado por cada una.

### 5.3. La invariante de progreso

Cada vuelta del bucle consume al menos un token. Hay que decirlo explícito porque el error puede
caer justo en un token que también arranca otro elemento: `class C { int int; }` falla en el
segundo `int`, que es un comienzo válido de miembro, y sin la garantía el analizador quedaría dando
vueltas sobre el mismo token. Está cubierto con un test con límite de tiempo.

### 5.4. El primer error no cambia

El logro **agrega errores al final de la lista, nunca cambia el primero**. Eso es lo que permite que
un mismo caso del corpus valga con el logro prendido y apagado, y hay un test que lo verifica sobre
el corpus entero además de una propiedad que lo verifica sobre entradas al azar.

### 5.5. El formato del reporte

Se repite el reporte completo de Pautas slide 25 —mensaje, renglón en blanco, código— por cada
error, en orden. Es el mismo formato con el que la Etapa 1 reporta varios errores léxicos con
*Multi-detección*, que se corrigió sin observaciones.
