# El patrón State del Analizador Léxico

Documento autocontenido: profundiza el §4 de [`arquitectura.md`](arquitectura.md) con el detalle
completo de cómo están conectadas las piezas — qué es cada tipo, quién lo crea, cuándo se reutiliza
una instancia y cuándo se construye una nueva. Pensado para poder explicar el mecanismo entero sin
leer código.

## 1. El problema que resuelve

Reconocer un token no es una decisión de un solo paso. Para un `floatLiteral` como `1.5e-3f` hay que
mirar el `1`, después el `.`, después el `5`, después la `e`, después el `-`, después el `3`, después
la `f` — y en cada paso la pregunta "¿qué significa el próximo carácter?" depende de **dónde estamos
parados** dentro del reconocimiento.

Un `5` significa cosas distintas según el contexto: al principio empieza un número; después de un
punto es parte de la mantisa; después de una `e` es parte del exponente; dentro de un string es un
carácter cualquiera. Ese "dónde estamos parados" es exactamente un **estado de un Autómata Finito**.

La cátedra exige modelar el analizador con la técnica **Método por Estado**: un componente de
software por cada estado del AF, que decide la transición según el carácter actual. Este proyecto la
implementa con el patrón de diseño **State** (Gang of Four), que es su forma orientada a objetos
canónica.

## 2. Las cinco piezas

| Pieza | Tipo | Rol |
|---|---|---|
| `EstadoLexico` | interfaz | El contrato común a todo estado: un único método `procesar(contexto)`. |
| `ContextoLexico` | clase (fachada) | La memoria compartida entre todos los estados durante el reconocimiento de un token: qué carácter se está mirando, qué lexema se lleva armado, en qué línea estamos, y las posiciones que los errores necesitan. Delega en dos piezas internas, `BuilderDeLexema` y `FabricaDeExcepcionesLexicas` (ver [`arquitectura.md`](arquitectura.md) §5.2.1); los estados sólo ven la fachada. |
| `ResultadoDeTransicion` | **interfaz sellada** | Lo que devuelve `procesar()`: sólo puede ser una de dos cosas, nunca una tercera. |
| `Continuar` | `record` | "Todavía no hay token completo — el próximo estado a consultar es este otro." |
| `Emitir` | `record` | "El token ya está completo — acá está." |

```java
public sealed interface ResultadoDeTransicion permits Continuar, Emitir {}

public record Continuar(EstadoLexico siguiente) implements ResultadoDeTransicion {}

public record Emitir(Token token) implements ResultadoDeTransicion {}
```

### Las dos operaciones del contexto que hay que entender

- **`avanzar()`** — consume el carácter actual **y lo agrega al lexema**. Es lo que se usa cuando el
  carácter forma parte del token que se está armando.
- **`descartar()`** — consume el carácter actual **sin** agregarlo al lexema. Es lo que se usa para
  los separadores y para el contenido de los comentarios: se leen, pero no forman parte de ningún
  token.

La distinción parece menor y es central: el criterio de recuperación ante errores se apoya
enteramente en ella, porque garantiza que lo que se consumió y lo que se reporta sean lo mismo. Ver
[`manejo-de-errores.md`](manejo-de-errores.md) §2.

### Por qué `sealed`

`sealed interface ... permits Continuar, Emitir` le dice al compilador: "estos son los **únicos** dos
tipos que pueden implementar esta interfaz, no puede aparecer un tercero en ningún otro archivo".

Eso habilita al driver a usar un `switch` **exhaustivo**: el compilador verifica en tiempo de
compilación que estén cubiertos todos los casos posibles, sin necesitar una rama `default` que "por
las dudas" atrape un caso no contemplado. Ver §5 para la comparación con la alternativa.

## 3. El driver

`AnalizadorLexico.proximoToken()` es el **único** lugar que arranca la cadena de estados y decide
cuándo terminó:

```java
public Token proximoToken() throws ExcepcionLexica {
    contexto.iniciarNuevoToken();
    EstadoLexico estado = EstadoInicial.INSTANCIA;
    while (true) {
        ResultadoDeTransicion resultado = estado.procesar(contexto);
        switch (resultado) {
            case Emitir emitir -> {
                return emitir.token();
            }
            case Continuar continuar -> estado = continuar.siguiente();
        }
    }
}
```

En criollo: "empezá en el estado inicial; preguntale qué corresponde hacer con el carácter actual; si
contesta `Continuar`, andá al estado que te indicó y volvé a preguntar; si contesta `Emitir`, ese es
el token — devolvelo y frená."

Cada estado sólo sabe resolver **su propio paso**. Ninguno sabe cómo se llegó hasta ahí ni qué pasa
después de que devuelve su resultado. Esa separación es la esencia del patrón: la lógica de "¿qué
significa este carácter en esta situación puntual?" queda encapsulada por completo dentro de la clase
de ese estado, y agregar un token nuevo al lenguaje no obliga a tocar los estados existentes.

### Por qué un `while` y no la recursión que sugiere la cátedra

La cátedra presenta el AF encadenando llamadas: `e1()` termina llamando a `e2()`, que termina llamando
a `e3()`. Esa técnica sigue siendo Método por Estado — pero cada llamada anidada ocupa un marco de
pila de la JVM, y un lexema muy largo (un comentario de miles de caracteres, un string enorme) puede
agotar la pila con `StackOverflowError`.

El `while` de arriba logra exactamente el mismo comportamiento observable sin apilar nada: en cada
vuelta hay como máximo **una** llamada activa a `procesar()`, sin importar cuán largo sea el lexema.
Es la única libertad que este proyecto se tomó respecto de lo que enseñan las slides, y está
documentada como tal para que quede claro que la técnica exigida —un componente por estado— se
respeta igual.

**El costo**: hay que materializar el resultado de cada transición como un objeto en vez de
devolverlo por la pila. Son records inmutables de un solo uso, y el costo es despreciable frente a
la garantía de que el compilador no se cae con una entrada legal.

## 4. Trazado completo: reconocer `1.5e-3f`

| Paso | Estado que actúa | Mira | Qué hace | Devuelve |
|---|---|---|---|---|
| 1 | `EstadoInicial` | `1` | es dígito: lo consume | `Continuar(EstadoEntero)` |
| 2 | `EstadoEntero` | `.` | la corrida de dígitos sigue como float: lo consume | `Continuar(EstadoFloatConPunto)` |
| 3 | `EstadoFloatConPunto` | `5` | dígito de la parte decimal: lo consume | `Continuar(EstadoFloatConPunto)` — el mismo estado, se re-consulta a sí mismo |
| 4 | `EstadoFloatConPunto` | `e` | arranca el exponente: lo consume | `Continuar(EstadoFloatExponenteInicio)` |
| 5 | `EstadoFloatExponenteInicio` | `-` | signo del exponente: lo consume | `Continuar(EstadoFloatExponenteRequiereDigito)` |
| 6 | `EstadoFloatExponenteRequiereDigito` | `3` | el dígito obligatorio tras el signo: lo consume | `Continuar(EstadoFloatExponenteDigitos)` |
| 7 | `EstadoFloatExponenteDigitos` | `f` | es sufijo float, no otro dígito: lo consume | `Emitir(Token(floatLiteral, "1.5e-3f", 1))` |

Siete vueltas del `while`, siete objetos `ResultadoDeTransicion` construidos y descartados, y el
driver nunca necesitó saber nada sobre floats. Cada estado resolvió sólo su fragmento.

**El paso 6 es el que justifica que exista un estado propio.** `EstadoFloatExponenteRequiereDigito`
existe porque después del signo un dígito es **obligatorio**: `1.5e-` es un error. Si el signo y los
dígitos compartieran estado, no habría forma de distinguir "todavía no vi ningún dígito del
exponente" de "ya vi al menos uno" — y `1.5e-` pasaría por válido.

**El paso 3 muestra un estado que transiciona a sí mismo**, que es la forma normal de expresar un
bucle del autómata (`Digito*`): no hay recursión ni acumulación, simplemente la próxima vuelta del
`while` vuelve a consultar la misma instancia.

## 5. `switch` sobre tipo sellado en vez de `instanceof`

Una versión anterior del driver comprobaba el resultado así:

```java
// Funciona, pero no aprovecha que el tipo es sealed
if (resultado instanceof Emitir emitir) {
    return emitir.token();
}
estado = ((Continuar) resultado).siguiente();
```

Funciona, pero tiene dos problemas de fondo que el `switch` resuelve:

1. **No hay chequeo de exhaustividad.** Si mañana `ResultadoDeTransicion` ganara un tercer tipo, el
   código de arriba seguiría compilando igual, y el casteo `(Continuar) resultado` explotaría con un
   `ClassCastException` recién cuando ese tercer caso realmente ocurriera. Un bug que el compilador
   podía atrapar se convierte en uno que sólo aparece en ejecución.
2. **El casteo es manual y silencioso.** `((Continuar) resultado)` le pide al lector confiar en que
   "si no era `Emitir`, tiene que ser `Continuar`" — una inferencia correcta hoy, pero que nada deja
   explícita ni protege a futuro.

El `switch` sobre un tipo `sealed` convierte esa inferencia implícita en una garantía del compilador:
si aparece un tercer tipo, `AnalizadorLexico` directamente **no compila** hasta agregar el `case`.

Es el mismo principio por el que `TipoDeToken` es un `enum` y no un `String` suelto: dejar que el
compilador mantenga los casos sincronizados, en vez de confiar en que alguien se acuerde.

## 6. Singleton compartido vs. instancia nueva — el criterio

Mirando el paquete `minijava.lexico.estados` se ven dos estilos de construcción, y la elección
**no es arbitraria**: depende de si el estado necesita recordar algo específico de *este* recorrido,
o si su comportamiento es una función pura del `ContextoLexico` que recibe.

### Estados sin memoria propia → un único `INSTANCIA` compartido

`EstadoInicial`, `EstadoIdMV`, `EstadoEntero`, `EstadoStringAbierto`, `EstadoCharCerrando` y la
mayoría del paquete **no tienen ningún campo de instancia**. Todo lo que necesitan para decidir la
transición ya está en el contexto que reciben por parámetro. Como no cargan datos propios, una única
instancia —creada una sola vez como constante estática— es segura de reutilizar en cualquier momento,
para cualquier token, incluso de forma intercalada:

```java
public final class EstadoIdMV implements EstadoLexico {
    public static final EstadoIdMV INSTANCIA = new EstadoIdMV();
    private EstadoIdMV() {}
    // procesar(...) sólo lee `contexto` — nunca `this`
}
```

El constructor es privado justamente para que no exista forma de crear una segunda instancia por
error.

Este es también el motivo por el que `EstadoOperadorConSufijoOpcional` no tiene un `INSTANCIA`
genérico sino seis constantes con nombre propio (`MAYOR`, `MENOR`, `NEGACION`, `ASIGNACION`, `SUMA`,
`RESTA`): cada una es igual de "sin memoria de recorrido" — sus campos son **configuración fija de
esa variante**, no datos que cambien durante el reconocimiento. Siguen siendo singletones
reutilizables; sólo que hay seis sabores en vez de uno.

### Estados con memoria del recorrido actual → `new` en cada transición

Tres estados sí cargan un dato específico de **este** recorrido puntual, y por eso se construyen con
`new` justo al transicionar hacia ellos, nunca como constante:

- **`EstadoComentarioMultilinea(posicionDeApertura, ultimoFueAsterisco)`** — si el comentario nunca
  cierra, el error debe reportar la posición donde **abrió**, no donde se encontró el fin de archivo
  (que puede estar muchas líneas después, en contenido no relacionado). Esa posición es distinta cada
  vez que se entra a un comentario. `ultimoFueAsterisco` es el estado del autómata de dos estados que
  reconoce el cierre `*/`, y cambia en cada paso.
- **`EstadoEscapeUnicode(digitosRestantes, siguienteAlTerminar)`** — cuenta cuántos dígitos
  hexadecimales faltan (empieza en 4 y decrece) y sabe a qué estado volver al terminar, que es
  distinto según si el escape está dentro de un `charLiteral` o de un `litString`. Ninguno de los dos
  datos es fijo entre usos.
- **`EstadoMarcadorUnicode(siguienteAlTerminar, dentroDeLitString)`** — consume la corrida de `u`
  repetidos del marcador y, por la misma razón, necesita saber a dónde volver y en qué clase de
  literal está.

**La regla, en una frase**: si `procesar()` sólo consulta al `ContextoLexico`, el estado es un
singleton; si necesita cargar algún dato que varía según *cómo se llegó hasta ahí en este recorrido*,
se construye con `new` en el momento de la transición.

Se revisaron los 21 estados uno por uno para escribir esta sección, y los tres de arriba son los
únicos con campos de instancia. El resto son singletones sin estado, consistente con la regla.

### Por qué esto importa y no es un detalle de eficiencia

No se trata de ahorrar objetos. Un estado con memoria propia **compartido** entre recorridos sería
directamente incorrecto: dos comentarios multilínea en el mismo archivo reportarían la misma posición
de apertura, y dos escapes Unicode se pisarían el contador. La regla existe para que sea imposible
escribir ese bug: si el estado tiene un campo, no puede ser una constante.

## 7. Cómo agregar un token nuevo

El procedimiento completo, como demostración de que la estructura es extensible:

1. Agregar el valor a `TipoDeToken`, con el nombre exacto que debe imprimirse.
2. Si el token es de un solo carácter sin lookahead, agregarlo a la tabla de puntuación de
   `EstadoInicial`. **Listo** — no hace falta ninguna clase nueva.
3. Si necesita más de un carácter, crear una clase por cada estado nuevo del autómata, y agregar en
   `EstadoInicial` la transición que lo dispara.
4. Agregar la ER a [`especificacion-lexica.md`](especificacion-lexica.md) y los casos a
   `resources/`.

Ningún estado existente cambia salvo `EstadoInicial`, y sólo para conocer el punto de entrada.

## 8. Ver también

- [`arquitectura.md`](arquitectura.md) — la visión de conjunto y dónde encaja el driver.
- [`especificacion-lexica.md`](especificacion-lexica.md) — los autómatas que estos estados implementan.
- [`manejo-de-errores.md`](manejo-de-errores.md) — qué hace un estado cuando ninguna transición aplica.
