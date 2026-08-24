# El patrón State del Analizador Léxico — cómo interactúan sus piezas

Documento autocontenido: profundiza el §4 de [`arquitectura.md`](arquitectura.md) con el detalle
completo de cómo están conectadas las piezas del patrón State — qué es cada tipo, quién lo crea,
cuándo se reutiliza una instancia y cuándo se crea una nueva. Pensado para poder explicar el
mecanismo entero sin leer código.

## 1. El problema que resuelve

Reconocer un token (por ejemplo, un `floatLiteral` como `1.5e-3f`) no es una decisión de un solo
paso: hay que mirar el `1`, después el `.`, después el `5`, después el `e`, después el `-`, después
el `3`, después el `f`, y en cada paso la pregunta "¿qué significa el próximo carácter?" depende de
**dónde estamos parados** dentro del reconocimiento. Eso "dónde estamos parados" es exactamente un
**estado de un Autómata Finito (AF)**. La cátedra exige modelar el analizador léxico con la técnica
**Método por Estado**: un componente de software por cada estado del AF, que decide la transición
según el carácter actual. Este proyecto implementa esa técnica con el patrón de diseño **State**
(uno de los patrones clásicos de Gang of Four).

## 2. Las cinco piezas y su rol

| Pieza | Tipo | Rol |
|---|---|---|
| `EstadoLexico` | interfaz | El contrato común a todo estado: un único método `procesar(contexto)`. |
| `ContextoLexico` | clase | La "memoria compartida" entre todos los estados durante el reconocimiento de un token: qué carácter se está mirando, qué lexema se lleva armado, en qué línea estamos. |
| `ResultadoDeTransicion` | **interfaz sellada** (`sealed interface`) | Lo que devuelve `procesar()`: sólo puede ser una de dos cosas — nunca una tercera. |
| `Continuar` | `record`, implementa `ResultadoDeTransicion` | "Todavía no hay token completo — el próximo estado a consultar es este otro." |
| `Emitir` | `record`, implementa `ResultadoDeTransicion` | "El token ya está completo — acá está." |

```java
public sealed interface ResultadoDeTransicion permits Continuar, Emitir {}

public record Continuar(EstadoLexico siguiente) implements ResultadoDeTransicion {}

public record Emitir(Token token) implements ResultadoDeTransicion {}
```

**Por qué `sealed`**: `sealed interface ... permits Continuar, Emitir` le dice al compilador
"estos son los ÚNICOS dos tipos que pueden implementar esta interfaz, no puede aparecer un tercero
en ningún otro archivo". Eso es lo que habilita al driver a usar un `switch` **exhaustivo** — el
compilador puede verificar en tiempo de compilación que el `switch` cubre todos los casos posibles,
sin necesitar una rama `default` que "por las dudas" atrape un caso no contemplado. Antes de este
detalle estar bien aprovechado, el driver comprobaba el tipo con `instanceof` y cásteaba el otro caso
a mano — funcionaba, pero no se apoyaba en la garantía del compilador; ver §5.

## 3. El driver: quién pregunta y cuándo para

`AnalizadorLexico.proximoToken()` es el **único lugar** que arranca la cadena de estados y decide
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

Leído en criollo: "empezá en el estado inicial; preguntale qué corresponde hacer con el carácter
actual; si contesta `Continuar`, andá al estado que te indicó y volvé a preguntar; si contesta
`Emitir`, ese es el token — devolvelo y frená." Cada estado sólo sabe resolver **su propio paso**;
ninguno sabe ni le importa cómo se llegó hasta ahí ni qué pasa después de que devuelve su resultado.
Esa separación es la esencia del patrón State: la lógica de "¿qué significa este carácter en esta
situación puntual?" queda encapsulada por completo dentro de la clase de ese estado.

### Por qué un `while` y no la recursión que sugiere la cátedra

La cátedra presenta el AF encadenando llamadas: el estado `e1()` termina llamando a `e2()`, que
termina llamando a `e3()`, etc. Esa técnica sigue siendo Método por Estado — pero cada llamada anidada
ocupa un marco de pila (*stack frame*) de la JVM, y un lexema muy largo (un comentario de miles de
caracteres, un string larguísimo) puede agotar la pila (`StackOverflowError`). El `while` de arriba
logra exactamente el mismo comportamiento observable, sin apilar nada: en cada vuelta hay como máximo
UNA llamada activa a `procesar()`, sin importar cuán largo sea el lexema. Es la única libertad que
este proyecto se tomó respecto a lo que enseña la cátedra, y está documentada explícitamente como tal
(`AGENTS.md` §4) para que quede claro que la técnica exigida — un componente por estado — se respeta
igual.

## 4. Trazado completo: reconocer `1.5e-3f`

| Paso | Estado que actúa | Mira | Qué hace | Devuelve |
|---|---|---|---|---|
| 1 | `EstadoInicial` | `1` | es dígito: lo consume | `Continuar(EstadoEntero)` |
| 2 | `EstadoEntero` | `.` | corrida de dígitos sigue como float: lo consume | `Continuar(EstadoFloatConPunto)` |
| 3 | `EstadoFloatConPunto` | `5` | dígito de la parte decimal: lo consume | `Continuar(EstadoFloatConPunto)` (mismo estado, se re-consulta a sí mismo) |
| 4 | `EstadoFloatConPunto` | `e` | arranca el exponente: lo consume | `Continuar(EstadoFloatExponenteInicio)` |
| 5 | `EstadoFloatExponenteInicio` | `-` | signo del exponente: lo consume | `Continuar(EstadoFloatExponenteRequiereDigito)` |
| 6 | `EstadoFloatExponenteRequiereDigito` | `3` | el dígito obligatorio tras el signo: lo consume | `Continuar(EstadoFloatExponenteDigitos)` |
| 7 | `EstadoFloatExponenteDigitos` | `f` | es sufijo float, no otro dígito: lo consume | `Emitir(Token(FLOAT_LITERAL, "1.5e-3f", línea))` |

Siete vueltas del `while`, siete objetos `ResultadoDeTransicion` construidos y descartados
(son inmutables, de un solo uso), y el driver nunca necesitó saber nada sobre floats — cada estado
resolvió sólo su fragmento.

## 5. `switch` sobre tipo sellado en vez de `instanceof` — por qué importa

Antes de esta versión, el driver comprobaba el resultado así:

```java
// Versión anterior — funciona, pero no aprovecha que el tipo es sealed
if (resultado instanceof Emitir emitir) {
    return emitir.token();
}
estado = ((Continuar) resultado).siguiente();
```

Esto **funciona** — pero tiene dos problemas de fondo que el `switch` de la §3 resuelve:

1. **No hay chequeo de exhaustividad.** El compilador no puede avisar si en algún momento
   `ResultadoDeTransicion` gana un tercer tipo (`permits Continuar, Emitir, OtroTipo`): el código de
   arriba seguiría compilando igual, y el casteo `(Continuar) resultado` explotaría en tiempo de
   ejecución con un `ClassCastException` recién cuando ese tercer caso realmente ocurriera — un bug
   que el compilador podría haber atrapado, convertido en uno que sólo aparece en producción.
2. **El casteo es manual y silencioso.** `((Continuar) resultado)` le pide al lector confiar en que
   "si no era `Emitir`, tiene que ser `Continuar`" — una inferencia correcta hoy, pero que nada la
   deja explícita ni la protege a futuro.

El `switch` sobre un tipo `sealed` convierte esa inferencia implícita en una garantía del compilador:
si mañana aparece un tercer tipo que implemente `ResultadoDeTransicion`, el `switch` de
`AnalizadorLexico` directamente **no compila** hasta agregar el `case` correspondiente. Es el mismo
principio por el que `TipoDeToken` es un `enum` y no un `String` suelto: dejar que el compilador
haga el trabajo de mantener los casos sincronizados, en vez de confiar en que un humano se acuerde.

## 6. Instancias compartidas (`INSTANCIA`) vs. instancias nuevas (`new`) — el criterio

Mirando el paquete `minijava.lexico.estados` se ven dos estilos de construcción, y **la elección no
es arbitraria**: depende de si el estado necesita "recordar" algo específico de *este* recorrido en
particular, o si su comportamiento es una función pura del `ContextoLexico` que recibe.

### Estados sin memoria propia → un único singleton `INSTANCIA` compartido para todo el programa

`EstadoInicial`, `EstadoIdMV`, `EstadoEntero`, `EstadoStringAbierto`, `EstadoCharCerrando`, y la
mayoría de los estados del paquete: **no tienen ningún campo de instancia**. Todo lo que necesitan
para decidir la transición ya está en el `ContextoLexico` que reciben como parámetro. Como no cargan
ningún dato propio, una única instancia — creada una sola vez como constante estática — es
perfectamente segura de reutilizar en cualquier momento, para cualquier token, incluso de forma
intercalada (por ejemplo durante la recuperación de Multi-detección de Errores, donde el driver
puede volver a pasar por `EstadoInicial` muchas veces en la misma corrida):

```java
public final class EstadoIdMV implements EstadoLexico {
    public static final EstadoIdMV INSTANCIA = new EstadoIdMV();
    private EstadoIdMV() {}
    // procesar(...) sólo lee `contexto` — nunca `this`
}
```

Este es el mismo motivo por el que `EstadoOperadorConSufijoOpcional` no usa un `INSTANCIA` genérico
sino seis constantes con nombre propio (`MAYOR`, `MENOR`, `NEGACION`, `ASIGNACION`, `SUMA`, `RESTA`):
cada una es igual de "sin memoria de recorrido" — sus campos (`sufijo`, `conSufijo`, `sinSufijo`) son
**configuración fija de esa variante**, no datos que cambien durante el reconocimiento. Siguen siendo
singletones reutilizables; sólo tienen seis "sabores" en vez de uno.

### Estados con memoria propia del recorrido actual → `new` en cada transición hacia ese estado

Tres estados sí necesitan cargar un dato que es específico de **este** recorrido puntual, y por eso
se construyen con `new` justo en el momento de transicionar hacia ellos — nunca como constante
estática:

- **`EstadoComentarioMultilinea(lineaDeApertura, ultimoFueAsterisco)`**: si el comentario nunca
  cierra, el error tiene que reportar la línea donde **abrió** el comentario, no la línea donde se
  encontró el fin de archivo. Esa línea de apertura es distinta cada vez que se entra a un
  comentario — no puede ser una constante compartida. `ultimoFueAsterisco` también cambia en cada
  paso (ver el AF de dos estados en `arquitectura.md` §6.6), así que cada transición dentro del
  comentario construye una instancia nueva con el valor actualizado.
- **`EstadoEscapeUnicode(digitosRestantes, siguienteAlTerminar)`**: cuenta cuántos hex dígitos faltan
  (empieza en 4 y decrece), y sabe a qué estado volver al terminar — que es distinto según si el
  escape está dentro de un `charLiteral` (`EstadoCharCerrando`) o de un `litString`
  (`EstadoStringAbierto`). Ninguno de los dos datos es fijo entre usos.
- **`EstadoMarcadorUnicode(siguienteAlTerminar)`**: consume la corrida de `u` repetidos del marcador
  del escape Unicode (`\uu0041` es válido — ver `arquitectura.md` §6.1) y también necesita saber a
  qué estado volver, por la misma razón que el anterior.

**La regla general, en una frase**: si `procesar()` sólo consulta al `ContextoLexico`, el estado es
un singleton (`INSTANCIA`, creado una vez); si el estado necesita cargar algún dato que varía según
*cómo se llegó hasta ahí en este recorrido puntual*, se construye con `new` en el momento de la
transición. Se revisaron los 22 archivos de `minijava.lexico.estados` uno por uno para esta
documentación y los tres casos de arriba son los únicos con campos de instancia — el resto son
singletones sin estado, consistente con la regla.

## Ver también

- [`arquitectura.md`](arquitectura.md) — visión completa del proyecto, especificación léxica formal.

Este documento es autocontenido y no depende de `AGENTS.md` para entenderse. Es `AGENTS.md` el que
enlaza hacia acá, no al revés.
