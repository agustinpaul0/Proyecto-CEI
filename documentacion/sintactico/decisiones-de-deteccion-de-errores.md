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

### 3.1. Lo que se va al analizador semántico

El logro *MiniLambdas E2* dice: *«No se permite la captura de variables locales, parámetros, ni el
uso de `this` dentro de una Lambda»*. Esas tres cláusulas, más la de que los parámetros de una
lambda no se repitan, **no las controla esta etapa**. Lo indicó el profesor: son controles de
significado, en el semántico se resuelven más fácil, y el logro en esta instancia no los requiere.

El motivo técnico coincide con el criterio de arriba: ninguna de las tres se decide con información
local.

- `x -> campo` es un atributo, o sea un `this` implícito.
- `x -> n` es una variable local de afuera, o sea una captura.
- **Son la misma forma**: un `idMetVar` suelto que no está entre los parámetros. Separarlas exige
  resolver el nombre contra su declaración, que es exactamente la tabla de símbolos de la Etapa 3.

El `this` explícito sí sería decidible con un booleano, pero controlarlo solo no aporta: dejaría
rechazado `x -> this.campo` y aceptado `x -> campo`, que es el mismo error escrito de otra forma.

El sintáctico entonces las acepta todas:

| Entrada | Veredicto | Por qué |
|---|---|---|
| `x -> this.campo` | acepta | el `this` lo controla el semántico |
| `x -> x + n` | acepta | si `n` es captura o campo no se sabe sin declaraciones |
| `x -> metodo(n)` | acepta | ídem |
| `x -> y -> x + y` | acepta | la interna usa `x`, que es de la externa: lo ve el semántico |
| `(a, a) -> a` | acepta | el parámetro repetido lo controla el semántico |
| `x -> x + 1` | acepta | usa su propio parámetro |
| `x -> x.campo` | acepta | |
| `x -> C.metodo(x)` | acepta | |

Con esto se borró `RestriccionesDeLambda` entera, que era la lógica más compleja del analizador. La
pieza que se fue con ella es la pila de **capturas en duda**: como leer el contenido de un
paréntesis lo hacía pasar por el chequeo de captura antes de saber si era una lista de parámetros,
en `x -> (y) -> y` el `y` se habría reportado como captura de `x`; había que anotar los nombres en
duda y resolverlos al cerrar el paréntesis. Sin chequeo de captura, esa maquinaria no tiene motivo.

### 3.2. Lo que se queda: el parámetro tiene que ser un identificador sin tipo

`(a) -> a` es una lambda y `(a + 1) -> a` no lo es, pero hasta ver el `->` los dos son una
`<Expresion>` entre paréntesis: la gramática no puede distinguirlos sin lookahead ilimitado.

Es una restricción de **forma**, no de significado —no hay ningún nombre que resolver—, así que se
queda. El analizador lee el contenido como expresión y **recuerda si consumió exactamente un
`idMetVar`**; si al ver el `->` resulta que no, lo rechaza. La información extra sale de contar los
tokens consumidos (`EntradaDeTokens.consumidos()`).

```
(a + 1) -> a     rechaza   «un parametro de lambda tiene que ser un identificador»
(m1()) -> a      rechaza
(1) -> a         rechaza
(a.b) -> a       rechaza
((a)) -> a       rechaza   parentizar de más no es parentizar el parámetro
```

Del segundo parámetro en adelante la restricción la impone la gramática, que ahí sí exige
`idMetVar` (`<RestoDeParentesis> ::= , <ListaParametrosLambda> ) -> <CuerpoLambda>`).

### 3.3. Lo que NO se controla: la lambda parentizada con cola

`(x -> y).m()` **se acepta**, y `javac` lo rechaza con *«lambda expression not expected here»*.
Es deliberado.

MiniJava base ya define que cualquier expresión parentizada es un `<Primario>`
(`<ExpresionParentizada> ::= ( <Expresion> )`) y que un `<Primario>` arrastra cadena
(`<Referencia> ::= <Referencia> <MetodoEncadenado>`). Si el logro agrega la lambda como alternativa
de `<Expresion>` —que es el único no terminal de expresión que la gramática tiene—, `(x -> y).m()`
**se deriva solo**, sin tocar ninguna regla. Bloquearlo obligaría a achicar
`<ExpresionParentizada>`, o sea a modificar algo que la especificación ya daba.

El criterio del proyecto es no despegarse de lo que la base ya dictaminó, y sólo ir a Java donde la
base no dice nada. Acá dice, así que gana la base.

Lo que **sí** sigue sin ser operando es la lambda **sin paréntesis**: `1 + x -> x` no deriva, porque
la lambda se produce en el nivel más alto de la jerarquía y nunca en `<Primario>`.

| Entrada | Veredicto | Java |
|---|---|---|
| `1 + x -> x` | **rechaza** | error |
| `- x -> x` | **rechaza** | error |
| `(x -> y).m()` | acepta | error |
| `(x -> y) + 1` | acepta | error |
| `1 + (x -> y)` | acepta | error |
| `f((x -> y))` | acepta | compila |

Queda anotado para confirmar con el profesor.

### 3.4. El resumen de la frontera

| Restricción del logro | Dónde se verifica |
|---|---|
| forma `params -> expr`, parámetros sin tipo, cuerpo sin llaves | gramática |
| dónde puede aparecer una lambda | gramática (`<Expresion>`, no `<Primario>`) |
| el parámetro es un identificador sin tipo | analizador sintáctico (§3.2) |
| no `this`, explícito ni implícito | **analizador semántico** |
| no captura de variables locales ni parámetros | **analizador semántico** |
| parámetros sin nombre repetido | **analizador semántico** |

Y lo que ya estaba del otro lado por definición: que el nombre exista, que el tipo cierre, que la
aridad coincida.

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
mantiene verificado: `python3 herramientas/primeros.py gramatica/minijava-ll1-final.g
--ambiguedades` **falla** si aparece una ambigüedad que no esté declarada con su regla.

> **Los Siguientes no son parte del método.** Ese diagnóstico los calcula para poder detectar
> ambigüedades, que es un ítem del checklist de Clase 4, slide 41. Pero el analizador decide
> **sólo con Primeros**, como enseña la cátedra: los Siguientes no aparecen en la gramática, ni en
> la elección de producciones, ni en una línea del compilador.

## 5. Recuperación en Modo Pánico

Logro de la etapa, **implementado**. Al detectar un error, en lugar de terminar, el analizador
descarta tokens hasta uno donde pueda reanudar y sigue, de modo que un fuente con varios errores
los reporte todos. Está siempre activa.

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

La recuperación **agrega errores después del primero, nunca cambia cuál es el primero**: el primer
código reportado es exactamente el que daría un analizador que cortara ahí, que es la convención
base de Pautas slide 25. Cada caso del corpus con errores declara ese primer código en su primera
línea, y el tester estricto verifica que sea el primero que el compilador emite.

### 5.5. El formato del reporte

Se repite el reporte completo de Pautas slide 25 —mensaje, renglón en blanco, código— por cada
error, en orden. Es el mismo formato con el que la Etapa 1 reporta varios errores léxicos con
*Multi-detección*, que se corrigió sin observaciones.
