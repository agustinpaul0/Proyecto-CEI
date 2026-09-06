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
indicador al empezar el cuerpo y apagarlo al salir.

```java
private boolean dentroDeLambda = false;

private void cuerpoLambda() {
    boolean anterior = dentroDeLambda;   // anidadas: x -> y -> …
    dentroDeLambda = true;
    expresion();
    dentroDeLambda = anterior;
}

private void primario() {
    if (tokenActual.tipo() == PR_THIS) {
        if (dentroDeLambda) throw new ExcepcionSintactica(tokenActual, "no se permite this dentro de una lambda");
        match(PR_THIS);
    }
    …
}
```

**Hay un segundo caso, implícito.** Llamar a un método propio sin escribir `this` es un uso de
`this` igual: `x -> metodo(x)` es `x -> this.metodo(x)`. Y adentro de una lambda de MiniJava, una
llamada sin punto **sólo** puede ser un método de la clase: no hay funciones sueltas en el
lenguaje, y un parámetro es un valor, no se lo puede llamar. Así que **toda llamada sin punto
dentro de una lambda es un `this` implícito**, y se rechaza con el mismo indicador.

### 3.2. Captura de variables

Misma cláusula del logro. Es un chequeo sobre nombres, pero el conjunto de nombres legales es
inusualmente chico y **local**: como el cuerpo de una lambda es *una única expresión*, adentro no
se puede declarar nada, así que los únicos nombres válidos son **sus propios parámetros**, que el
analizador acaba de leer.

La gramática ya distingue los tres usos de un `idMetVar`, así que se sabe cuál es un acceso a
variable y cuál no:

| Forma | Producción | ¿Se chequea? |
|---|---|---|
| `x` solo | `AccesoLlamadaOLambda → ε` | **sí**: tiene que estar en los parámetros |
| `x(…)` | `AccesoLlamadaOLambda → ArgsActuales` | rechazo directo: es `this` implícito (3.1) |
| `.campo` | `Encadenado → . idMetVar …` | no: es un campo de otro objeto, no una variable |
| `C.m(…)` | `LlamadaMetodoEstatico` | no: es estático, no captura nada |

```java
private Set<String> parametrosDeLaLambda = null;   // null = no estamos en una lambda
```

Las lambdas anidadas no necesitan una pila de alcances: como usar el parámetro de la lambda de
afuera **también** está prohibido, alcanza con el conjunto de la más interna.

Ejemplos:

| Entrada | Veredicto | Por qué |
|---|---|---|
| `x -> x + 1` | acepta | `x` es su parámetro |
| `(a, b) -> a * b` | acepta | los dos son parámetros |
| `x -> x.campo` | acepta | `campo` va después de un `.` |
| `x -> C.metodo(x)` | acepta | llamada estática, no captura |
| `x -> x + n` | **rechaza** | `n` no está entre los parámetros |
| `x -> this.campo` | **rechaza** | `this` explícito |
| `x -> metodo(x)` | **rechaza** | `this` implícito |
| `x -> y -> x + y` | **rechaza** | la interna usa `x`, que es de la externa |

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

La gramática tiene siete ambigüedades declaradas, y ninguna se resuelve reescribiéndola: las siete
se resuelven con el **orden de las ramas** en el analizador, que es la segunda de las dos vías que
ofrece Clase 4, slide 49. En los siete casos la regla es la misma —**voraz**— y coincide con Java.

| Ambigüedad | Regla | Efecto |
|---|---|---|
| *dangling else* | tomar la rama `else` apenas se ve el token | liga al `if` más cercano |
| `new int[3][4]` | consumir el `[` como otra dimensión de la creación | es una creación 2-D, no un acceso |
| cuerpo de lambda (×5) | dejar que `Expresion()` consuma todo lo que pueda | `x -> a + b` es `x -> (a + b)` |

El detalle de cada una, con las derivaciones que las exhiben, está en
[`transformacion-a-ll1-paso-a-paso.md`](transformacion-a-ll1-paso-a-paso.md). El listado se
mantiene verificado: `python3 herramientas/primeros.py gramatica/minijava-ll1-logros.g
--ambiguedades` recorre las 1024 configuraciones de logros y **falla** si aparece una ambigüedad
que no esté declarada con su regla.

> **Los Siguientes no son parte del método.** Ese diagnóstico los calcula para poder detectar
> ambigüedades, que es un ítem del checklist de Clase 4, slide 41. Pero el analizador decide
> **sólo con Primeros**, como enseña la cátedra: los Siguientes no aparecen en la gramática, ni en
> la elección de producciones, ni en una línea del compilador.

## 5. Recuperación en Modo Pánico

Logro de la etapa. Al detectar un error, en lugar de terminar, el analizador descarta tokens hasta
encontrar uno de sincronización —`;`, `}` o `{` según el contexto, Pautas slide 40— y reanuda el
análisis, de modo que un fuente con varios errores los reporte todos.

Va detrás de su propio interruptor: **apagado, el compilador corta en el primer error**, que es la
convención base de la slide 25. Se implementa en la Sesión 4; el plan está en
[`../proyecto/plan-de-accion-etapa-2.md`](../proyecto/plan-de-accion-etapa-2.md).

## Ver también

- [`../compartido/manejo-de-errores.md`](../compartido/manejo-de-errores.md) — el mecanismo de
  reporte, común a todas las etapas.
- [`transformacion-a-ll1-paso-a-paso.md`](transformacion-a-ll1-paso-a-paso.md) — las ambigüedades
  y su resolución, con las derivaciones.
- [`resolucion-de-logros.md`](resolucion-de-logros.md) — qué exige cada logro y dónde se resuelve.
