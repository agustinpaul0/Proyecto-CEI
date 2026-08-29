# Catálogo de decisiones

Documento autocontenido. Reúne **todas** las decisiones que se tomaron en el proyecto donde había más
de una opción defendible: qué se eligió, qué se descartó, de dónde sale la respuesta y por qué.

Está organizado para poder auditarlo: cada decisión declara su **fuente** (una consigna de la
cátedra, la especificación de Java, una verificación empírica, o un criterio propio) y, donde
corresponde, el caso de test que la fija.

## 1. Las dos reglas que gobiernan todo

**Regla 1 — Lo que la cátedra especifica se respeta al pie de la letra.** El formato de salida, el
código de error, la técnica de implementación y las expresiones regulares salen de
`Aspectos Lexicos MiniJava 2026.pdf`, `Pautas Etapa 1.pdf` y las clases 01 y 02. Cuando este
proyecto tiene una opinión distinta, gana la cátedra.

**Regla 2 — Lo que la cátedra no especifica se resuelve como en Java.** Está escrita explícitamente
por la cátedra, en la última línea del README de la Etapa 1: *"todo lo que NO esté especificado para
MiniJava tiene comportamiento por defecto igual al de Java"*.

La segunda regla tiene un procedimiento asociado, y es tan importante como la regla misma:

> **"Como en Java" nunca se decide de memoria.** Se verifica contra la especificación del lenguaje
> (JLS) o **ejecutando `javac`** sobre el caso concreto, y el resultado de esa ejecución queda
> registrado en la documentación y fijado en un test.

La razón es práctica: el comportamiento de Java en los bordes (escapes, literales octales, límites
numéricos, escapes Unicode) presenta casos no evidentes, y una suposición razonable resulta equivocada con
frecuencia. Varias de las decisiones de este documento **contradicen** lo que uno esperaría, y sólo
se descubrió al ejecutar el compilador de referencia.

## 2. Decisiones sobre la técnica de implementación

### 2.1. Método por Estado, implementado como patrón State

**Fuente**: cátedra (Clase 02). Presenta tres estrategias —Método por Estado, Switch/Case, Tabla de
transiciones— y fija la primera.

**Decisión**: un componente de software por cada estado del autómata, implementado como el patrón de
diseño State (GoF): una clase por estado, con un único método `procesar(contexto)`.

**Descartado**: las otras dos estrategias, y cualquier generador automático (JFlex, ANTLR, lex/flex),
que la consigna prohíbe expresamente.

### 2.2. Driver iterativo en vez de encadenamiento recursivo

**Fuente**: criterio propio. Es la **única** desviación respecto de lo que muestran las slides.

**El problema**: la cátedra encadena los estados con llamadas recursivas (`return e2()` dentro de
`e1()`). Cada carácter del lexema apila un marco de pila de la JVM, así que un comentario o un string
de decenas de miles de caracteres puede agotar la pila con `StackOverflowError`. Eso no es un error
léxico: es una caída del compilador ante una entrada legal.

**Decisión**: un driver central hace un bucle `while` y consulta al estado actual en cada vuelta. Se
conserva exactamente la estructura de "un componente por estado" que exige la técnica; sólo cambia el
mecanismo por el que se encadenan.

**Costo**: hay que materializar el resultado de cada transición como un objeto (`Continuar` o
`Emitir`) en vez de devolverlo por la pila. Son objetos inmutables de un solo uso, y el costo es
despreciable frente a la garantía de no caerse.

### 2.3. `ResultadoDeTransicion` es una `sealed interface`

**Fuente**: criterio propio.

**Decisión**: el tipo que devuelve cada estado es una interfaz sellada con exactamente dos
implementaciones permitidas (`Continuar`, `Emitir`). Eso permite que el driver use un `switch`
**exhaustivo sin rama `default`**: el compilador verifica que estén cubiertos todos los casos
posibles.

**Descartado**: comprobar con `instanceof` y castear el otro caso a mano. Funciona, pero si mañana
apareciera un tercer tipo de resultado el código seguiría compilando y fallaría en tiempo de
ejecución con un `ClassCastException`. Con el `switch` sellado, directamente no compila hasta cubrir
el caso nuevo.

### 2.4. Los 49 tokens de lexema fijo se nombran con una tabla, no con estados

**Fuente**: cátedra (Clase 02, slides 62-63), que recomienda explícitamente esta técnica para las
palabras clave. Acá se generaliza al resto de los tokens que están en la misma situación.

**El punto de partida**: de los 56 tokens, **49 tienen lexema fijo** —las 21 palabras clave, los 10
símbolos de puntuación, los 17 operadores y `EOF`—, en el sentido de que el tipo y el lexema se
determinan mutuamente. Los otros 7 (`idClase`, `idGen`, `idMV` y los cuatro literales) leen su
lexema del fuente.

**Decisión**: una única tabla, `TablaDeLexemas`, con las dos direcciones — lexema → tipo para emitir,
tipo → lexema para el `$` del `EOF`, que no se lee del fuente. Ningún estado del autómata lleva un
`TipoDeToken` encima: delimita el lexema y consulta la tabla. Las palabras clave son un subconjunto
de esa tabla y siguen funcionando igual que siempre: se reconoce un identificador común y, antes de
emitir, se consulta.

**Lo que la tabla no puede hacer, y por eso el autómata sigue haciendo falta**: el *maximal munch*.
La tabla no sabe si un `>` es `op_mayor` o el principio de un `op_mayorIgual`; eso lo decide el
autómata mirando el carácter siguiente, y recién cuando terminó de consumir consulta. El reparto es
**el autómata delimita, la tabla nombra**.

**Qué se gana**: la correspondencia tipo ↔ lexema deja de estar repetida en seis lugares. Como
efecto concreto, `EstadoOperadorConSufijoOpcional` pasa de seis instancias a tres —lo único que
distinguía a `>` de `<` era el tipo de token— y `EstadoOperadorDoble` de dos a una. Y habilita dos
tests que antes no eran expresables: que ningún tipo de token quede sin entrada en la tabla, y que
cada uno de los 49 lexemas, suelto en un fuente, produzca exactamente el tipo que la tabla promete.

**Descartado**: un autómata dedicado por palabra clave. Requeriría casi un estado por letra de cada
palabra, fusionados con los estados de identificador — decenas de estados frágiles para algo que una
tabla resuelve en tiempo constante.

## 3. Decisiones sobre el lenguaje reconocido

### 3.1. `float` es palabra reservada; `double` no

**Fuente**: consulta directa al profesor. `float` **no figura en ningún PDF** de la cátedra — se
buscó explícitamente — y se agrega por confirmación del docente.

**Decisión revertida, y vale la pena documentarla**: en una primera versión se agregó también
`double` como palabra reservada, mapeada al mismo tipo de token que `float`, interpretando "tratemos
`float` y `double` como lo mismo" como "ambas palabras son reservadas". Esa lectura era incorrecta:
lo que significa es que MiniJava tiene **un solo** tipo de dato de punto flotante. `double` sigue
siendo un identificador común (`idMV`). El único lugar donde el concepto "double" aparece en la
sintaxis de MiniJava son los sufijos `d`/`D` de un literal float, heredados de Java.

### 3.2. `floatLiteral` — la gramática completa se derivó de una sola frase

**Fuente**: Pautas Etapa 1: *"Estos deben tener la misma estructura que los floats de Java, con la
salvedad que siempre deben comenzar con un dígito"*. `Aspectos Lexicos MiniJava 2026.pdf` **no
menciona floats en absoluto** — no están entre sus "cinco tipos de literales". Los floats son
puramente un logro.

**Decisión**: se adopta la gramática de Java (parte entera, parte fraccionaria, exponente con signo
opcional, sufijo `f`/`F`/`d`/`D`) con la única restricción de que siempre empiece con dígito. La ER
completa está en [`especificacion-lexica.md`](especificacion-lexica.md) §4.

**Consecuencia verificada**: `.5` **no** es un float en MiniJava (sí lo es en Java). Produce dos
tokens, `punto` e `intLiteral(5)`.

### 3.3. Los floats no tienen límite de dígitos; los enteros sí

**Fuente**: cátedra para el entero (1 a 9 dígitos), Java verificado con `javac` para el float.

**El límite de 9 dígitos es exclusivo de `intLiteral`.** La frase de Pautas sobre floats habla de
*estructura*, no de rango, y Java tampoco acota la cantidad de dígitos de un literal de punto
flotante. Verificado ejecutando `javac`:

| literal | `javac` |
|---|---|
| `1.0000…0001f` (60+ dígitos) | compila |
| `123456789012345678901234567890.0f` (30 dígitos) | compila |
| `3.4e38f` / `3.5e38f` | compila / `floating-point number too large` |
| `1e-45f` / `1e-46f` | compila / `floating-point number too small` |

Lo que Java rechaza es un literal no nulo que **se redondee** a infinito o a cero: un chequeo de
**valor**, hecho al convertir el literal, no al tokenizarlo. **MiniJava no lo implementa**, y es
deliberado: la consigna pidió la misma *estructura*, y un chequeo de rango es semántico, no léxico —
no corresponde a la Etapa 1. Fijado en `resources/sinErrores/floatFueraDelRangoDeJava.java`, donde
`3.5e38f`, `1e-46f`, `1.8e308` y `1e999` son todos `floatLiteral` válidos.

**Asimetría que conviene tener presente**: el chequeo de 9 dígitos corre **sólo si la corrida de
dígitos no continúa hacia un float**. Por eso `1234567890.5` es un `floatLiteral` válido con diez
dígitos en la parte entera, mientras que `1234567890` solo es un error.

### 3.4. Escapes en `charLiteral`: cualquier carácter

**Fuente**: `Aspectos Lexicos MiniJava 2026.pdf` §2.3.2, que lo dice explícitamente: `'\x'` con `x`
cualquier carácter.

**Decisión**: `'\q'` es un `charLiteral` válido, con valor `q`. Es una **permisividad explícita** de
la cátedra, más laxa que Java.

**Hasta dónde llega "cualquier carácter"**: no hay restricción a ASCII —`'\ñ'`, `'⭐'` y `'中'` son
válidos—, pero sí un límite técnico heredado de la interfaz provista. `SourceManager` declara
`char getNextChar()`, y un `char` de Java es **una unidad de código UTF-16**, de 16 bits. Por eso lo
que vive en el BMP entra y lo que está fuera —un emoji como `'🌟'`, que es un par subrogado— no: son
dos unidades, y el literal sale *mal formado*. Lo mismo pasa con `'⭐️'`, que es la estrella más un
selector de variación `U+FE0F` aunque se dibuje igual que `'⭐'`.

**No es una divergencia con Java**: `javac` responde
`error: character literal contains more than one UTF-16 code unit`, o sea la misma limitación con el
mismo motivo. **Descartado** trabajar con *code points* (`int`) para admitir caracteres astrales:
exigiría cambiar la firma de `getNextChar()`, que es material de la cátedra y no se toca. Ver
[`especificacion-lexica.md`](especificacion-lexica.md) §3.2.

### 3.5. Escapes en `litString`: no hay lista de escapes, hay una regla posicional

**Fuente**: `Aspectos Lexicos MiniJava 2026.pdf` §2.3.3, textual. Es un caso de manual de la Regla 1:
la cátedra **sí** especifica esto, así que no hay que ir a buscar a Java.

**Lo que dice la consigna**, y conviene citarlo entero porque cada palabra cuenta:

> *"es posible que en la secuencia de caracteres aparezca una comilla doble siempre y cuando se la
> anteceda por el caracter `\`. Por esto último la comilla doble que marca el final de un string
> correcto nunca estará antecedida por ese caracter."*

**La diferencia de forma con `charLiteral`, que es la clave.** §2.3.2 le da al `charLiteral` una
**producción con dos alternativas** (`'x'` y `'\y'`, con `x` e `y` variables independientes): eso es
una regla de **apareamiento**, la barra y el carácter siguiente forman una unidad, y de ahí sale que
`'\\'` y `'\''` sean válidos. §2.3.3 no da ninguna producción: da un **lookbehind de un carácter**
sobre el texto crudo. Son dos mecanismos distintos, y copiarle al `charLiteral` su apareamiento sería
agregarle al `litString` una regla que la consigna no le dio.

**Decisión**: cierra la primera `"` que no esté inmediatamente antecedida por una `\`. No existe un
conjunto de escapes válidos: `"hola \q"` es un `litString` común, con una barra y una `q` como dos
caracteres cualesquiera.

**Consecuencias**:

| Fuente | Resultado |
|---|---|
| `"hola \q"` | `litString` válido |
| `"di \"hola\" chau"` | **un solo** `litString` |
| `"a\\"` | `litString sin cerrar` — la `"` final está antecedida por `\` |
| `"a\\\""` | `litString` válido — la primera `"` no cierra, la segunda sí |
| `"barra \\ al medio"` | `litString` válido |

La tercera fila es la única divergencia con Java que sale de esto: en Java `"a\\"` es la barra
invertida, porque ahí la primera barra escapa a la segunda. Acá una barra no "cancela" a la anterior
— la regla no habla de pares, habla de qué carácter antecede a la comilla. Está en §7.

**Descartado**: extenderle al `litString` el apareamiento de §2.3.2, que haría válido `"a\\"` y lo
alinearía con Java. Se descarta porque contradice la frase citada arriba, que es explícita: *"la
comilla doble que marca el final de un string correcto **nunca** estará antecedida por ese
caracter"*. Donde la cátedra habla, no se aplica "como en Java" (Regla 1).

**Lo único que sí se valida dentro del literal** es el escape Unicode `\uXXXX`, que es un logro
aparte (§3.9) y cuyo criterio de error confirmó el profesor.

### 3.7. `&` y `|` sueltos son error

**Fuente**: `Aspectos Lexicos MiniJava 2026.pdf` §2.4.2. La lista de operadores incluye `&&` y `||`,
y **no** incluye `&` ni `|` por separado.

**Decisión**: los estados que reconocen el primer carácter de esos operadores son **no terminadores**:
si no viene el segundo carácter, es un error léxico, no un token.

### 3.8. `''` (charLiteral vacío) es error

**Fuente**: la ER de la cátedra, más `javac` como confirmación.

La ER es `'x'` con `x ≠ \, '` o bien `'\x'`. La cadena vacía no está en ninguna de las dos
alternativas, así que no hay ambigüedad por el lado de la cátedra. `javac` coincide:
`empty character literal`.

Fijado en `resources/conErrores/tresComillasSimples.java`, que además ejercita la cascada: sobre
`'''`, las dos primeras comillas dan el error de char vacío y la tercera abre un `charLiteral` que
nunca cierra.

### 3.9. El escape Unicode admite el marcador `u` repetido

**Fuente**: JLS §3.3, verificado contra la especificación.

**Decisión**: `\u0041`, `\uu0041` y `\uuuu0041` son todos escapes válidos y equivalentes. La ER real
es `\u+[0-9a-fA-F]{4}` (una o más `u`), no `\u[0-9a-fA-F]{4}`.

Es un detalle que se descubrió leyendo la especificación y que uno no adivinaría: existe porque en
Java el pre-procesador de escapes Unicode debe poder representar un `\u` literal sin re-traducirlo.

### 3.10. `unicodeHabilitado` sólo controla el escape, no los caracteres crudos

**Fuente**: criterio propio.

El logro *Unicodes* cubre dos cosas: caracteres no-ASCII crudos dentro de literales (`'ñ'`,
`"letra ü"`) y la secuencia escapada `\uXXXX`. El interruptor de configuración desactiva **sólo la
segunda**.

**Por qué**: gatear también los caracteres crudos exigiría meter clasificadores "sólo ASCII" en cada
estado del autómata, ensuciando todo el léxico para demostrar algo que el escape ya demuestra por sí
solo.

### 3.11. Maximal munch, sin backtracking

**Fuente**: técnica estándar, coincide con el lexer de Java.

**Decisión**: se arma el lexema más largo posible y, cuando ya no se puede extender, se emite el
token. Consecuencias verificadas:

- `1abc` → dos tokens: `intLiteral(1)` + `idMV(abc)`. No es un error.
- `1e` → **error**, sin retroceder a emitir `intLiteral(1)` + `idMV(e)`. Una vez que se consumió la
  `e` como inicio de exponente, no se vuelve atrás.

## 4. Decisiones sobre el manejo de errores

El desarrollo completo está en [`manejo-de-errores.md`](manejo-de-errores.md); acá van las
decisiones con su fuente.

### 4.1. El lexema reportado es el armado hasta el momento del error

**Fuente**: `Pautas Etapa 1.pdf`, slide 24, literal: *"Deberá reportarse un mensaje adecuado a la
naturaleza del error indicando el lexema armado hasta el momento del error y en qué número de línea
del fuente MiniJava se produjo"*, y el código `[Error:Lexema|NroLinea]` lleva ese mismo lexema.

**Consecuencia visible**: con dos escapes inválidos en un mismo string, el segundo error reporta un
lexema que **contiene** al del primero (`"a\q` y después `"a\q b\z`). Parece redundante, pero es
exactamente lo que la consigna pide.

### 4.2. Recuperación estructural: se consume exactamente lo que se reporta

**Fuente**: verificación empírica sobre `javac`. La cátedra **no dice nada** sobre cómo recuperarse,
porque su comportamiento base ni siquiera tiene recuperación.

**Decisión**: todo estado que lanza un error consume del fuente exactamente los caracteres que
reporta en el lexema, ni uno más. No existe ningún método `recuperarDeError()`: el siguiente
`proximoToken()` arranca solo en el primer carácter que no formó parte del lexema.

**Verificado con `javac`**:

| fuente | qué hace `javac` |
|---|---|
| `char c = 'abbc';` | dos errores `unclosed character literal`, y `bbc` tokenizado como identificador |
| `char d = 'a;` | un error en la comilla; **el `;` sobrevive** |
| `String s = "hola \q mundo";` | un `illegal escape character`, y **sigue leyendo el literal** hasta la comilla de cierre |
| `int c = 1234567890123 + 9;` | un `integer number too large`; **el `+ 9` sobrevive** |

O sea: `javac` no sincroniza hasta un delimitador, y nunca descarta un carácter que no formó parte
del lexema reportado.

**El defecto que esto corrigió**: la implementación anterior descartaba siempre el carácter que estaba
mirando, incluso cuando nunca había formado parte del error. Consecuencias reales, todas invisibles
en el corpus de entonces porque cada caso tenía un espacio detrás del lexema malo:

| fuente | antes | ahora |
|---|---|---|
| `v1 &v2` | `[Error:&\|1]` + `(intLiteral,2,1)` — **se comía la `v`** | `[Error:&\|1]` + `(idMV,v2,1)` |
| `1e;` | `[Error:1e\|1]`, el `;` desaparecía | `[Error:1e\|1]` + `(puntoYComa,;,1)` |
| `1234567890+1` | el `+` desaparecía sin mensaje | `[Error:1234567890\|1]` + `(op+,+,1)` + `(intLiteral,1,1)` |
| `'ab'` | `[Error:'a\|1]` + `[Error:'\|1]`, la `b` desaparecía | `[Error:'a\|1]` + `(idMV,b,1)` + `[Error:'\|1]` |

**Beneficio colateral**: la terminación queda garantizada estructuralmente. El lexema de un error
nunca es vacío, así que cada error consumió al menos un carácter y el análisis siempre avanza — antes
eso lo garantizaba el método que descartaba un carácter, que era justamente el que rompía la
correspondencia.

### 4.3. Un escape mal formado descarta el `litString` hasta su comilla de cierre

**Fuente**: **indicación del profesor de la materia**. Es el único caso del catálogo en que la
fuente no es ni la consigna escrita ni Java: el profesor revisó la salida del compilador y fijó el
criterio explícitamente. Por eso pisa a la Regla 2 — donde la cátedra habla, no se aplica "como en
Java".

**Decisión**: ante un escape Unicode sin sus cuatro dígitos hexadecimales dentro de un `litString`
(`"a\u12G4"`) se reporta el error y se **descarta lo que queda del literal hasta su comilla de
cierre**, sin emitir token.

**Por qué es el único caso**: desde §3.5, una barra seguida de cualquier otra cosa no es un error
dentro de un `litString`, así que el escape Unicode es el único contenido del literal que puede
estar mal formado. Los otros dos errores posibles —sin cerrar por salto de línea o por fin de
archivo— no necesitan recuperación: ya consumieron hasta el final de la línea.

**La alternativa que se descartó**: reportar el escape como error **no fatal** y seguir leyendo el
literal hasta emitirlo entero como token, que es lo que hace `javac`. El profesor consideró esa
salida incorrecta: el compilador afirmaría haber reconocido un literal cuyo contenido no supo
interpretar.

**Por qué el criterio nuevo se sostiene**, más allá de la indicación: es **recuperación en modo
pánico con token de sincronización**, la técnica clásica ante un error de análisis — se descarta
entrada hasta un delimitador que devuelve al analizador a un estado conocido. El delimitador natural
de un `litString` es su comilla de cierre.

**También descartado**: dejar que la recuperación estructural de §4.2 siga su curso, sin descartar
nada. Sobre `"a\u12G4" + v1` eso produce `idMV(G4)` y —lo decisivo— la comilla de **cierre** abre un
literal nuevo que se come ` + v1` y muere sin cerrar: tokens fantasma, errores inventados, y dos
tokens legítimos (`+` y `v1`) perdidos. Descartar hasta el cierre los conserva.

**Consecuencia sobre §4.2**: es la única excepción a "se consume exactamente lo que se reporta". El
lexema reportado sigue siendo el armado hasta el error (`"a\u12`, como exige §4.1); lo descartado es
recuperación y no aparece en ningún lexema. La excepción está verificada en la propiedad de
reconstrucción del fuente.

**Qué queda afuera**: dentro de un mismo literal sólo se reporta el primer error, porque el resto ya
se descartó. Entre literales distintos la multi-detección sigue intacta: `"a\u12G4" "b\u34Z6"`
reporta dos errores. En `charLiteral` la resincronización no aplica: no hay contenido de largo
arbitrario que descartar, así que la recuperación es estructural.

**Divergencia con Java, asumida**: `javac` sobre un escape Unicode roto sigue leyendo el literal.
MiniJava no lo emite. Está en la tabla de §7.

### 4.4. Dónde apunta el `^`: el criterio de `javac`

**Fuente**: `javac`, verificado ejecutándolo caso por caso. La cátedra **no pide columna** — es un
agregado propio del logro *Reporte de Error Elegante* —, así que aplica la Regla 2.

**La regla, una sola**: si el mensaje habla de un **carácter ofensivo puntual**, el `^` va a ese
carácter; si habla de la **construcción entera** que falló, el `^` va a donde esa construcción
**empieza**.

| fuente | diagnóstico de `javac` | señala |
|---|---|---|
| `v1 # v2` | `illegal character` | el `#` |
| `"a\q b\z c"` | `illegal escape character` (dos veces) | la `q` y la `z` — **no** la barra |
| `'\u12G4'` | `illegal unicode escape` | la `G` |
| `''` | `empty character literal` | la comilla de **apertura** |
| `'a` / `'ab'` | `unclosed character literal` | la comilla de **apertura** |
| `"hola sin cerrar` | `unclosed string literal` | la comilla de **apertura** |
| `1234567890123` | `integer number too large` | el **primer** dígito |
| `1e` | `malformed floating-point literal` | el **primer** carácter |
| `/* sin cerrar` | `unclosed comment` | la barra de **apertura** |

Además de imitar a Java, tiene sentido propio: en `"hola sin cerrar` lo que hay que corregir es la
comilla de apertura (le falta el par), no la última letra que se alcanzó a leer.

Fijado caso por caso en `PosicionDelCaretTest`, con el diagnóstico de `javac` citado en cada uno.

### 4.5. El lexema de un error nunca lleva un carácter invisible crudo

**Fuente**: criterio propio, con `javac` como precedente de la convención.

**El problema**: el lexema se imprime literal en el encabezado y dentro de `[Error:Lexema|NroLinea]`.
Un carácter de control ahí es, en el mejor de los casos, invisible; y si es un salto de línea,
**parte el código de error en dos líneas** y rompe el contrato de la cátedra de un elemento por
línea.

**Decisión**: todo carácter invisible del lexema de un error se renderiza con su escape de Java
(`\n`, `\t`, `\uXXXX`…). Es la misma convención de `javac`, que ante un espacio no rompible reporta
literalmente `illegal character: '\u00a0'` — con el escape, no con el carácter.

"Invisible" no son sólo los controles C0: incluye los caracteres de formato (el BOM entre ellos), los
separadores de espacio que no son el espacio común, y los surrogados sueltos. Un par surrogado bien
formado (un emoji) se deja crudo, porque se ve perfecto y escaparlo empeoraría el mensaje.

**Alcance**: vale **sólo** para el lexema de un error. El de un token se imprime crudo, porque el
contrato de la cátedra es que sea el texto exacto del fuente.

### 4.6. Un tipo de error por punto de falla

**Fuente**: criterio propio.

**Antes**: los once puntos de falla compartían una única frase genérica. El reporte no podía decir
qué construcción había fallado, y los tests no podían validar por tipo — sólo por el texto completo
del mensaje, que es exactamente lo que un test no debería mirar.

**Decisión**: `TipoDeErrorLexico` tiene un valor por punto de falla, cada uno con su razón. El
mensaje de `CARACTER_DESCONOCIDO` conserva el texto literal del ejemplo de la cátedra
(`<lexema> no es un símbolo válido`) porque es el único que ella fija; los demás estrenan razón
propia.

**Consecuencia deseada**: los tests validan errores **por tipo y localización**, nunca por el texto
del mensaje. Cambiar la redacción de un mensaje no rompe ningún test.

### 4.7. "Sin cerrar" y "mal formado" son errores distintos

**Fuente**: criterio propio, a pedido explícito durante el desarrollo.

Sobre un `charLiteral` que no cierra donde debía, el estado mira qué lo interrumpió: si fue un fin de
línea o de archivo, el literal **nunca se cerró**; si fue un carácter cualquiera, el literal **tiene
más de un carácter**. Son dos problemas distintos y merecen dos mensajes distintos. Sólo cambia el
encabezado legible: el código `[Error:Lexema|NroLinea]` no cambia ni de formato ni de contenido.

### 4.8. Un comentario de línea no puede estar mal formado

**Fuente**: derivado de la gramática, verificado contra el código y contra `javac`.

Vale dejarlo escrito porque la pregunta reaparece: **no falta ningún caso de prueba de "comentarios
de línea mal formados", porque esa categoría de error no existe**. Un `//` se cierra con un `'\n'` o
con el fin de archivo, y las dos terminaciones son igual de válidas — `javac` compila sin una queja
un archivo cuya última línea es un comentario sin salto final. Por eso `EstadoComentarioDeLinea` no
tiene ninguna rama de error y no hay ningún `TipoDeErrorLexico` que pueda producir: descarta
caracteres hasta una de esas dos condiciones y vuelve a `EstadoInicial`.

El único error de la familia comentario es `COMENTARIO_SIN_CERRAR`, exclusivo de `/* */`, que sí
tiene una terminación obligatoria.

Lo que sí hacía falta cubrir son los **bordes**, y ahí el corpus tenía huecos reales: qué pasa con un
`//` que contiene una construcción que en otro contexto abriría algo (`"`, `'`, `/*`), con un `//`
vacío entre dos tokens, con uno terminado en barra invertida (Java no tiene continuación de línea) y
con uno cerrado por `\r` solo o por `\r\n`. Los casos que terminan exactamente en el constructo
—el archivo entero es `//`, o `/*`, o `/`— no se pueden escribir en el formato del corpus, porque
sus líneas de oráculo son ellas mismas comentarios y quedarían después; viven como unit tests en
`AnalizadorLexicoTest`.

## 5. Decisiones de arquitectura

Desarrolladas en [`arquitectura.md`](arquitectura.md) §5; resumen con su justificación:

| Decisión | Por qué |
|---|---|
| `Salida` es una interfaz, no `System.out` directo | Permite testear el Módulo Principal completo con `assertEquals` contra una lista de líneas, sin capturar el stream global de la JVM. Es la decisión de mayor impacto en testeabilidad del proyecto. |
| Todas las dependencias por constructor | Cada pieza se sustituye en tests sin frameworks de mocking. |
| Los cuatro logros de comportamiento detrás de flags | Sin interruptor, el comportamiento base que pide la consigna quedaría sepultado y sería imposible demostrar que también se cumple. |
| Sin `System.exit()` ni estado estático mutable | Los testers de la cátedra invocan `main()` in-process y repetidamente: un `exit` mataría la corrida y un estático mutable contaminaría un caso con el anterior. |
| Un estado parametrizado para los seis operadores con sufijo | Seis clases idénticas salvo dos caracteres no aportan nada. |
| Puntuación de un carácter con un `Map`, no una clase por símbolo | Ídem: diez clases triviales contra una tabla. |
| `Token` es un `record` inmutable | Un token no cambia después de reconocido; hacerlo inmutable elimina toda una clase de errores. |
| `TipoDeToken` es un `enum`, no un `String` | El compilador mantiene los casos sincronizados en vez de confiar en la memoria de quien edita. |
| Las clases de la cátedra quedan intactas | Sólo se les cambió el `package`. El reemplazo es una clase nueva; las originales quedan como referencia de lo recibido. |

## 6. Decisiones de proceso

### 6.1. Los tests importan más que el código de producción

Es una regla explícita del proyecto, no una aspiración. Todo código nuevo lleva tests; todo defecto
encontrado se convierte en un test de regresión permanente que **nunca** se borra ni se renumera.
Un error léxico ante un programa inválido es comportamiento esperado; una excepción no controlada
—cualquier cosa que no sea `ExcepcionLexica`— es siempre un defecto y hace fallar el test.

Detalle completo en [`testing.md`](testing.md).

### 6.2. Nunca se ajusta un test para acomodar una implementación equivocada

Si un test falla, se arregla el código. Si la especificación cambió legítimamente, se actualiza el
test **y se documenta el motivo**. Las expectativas de columna que cambiaron al adoptar el criterio
de `javac` (§4.4) son un ejemplo: la expectativa vieja era la incorrecta, y quedó dicho por qué.

### 6.3. El material de la cátedra se lee como imágenes, no con extracción de texto

Las slides tienen diagramas de autómatas, capturas de código con globos de explicación y anotaciones
superpuestas. Extraer sólo el texto pierde la mitad de la información técnica — por ejemplo, el
ejemplo completo de implementación del analizador de la Clase 02 es un diagrama.

## 7. Dónde MiniJava diverge de Java, a propósito

Ninguna de estas divergencias es un descuido: todas están cubiertas por casos de test escritos a
propósito, y todas tienen su razón.

| Caso | Java | MiniJava | Por qué |
|---|---|---|---|
| `\uXXXX` fuera de un literal | se traduce **antes** de tokenizar, en todo el archivo (JLS §3.3), así que un `\u12G4` en un comentario no compila | el escape sólo significa algo dentro de un `charLiteral` o un `litString`; en un comentario es texto | implementar el pre-lexer de Java es una fase entera aparte, y la cátedra no la pide |
| `"""` | el escape se traduce primero y la comilla **cierra** el string | no cierra nada: son seis caracteres del lexema | consecuencia directa de lo anterior |
| `041` | literal **octal**, vale 33 | `intLiteral` decimal de 3 dígitos | la ER de la cátedra es decimal y nada más |
| `09` | `illegal digit in an octal literal` | `intLiteral` válido de 2 dígitos | ídem |
| `0x1F`, `0b1010` | literal hexadecimal / binario | `intLiteral(0)` + `idMV` | ídem |
| `1_000` | válido (separadores desde Java 7) | `intLiteral(1)`, error por el `_`, `intLiteral(000)` | ídem |
| `0000000001` | `intLiteral` válido (el límite de Java es de **valor**) | **error**: el límite de MiniJava es de **dígitos** | la cátedra acota dígitos, no magnitud |
| `3.5e38f`, `1e999` | `floating-point number too large` | `floatLiteral` válido | el rango es un chequeo semántico, no léxico (§3.3) |
| `.5` | `floatLiteral` válido | `punto` + `intLiteral` | la consigna exige que un float empiece con dígito |
| `'\q'` | `illegal escape character` | `charLiteral` válido | permisividad explícita de la cátedra (§3.4) |
| `"a\q"` | `illegal escape character` | `litString` válido | §2.3.3 no define un conjunto de escapes: define qué comilla cierra (§3.5) |
| `"a\\"` | la barra invertida — la primera `\` escapa a la segunda | `litString sin cerrar` | §2.3.3 es una regla **posicional**: la comilla de cierre nunca está antecedida por `\`, y una barra no cancela a la anterior (§3.5) |
| `"a\u12G4"` | `illegal unicode escape`, y el literal se sigue leyendo | un error, y el literal se descarta hasta su comilla de cierre sin emitir token | criterio fijado por el profesor (§4.3) |
| Dígitos árabe-índicos (`١٢٣`) | no valen en un literal numérico | son un `intLiteral` | `Character.isDigit` los acepta; restringir a ASCII contradiría el logro Unicodes |
| Dígitos no ASCII en `\uXXXX` | inválidos | aceptados | `Character.digit(c,16)` los acepta |
| `中` al empezar un identificador | válido | error — aunque **sí** puede continuar un identificador | la ER de la cátedra dice "letra mayúscula" / "letra minúscula", y `中` no es ninguna de las dos |
| Form feed y tabulador vertical | son blancos | son errores léxicos | no están en la lista de separadores de la cátedra |
| BOM al inicio del archivo | se descarta | es un carácter desconocido | ídem |

Y una coincidencia que también vale documentar, porque costó trabajo verificarla: **la columna a la
que apunta el `^` coincide con `javac` en los nueve diagnósticos comparables** (§4.4).

## 8. Ver también

- [`arquitectura.md`](arquitectura.md) — la visión de conjunto.
- [`especificacion-lexica.md`](especificacion-lexica.md) — las ER exactas que salen de estas decisiones.
- [`manejo-de-errores.md`](manejo-de-errores.md) — el desarrollo completo de las decisiones de §4.
- [`manejador-de-archivo.md`](manejador-de-archivo.md) — la decisión de cargar el fuente completo.
- [`testing.md`](testing.md) — cómo cada decisión quedó fijada por un test.
