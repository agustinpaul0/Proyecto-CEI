# Convenciones de la cátedra

Lo que **el profesor indicó explícitamente** para este proyecto, en respuesta a consultas o al
corregir. Cada punto se aplica tal cual, aunque Java haga otra cosa: donde la cátedra habla, no se
aplica «como en Java». Cada uno enlaza el documento que lo desarrolla.

Las convenciones que están escritas en el material de la cátedra (contrato de salida, formato de
los errores, invocación) están en
[`documentacion/proyecto/convenciones-de-la-catedra.md`](documentacion/proyecto/convenciones-de-la-catedra.md).

Los códigos de error de los ejemplos son la salida real del compilador. Una **sentencia** va en la
línea 3 de este fuente y un **miembro** en la línea 2 de este otro:

```java
class Clase {                  class Clase {
  void m() {                     <miembro>
    <sentencia>                }
  }
}
```

## Etapa 3 — Analizador Semántico, Parte I

### 1. Los logros son independientes entre etapas

Si un logro de una etapa no se continúa en la siguiente, los programas con que se prueba la etapa
siguiente no traen esa construcción. Por ejemplo, con Lambdas hecho en la Etapa 2 y no continuado
en la 3, ningún programa de prueba de la Etapa 3 trae lambdas; con Genericidad Avanzada E2 y sin
Genericidad Avanzada E3, ninguno trae genéricos anidados. Por eso el semántico se hace para el
MiniJava base: el parser sigue aceptando lo que agregan los logros de la Etapa 2 (`private`,
`Caja<Par<String>>`, atributos inicializados), pero eso no suma controles.

Desarrollo: [`semantico/decisiones-de-logica.md` §1](documentacion/semantico/decisiones-de-logica.md).

### 2. No se exige un `main`

| Fuente | Resultado |
|---|---|
| `class Aa { }` | `[SinErrores]` |

### 3. `String` y `System` se pueden extender

`final` es parte del logro Sobrecarga! E3, que no se persigue. Java no deja extenderlas; acá sí.

| Fuente | Resultado |
|---|---|
| `class Aa extends String { }` | `[SinErrores]` |
| `class Aa extends System { static int read() { } }`, en la línea 1 | `[Error:read\|1]`: redeclara un estático heredado |

### 4. La `T` en un método estático y la interfaz contra `Object` se controlan

Se controlan como en Java, aunque el documento de la cátedra no los nombre.

| Fuente | Resultado |
|---|---|
| `class Caja<T> { static T crear() { } }` | `[Error:T\|1]` |
| `interface Ix { int toString(); }` | `[Error:toString\|1]` |

### 5. Donde el token queda a elección, se sigue a `javac`

El profesor aceptó cualquier criterio en estos tres errores:

| Fuente | Resultado | `javac` |
|---|---|---|
| `class Aa extends Bb { }` y `class Bb extends Aa { }`, en las líneas 1 y 2 | `[Error:Aa\|1]` | marca la declaración de `Aa`: «cyclic inheritance involving Aa» |
| `class Caja { }` y `class Aa { Caja<String> x; }`, en las líneas 1 y 2 | `[Error:Caja\|2]` | «type Caja does not take parameters» |
| `interface Ix { void m(); }` y `class Aa implements Ix { static void m() { } }`, en las líneas 1 y 2 | `[Error:m\|2]` | marca `m`: «m() in Aa cannot implement m() in Ix» |

Desarrollo:
[`semantico/decisiones-de-deteccion-de-errores.md`](documentacion/semantico/decisiones-de-deteccion-de-errores.md).

### 6. Los cuerpos son de la Etapa 4

Todo lo que está dentro de un cuerpo es chequeo de sentencias, y queda para la Etapa 4. Esto
incluye los nombres no declarados, la compatibilidad de tipos, la aridad de las llamadas, las
variables locales repetidas, qué inicializa `int x, y, z = 10;`, y la precedencia de los
operadores. En la Etapa 3 se controlan sólo las declaraciones.

| Fuente | Resultado |
|---|---|
| `class Aa { void m() { this.b().a = 5; } }` | `[SinErrores]` |

## Etapa 2 — Analizador Sintáctico

### 7. El `for` lleva una expresión en cada hueco

La Pauta del logro Fors! E2 dice *«se restringe a una sola sentencia/expresión por separador»*
(slide 41). Es **una** por hueco, no *a lo sumo una*: la inicialización, la condición y la
actualización son obligatorias. Java las hace opcionales; acá no.

| Sentencia | Resultado |
|---|---|
| `for (var i = 0; i < n; i++) ;` | válida |
| `for (i = 0; i < n; i++) ;` | válida |
| `for (var e : lista) ;` | válida |
| `for (;;) ;` | `[Error:;\|3]` |
| `for (; i < n; i++) ;` | `[Error:;\|3]` |
| `for (var i = 0; ; i++) ;` | `[Error:;\|3]` |
| `for (var i = 0; i < n; ) ;` | `[Error:)\|3]` |

Desarrollo: [`decisiones-de-logica.md` §10](documentacion/sintactico/decisiones-de-logica.md).

### 8. La variable de control del `for` se declara sólo con `var`

Los logros son independientes entre sí: el `for` no toma el declarador tipado del logro Variables
Locales Clásicas, ni en el `for` estándar ni en el de iteradores.

| Sentencia | Resultado |
|---|---|
| `for (var i = 0; i < n; i++) ;` | válida |
| `for (int i = 0; i < n; i++) ;` | `[Error:int\|3]` |
| `for (Persona p : lista) ;` | `[Error:p\|3]` |

Desarrollo: [`decisiones-de-logica.md` §10](documentacion/sintactico/decisiones-de-logica.md).

### 9. Sin asignaciones anidadas, salvo entre paréntesis

El declarador de Variables Locales Clásicas sigue el mismo criterio que el `var` de la gramática
base, donde el lado derecho del `=` es una `<ExpresionCompuesta>`, que no admite otro `=`. Un
paréntesis es un primario y adentro lleva una `<Expresion>` completa, así que ahí sí se puede. Vale
para todo lado derecho de un `=`: la asignación y los inicializadores.

| Sentencia | Resultado |
|---|---|
| `int x = y = 2;` | `[Error:=\|3]` |
| `int x, y = z = 2;` | `[Error:=\|3]` |
| `var x = y = 2;` | `[Error:=\|3]` |
| `x = y = 2;` | `[Error:=\|3]` |
| `int x = (y = 2);` | válida |
| `int x, y = (z = 2);` | válida |
| `var x = (y = 2);` | válida |
| `x = (y = 2);` | válida |
| `int x = (y = z = 2);` | `[Error:=\|3]`, la misma regla adentro del paréntesis |
| `int x = c ? a = 1 : b;` | válida: el operando del medio del ternario es una expresión completa (JLS §15.25) |
| `int x = c ? a : b = 1;` | `[Error:=\|3]` |

Desarrollo: [`decisiones-de-logica.md` §3](documentacion/sintactico/decisiones-de-logica.md).

### 10. La inicialización de arreglos es la de Java

Con inicializador, ninguna dimensión lleva tamaño (JLS §15.10.1), los inicializadores se anidan y la
coma final es opcional (JLS §10.6). El inicializador sólo aparece después de `new`.

| Sentencia | Resultado |
|---|---|
| `var a = new int[]{1, 2, 3};` | válida |
| `var a = new int[][]{{1, 2}, {3, 4}};` | válida |
| `var a = new int[]{1, 2,};` | válida |
| `var a = new int[]{};` | válida |
| `var a = new int[2]{1, 2};` | `[Error:{\|3]` |
| `var a = new int[2][2]{{1, 2}, {3, 4}};` | `[Error:{\|3]` |
| `int[] a = {1, 2};` | `[Error:{\|3]` |

Sin inicializador rige la creación de la gramática base, `new TipoBase DimensionesConTamanio`, en
la que toda dimensión lleva su tamaño. **Se sabe que Java admite `new int[2][]`** (JLS §15.10.1). El
profesor respondió: *«Hacelo igual que en Java […] aun así para la corrección voy a considerar todo
como correcto (como funciona tu compilador y la versión pura de java… en el semántico lo
ajustamos)»*. Las dos variantes son válidas para la corrección; se conserva la de la base por
coherencia con el criterio del proyecto: donde la base define una construcción, se respeta, y «como
en Java» rige para lo que el logro agrega, que es el inicializador. La corrección de la Etapa 2 no
tuvo observaciones, así que el criterio queda confirmado.

| Sentencia | Resultado |
|---|---|
| `var a = new int[3];` | válida |
| `var a = new int[2][3];` | válida |
| `var a = new int[2][];` | `[Error:]\|3]` |

Desarrollo: [`decisiones-de-logica.md` §7](documentacion/sintactico/decisiones-de-logica.md).

### 11. No hay atributos estáticos

`static` es modificador de métodos y de nada más, como en la gramática base.

| Miembro | Resultado |
|---|---|
| `static void m() { }` | válido |
| `int x;` | válido |
| `static int x;` | `[Error:;\|2]` |

Tampoco hay varios atributos en una misma declaración, como en la base. **Se sabe que Java los
admite**. El logro de Variables Locales Clásicas habla de variables locales declaradas en una
sentencia, y el de Atributos Inicializados agrega sólo la inicialización. Esto no lo indicó el
profesor: es apego a la gramática base.

| Miembro | Resultado |
|---|---|
| `int a = 0;` | válido |
| `int a, b;` | `[Error:,\|2]` |
| `int a = 0, b;` | `[Error:,\|2]` |

Desarrollo: [`decisiones-de-logica.md` §6 y §12](documentacion/sintactico/decisiones-de-logica.md).

### 12. `static` y la visibilidad van en cualquier orden, y `protected` no existe

`public static` y `static public` son válidos los dos, y lo mismo con `private`. Repetir la
visibilidad no lo es. `protected` no se agrega: no está en MiniJava.

| Miembro | Resultado |
|---|---|
| `public static void m() { }` | válido |
| `static public void m() { }` | válido |
| `private static void m() { }` | válido |
| `static private void m() { }` | válido |
| `public static public void m() { }` | `[Error:public\|2]` |
| `protected void m() { }` | `[Error:protected\|2]` |

Desarrollo: [`decisiones-de-logica.md` §6](documentacion/sintactico/decisiones-de-logica.md).

### 13. Los logros no son configurables

El compilador corre siempre con todos sus logros. Elegir con cuáles correr complejiza la gramática
sin necesidad, y cada etapa nueva obligaría a rehacer ese trabajo.

Desarrollo: [`flujo-de-trabajo.md`](documentacion/proyecto/flujo-de-trabajo.md), «Los logros no son
configurables».

### 14. La recursión a derecha se traduce con recursión, no con `while`

Es el Esquema Simple de Clase 5 (slide 59) sin desvíos. El `while` aleja el código del esquema que
se extiende en el semántico y es más propenso a errores.

Desarrollo: [`decisiones-de-diseno.md`](documentacion/sintactico/decisiones-de-diseno.md),
«Decisiones cerradas».

### 15. Ningún método de no terminal omite el `else` final

Si el no terminal no es anulable, el `else` final de su cadena de Primeros es el error (Clase 5,
slide 39). Si alguna parte derecha deriva a ε, es un `else` vacío con la producción citada al lado
—el «no hago nada :)» de la slide 52—, que es donde la Etapa 3 va a colgar la traducción de la
producción vacía.

Desarrollo: [`decisiones-de-diseno.md`](documentacion/sintactico/decisiones-de-diseno.md),
«Decisiones cerradas».

### 16. El lexema del fin de archivo es `$`

El material de la cátedra se contradice: Pautas slide 26 y *Aspectos Léxicos* §2.5 dicen `$`, y la
*Guía de uso de los testers — Etapa 2*, §5, dice que es vacío (`[Error:|N]`). Se consultó y el
profesor confirmó `$`.

| Fuente | Resultado |
|---|---|
| `class Clase {` y nada más, en la línea 1 | `[Error:$\|2]` |

Desarrollo:
[`decisiones-de-deteccion-de-errores.md`](documentacion/sintactico/decisiones-de-deteccion-de-errores.md).

## Etapa 1 — Analizador Léxico

### 17. `float` es palabra reservada; `double` no

`float` no figura en ningún PDF de la cátedra y se agrega por confirmación del profesor. MiniJava
tiene un solo tipo de punto flotante, así que `double` es un identificador común.

Desarrollo: [`lexico/decisiones-de-logica.md` §3.1](documentacion/lexico/decisiones-de-logica.md).

### 18. Un escape mal formado descarta el `litString` hasta su comilla de cierre

Ante un escape Unicode sin sus cuatro dígitos hexadecimales dentro de un `litString`
(`"a\u12G4"`), se reporta el error y se descarta lo que queda del literal hasta su comilla de
cierre, sin emitir token. `javac` lo reporta y sigue leyendo el literal. El profesor consideró esa
salida incorrecta, porque el compilador afirmaría haber reconocido un literal cuyo contenido no supo
interpretar.

Desarrollo:
[`lexico/decisiones-de-deteccion-de-errores.md` §4.3](documentacion/lexico/decisiones-de-deteccion-de-errores.md).
