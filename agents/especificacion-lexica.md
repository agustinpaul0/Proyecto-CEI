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
la salvedad que siempre deben comenzar con un dígito."* Toda esta gramática (incluidos los sufijos
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
