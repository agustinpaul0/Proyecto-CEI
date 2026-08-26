# Especificación léxica y logros

Índice: [`../AGENTS.md`](../AGENTS.md). ER exactas de cada token (todas, incluidos los símbolos de un
solo carácter) y autómatas en
[`../documentacion/especificacion-lexica.md`](../documentacion/especificacion-lexica.md) — este
archivo es la tabla operativa + las decisiones tomadas, no el detalle formal completo.

## Tabla

| Elemento | Regla | Token(s) |
|---|---|---|
| Palabras clave (21) | lookup tras armar un identificador (Clase 02, slide 62-63: **no** se modelan como AF propio, para evitar fusionar decenas de estados con los de identificador) | `pr_class`, `pr_extends`, `pr_interface`, `pr_implements`, `pr_static`, `pr_boolean`, `pr_char`, `pr_int`, `pr_void`, `pr_public`, `pr_if`, `pr_else`, `pr_while`, `pr_return`, `pr_var`, `pr_this`, `pr_new`, `pr_null`, `pr_true`, `pr_false`, `pr_float` |
| idClase / idInterfaz | letra mayúscula + uno o más de (letra, dígito, `_`) — mínimo 2 caracteres | `idClase` |
| idGen (parám. genérico) | una única letra mayúscula | `idGen` |
| idMV (método/variable) | letra minúscula + cero o más de (letra, dígito, `_`) | `idMV` |
| Entero | uno a nueve dígitos (se valida el límite de 9 tras consumir la corrida completa) | `intLiteral` |
| Char | `'x'` (x ≠ `\`, `'`) o `'\x'` (x cualquiera — permisividad explícita de la cátedra) | `charLiteral` |
| String | `"..."` con `\` escapando el carácter siguiente, sin saltos de línea; escapes restringidos al conjunto válido de Java (ver abajo) | `litString` |
| Float (logro) | igual que Java, **siempre** empieza con dígito | `floatLiteral` |
| Puntuación | `( ) { } [ ] ; , . :` | `parenA`/`parenC`, `llaveA`/`llaveC`, `corcheteA`/`corcheteC`, `puntoYComa`, `coma`, `punto`, `dosPuntos` |
| Operadores | `> < ! = == >= <= != && \|\| % + - * / ++ --` | `op_mayor`, `op_menor`, `op_negacion`, `op_asignacion`, `op_igual`, `op_mayorIgual`, `op_menorIgual`, `op_distinto`, `op_and`, `op_or`, `op_modulo`, `op+`, `op-`, `op_mult`, `op_div`, `op_incremento`, `op_decremento` |
| EOF | fin de archivo, no consume carácter (Clase 02, estado especial `e5`) | `EOF`, lexema `$` |

## Palabras reservadas fuera de los PDFs: `float` sí, `double` no

**`float` como palabra reservada**: no figura en ningún PDF de la cátedra (se buscó explícitamente);
se agrega por confirmación directa del profesor en una consulta puntual del usuario. Antes de este
cambio no era palabra clave — un identificador `float` habría tokenizado como `idMV`; ahora tokeniza
como `pr_float` y no puede usarse como nombre de variable/método/clase.

**`double` NO es palabra clave.** Primera versión de esta decisión (revertida): se había agregado
`double` como palabra reservada mapeada al mismo `TipoDeToken.PR_FLOAT` que `float`, interpretando
"tratemos float y double como lo mismo" como "ambas palabras son reservadas". El usuario corrigió esa
lectura: lo que el profesor quiso decir es que MiniJava sólo tiene **un** tipo de dato de punto
flotante (`float`), no que la palabra `double` deba existir como reservada. `double` sigue siendo un
identificador común (`idMV`) — sólo los **sufijos** `f`/`F`/`d`/`D` del literal float (ver abajo,
heredados tal cual de Java) son donde el concepto "double" realmente aparece en la sintaxis de
MiniJava.

## `floatLiteral` en detalle

```text
floatLiteral = Digito+ . Digito* Exponente? Sufijo?
             | Digito+ Exponente Sufijo?
             | Digito+ Sufijo

Exponente = (e|E) (+|-)? Digito+
Sufijo    = f|F|d|D
```

Pautas Etapa 1 dice literalmente: *"Estos deben tener la misma estructura que los floats de Java, con
la salvedad que siempre deben comenzar con un dígito."*

**Los floats no tienen límite de dígitos, ni por la cátedra ni por Java.** El límite de 9 dígitos es
exclusivo de `intLiteral`: la ER del `floatLiteral` usa `Digito+` sin cota en las tres partes (entera,
fraccionaria y exponente), y la frase de Pautas habla de **estructura**, no de rango.

Java tampoco acota la cantidad de dígitos — lo verificado con `javac`:

| literal | `javac` |
|---|---|
| `1.0000…0001f` (60+ dígitos) | **compila** |
| `123456789012345678901234567890.0f` (30 dígitos) | **compila** |
| `3.4e38f` / `3.5e38f` | compila / `floating-point number too large` |
| `1e-45f` / `1e-46f` | compila / `floating-point number too small` |

Lo que Java rechaza es un literal **no nulo que se redondee a infinito o a cero**: un chequeo de
**valor**, no léxico (lo hace al convertir el literal, no al tokenizarlo). **MiniJava no lo hace**, y
la decisión es deliberada: la cátedra pidió la misma *estructura*, y un chequeo de rango es
semántico, no de Etapa 1. Fijado en `resources/sinErrores/floatFueraDelRangoDeJava.java`.

**Asimetría a tener presente**: el chequeo de 9 dígitos de `EstadoEntero` corre **sólo si la corrida
de dígitos no continúa hacia un float**, así que `1234567890.5` es un `floatLiteral` válido con diez
dígitos en la parte entera, mientras que `1234567890` solo es un error. Y el límite de MiniJava es de
**dígitos** mientras que el de Java es de **valor**: `0000000001` (diez dígitos, valor 1) es error en
MiniJava y válido en Java. Toda esta gramática (incluidos los sufijos
`f`/`F`/`d`/`D`) se deriva de esa frase vía el protocolo de [`investigacion.md`](investigacion.md),
ya que `Aspectos Lexicos MiniJava 2026.pdf` no menciona floats en absoluto (no está entre sus "cinco
tipos de literales" — floats es puramente un logro de Pautas, no parte de la especificación base).

## Tres casos especiales — corrección de una confusión anterior

En un intento previo se afirmó, incorrectamente, que el EOF, el comentario multilínea y el límite de
9 dígitos "no eran expresables" con precisión como ER/AF. Es falso; los tres son perfectamente
regulares, corregido en
[`../documentacion/especificacion-lexica.md`](../documentacion/especificacion-lexica.md):

- El EOF es una ER trivial de un símbolo sobre el carácter centinela (`SourceManager.END_OF_FILE`);
  lo único especial es que esa transición no avanza la lectura (detalle del driver, no del lenguaje).
- El comentario multilínea tiene una ER exacta y estándar (la misma que usa `lex`/`flex` para
  comentarios de C): `/\*([^*]|\*+[^*/])*\*+/`. No es una aproximación.
- El límite de 9 dígitos es trivialmente regular (toda repetición acotada lo es): una cadena lineal
  de 9 estados terminales sin bucle, o la ER `D D? D? D? D? D? D? D? D?`. Formulación equivalente que
  el profesor confirmó como válida: una estrella de Kleene "de aridad 1-9", es decir `D{1,9}` — la
  notación de repetición acotada que se expande exactamente a la misma cadena de nueve `D?`
  opcionales (no es una estrella de Kleene sin cota; `*` sin límite sería incorrecto acá, precisamente
  porque el límite de 9 es lo que se está modelando).
- El escape Unicode (`\u` + 4 hex dígitos) acepta repetir el marcador `u` una o más veces
  (`\uu0041`, `\uuu0041`, ...), semántica heredada de Java (JLS §3.3, ver
  [`investigacion.md`](investigacion.md)). La ER real del escape es `\u+[0-9a-fA-F]{4}` (una o más
  `u`), no `\u[0-9a-fA-F]{4}` (una sola).

## Decisiones ambiguas resueltas (documentar también en el informe)

- **`&` y `|` solos son error.** No están en la lista de operadores de `Aspectos Lexicos MiniJava
  2026.pdf` §2.4.2 — sólo `&&` y `||`. Sus estados iniciales son no terminadores.
- **Escapes en `charLiteral`: `'\x'` con `x` arbitrario es válido** — dicho explícitamente en
  `Aspectos Lexicos MiniJava 2026.pdf` §2.3.2. `'\q'` es un `charLiteral` válido con valor `q`.
- **Escapes en `litString`: restringidos al conjunto válido de Java** (JLS §3.10.7, verificado contra
  la especificación — ver [`investigacion.md`](investigacion.md)): `b s t n f r " ' \` o un dígito
  octal `0`-`7`, más el escape unicode. A diferencia de `charLiteral`, la cátedra **no** dice
  "cualquier carácter" para `litString` (§2.3.3 sólo cita a Java para el caso `\"` específicamente),
  así que acá aplica "ante la duda, como en Java" en vez de la permisividad explícita del char.
  `"a\q"` es un error léxico; `"a\s b"`, `"a\7"`, `"a\""` son válidos.
- **`\` dentro de un string consume el siguiente carácter** (semántica Java estándar) → `"a\\"`
  cierra correctamente. La lectura literal de la consigna ("la comilla final nunca estará antecedida
  por `\`") daría un string sin cerrar en ese caso; se prioriza la regla "ante la duda, como en Java".
- **El lexema reportado es siempre el texto crudo del fuente.** Confirmado por
  `testersLexico/Guia_de_uso_testers.md`: se espera `//#'\n',3` (4 caracteres literales), no el salto
  de línea decodificado. El valor decodificado (para etapas posteriores) se guarda aparte en el
  `Token`, nunca reemplaza el lexema.
- **Límite de 9 dígitos**: se consume la corrida completa de dígitos y recién ahí se valida — así no
  se rompe un float largo (`1234567890.5` es válido, la parte entera sola no debe cortar antes).
- **`1e` / `1.5e` sin dígitos después del exponente → error**, sin backtracking a un token más corto.
- **`/*` sin cerrar → error**, con lexema `/*` y la línea de **apertura** del comentario.
- **`1abc` → dos tokens**: `intLiteral(1)` seguido de `idMV(abc)` (maximal munch estándar, igual que
  el lexer de Java).
- **El escape inválido de `litString` es un error NO fatal**: se reporta y el literal se sigue
  leyendo hasta su comilla de cierre, así que el token se emite igual. Es lo que hace `javac`
  (`illegal escape character` no aborta el literal, verificado ejecutándolo). El escape unicode mal
  formado sí es fatal: Java lo resuelve en el pre-lexer (JLS §3.3) y no hay comportamiento que
  copiar.
- **`unicodeHabilitado` gatea sólo el escape `\uXXXX`**, no los caracteres no-ASCII crudos dentro de
  literales: gatear eso exigiría clasificadores ASCII-only en todo el autómata, y el logro se puede
  demostrar igual con el escape.

## Divergencias con Java que quedan fijadas por tests

Ninguna es un descuido: están todas cubiertas por casos de `resources/` a propósito, y hay que
mencionarlas en el informe.

| Caso | Java | MiniJava |
|---|---|---|
| `\uXXXX` fuera de un literal (en un comentario, en un identificador) | se traduce **antes** de tokenizar en todo el archivo (JLS §3.3), así que un `\u12G4` en un comentario no compila | el escape sólo significa algo dentro de un `charLiteral` o un `litString`; en un comentario es texto |
| `"\u0022"` | el escape se traduce primero y la comilla **cierra** el string | no cierra nada: son seis caracteres del lexema |
| Dígitos árabe-índicos (`١٢٣`) | no valen en un literal numérico | `Character.isDigit` los acepta: son un `intLiteral` |
| Dígitos no ASCII en `\uXXXX` | inválidos | `Character.digit(c,16)` los acepta |
| `中` al empezar un identificador | válido | error: no es ni mayúscula ni minúscula, aunque **sí** puede continuar un identificador |
| Form feed y tabulador vertical | son blancos | son errores léxicos |
| BOM al inicio del archivo | se descarta | es un carácter desconocido |
| `3.5e38f`, `1e999` (float fuera de rango) | `floating-point number too large` | `floatLiteral` válido: el rango es un chequeo semántico, no léxico |
| `0000000001` | `intLiteral` válido (el límite es de valor) | error: el límite de MiniJava es de **dígitos** |
| `041` | literal **octal**, vale 33 | `intLiteral` decimal de 3 dígitos |
| `09` | `illegal digit in an octal literal` | `intLiteral` válido de 2 dígitos |
| `0x1F`, `0b1010` | literal hexadecimal / binario | `intLiteral(0)` + `idMV` — la ER de la cátedra es decimal y nada más |
| `1_000` | válido (separadores desde Java 7) | `intLiteral(1)`, error por el `_`, `intLiteral(000)` |
| Un carácter invisible en el mensaje de error | `illegal character: '\u00a0'` — escapado | escapado igual, misma convención |

## Los 8 logros y su estrategia

| Logro | Estrategia |
|---|---|
| Manejador de Archivos Eficiente | **Requisito, no un extra**: ver [`arquitectura.md`](arquitectura.md) "por qué el manejador de la cátedra está roto". Char a char sobre buffer de bloques, sólo `java.io`. |
| Multi-detección de Errores | recuperación en el Módulo Principal; invariante a testear con property-based: cada recuperación consume ≥1 carácter o llega a EOF (garantiza terminación). |
| Reporte de Error Elegante | además de línea y razón, mostrar la línea completa del fuente y un `^` apuntando a la posición — **y también columna**, decisión del usuario más allá de lo pedido por la cátedra. |
| Floats I | activable/testeable por flag, ver `ConfiguracionDelCompilador`. |
| Unicodes | caracteres no-ASCII crudos dentro de literales char/string + escape `\uXXXX` (marcador repetible). |
| Imbatibilidad Léxica | consecuencia de la suite masiva + corpus de `resources/`. |
| Good Job! I | consecuencia de lo anterior. |
| Entrega Anticipada | hito de calendario. |

Los cuatro logros de comportamiento (floats, unicode, reporte elegante, multi-detección) se activan
vía `ConfiguracionDelCompilador(floatsHabilitados, unicodeHabilitado, reporteElegante,
multiDeteccionDeErrores)` — todos `true` por defecto — para poder testear cada combinación por
separado y no acoplar el comportamiento base al de los logros.
