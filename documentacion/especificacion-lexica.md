# Especificación léxica formal

Documento autocontenido: la expresión regular exacta de **todos** los tokens de MiniJava, sin omitir
ninguno, con sus autómatas y con lo que queda deliberadamente afuera del lenguaje.

Notación usada en todo el documento:

```text
Digito     = [0-9]
LetraMayus = [A-Z]
LetraMin   = [a-z]
Letra      = LetraMayus | LetraMin
Hex        = [0-9a-fA-F]
```

Un apunte sobre "letra" y "dígito": el proyecto los reconoce con `Character.isLetter` y
`Character.isDigit` de Java, que abarcan más que el ASCII. Las consecuencias de esa elección —y por
qué se tomó— están en [`decisiones.md`](decisiones.md) §7.

## 1. Los 56 tokens, de un vistazo

| Familia | Cantidad | Tokens |
|---|---|---|
| Palabras clave | 21 | `pr_class`, `pr_extends`, `pr_interface`, `pr_implements`, `pr_static`, `pr_boolean`, `pr_char`, `pr_int`, `pr_void`, `pr_public`, `pr_if`, `pr_else`, `pr_while`, `pr_return`, `pr_var`, `pr_this`, `pr_new`, `pr_null`, `pr_true`, `pr_false`, `pr_float` |
| Identificadores | 3 | `idClase`, `idGen`, `idMV` |
| Literales | 4 | `intLiteral`, `charLiteral`, `litString`, `floatLiteral` |
| Puntuación | 10 | `parenA`, `parenC`, `llaveA`, `llaveC`, `corcheteA`, `corcheteC`, `puntoYComa`, `coma`, `punto`, `dosPuntos` |
| Operadores | 17 | `op_mayor`, `op_menor`, `op_negacion`, `op_asignacion`, `op_igual`, `op_mayorIgual`, `op_menorIgual`, `op_distinto`, `op_and`, `op_or`, `op_modulo`, `op+`, `op-`, `op_mult`, `op_div`, `op_incremento`, `op_decremento` |
| Fin de archivo | 1 | `EOF` |

Los nombres de la columna derecha son literales: son exactamente los que se imprimen en la salida,
porque el formato que fija la cátedra es `(NombreToken,Lexema,NroLinea)`.

## 2. Identificadores

| Token | Expresión regular | Ejemplos | Contraejemplos |
|---|---|---|---|
| `idGen` | `LetraMayus` | `A`, `T` | `AB` (es `idClase`) |
| `idClase` | `LetraMayus (Letra \| Digito \| _)+` | `Alpha`, `A1`, `Mi_Clase` | `A` (es `idGen`) |
| `idMV` | `LetraMin (Letra \| Digito \| _)*` | `v1`, `x`, `mi_metodo` | `_x` (empieza con guion bajo) |

`idGen` (el parámetro de tipo genérico, la `T` de `List<T>`) y `idClase` comparten el primer
carácter, así que se distinguen por longitud: **una** mayúscula sola es `idGen`; una mayúscula
seguida de al menos un carácter más es `idClase`. El autómata lo resuelve con un estado intermedio,
`EstadoIdGenOClase`, que es terminal —si el token termina ahí, es `idGen`— y que transiciona a
`EstadoIdClase` en cuanto aparece un carácter más.

```mermaid
stateDiagram-v2
    [*] --> IdGenOClase: LetraMayus
    IdGenOClase --> IdClase: Letra, Digito o _
    IdClase --> IdClase: Letra, Digito o _
    IdGenOClase --> [*]: Emitir idGen
    IdClase --> [*]: Emitir idClase
```

Un identificador que empieza con minúscula es siempre `idMV`, **salvo** que su lexema completo
coincida con una de las 21 palabras clave: en ese caso se emite la palabra clave. Ese chequeo es una
consulta a una tabla, no un autómata — ver §7.

## 3. Literales

### 3.1. `intLiteral`

```text
intLiteral = Digito Digito? Digito? Digito? Digito? Digito? Digito? Digito? Digito?
```

Uno a nueve dígitos. Equivale a la notación de repetición acotada `Digito{1,9}`.

**El límite se valida después, no durante.** El estado consume la corrida **completa** de dígitos y
recién al terminar comprueba que no haya más de nueve. La razón es que si el carácter siguiente a la
corrida es un `.` o una `e`/`E`, la corrida continúa hacia un `floatLiteral` — que **no** tiene ese
límite —, y cortar en el noveno dígito rompería esa continuación. Por eso `1234567890.5` es un float
válido de diez dígitos en la parte entera, mientras que `1234567890` solo es un error.

### 3.2. `charLiteral`

```text
charLiteral = ' X '  |  ' \ Y '  |  ' \ U '

X = cualquier carácter salvo \ y '
Y = cualquier carácter salvo u
U = escape Unicode (ver §3.5)
```

La cadena vacía (`''`) **no** está en ninguna alternativa: es un error. Y `'\q'` **sí** es válido,
con valor `q` — es una permisividad explícita de la cátedra, más laxa que Java.

```mermaid
stateDiagram-v2
    [*] --> Abierto: '
    Abierto --> Cerrando: cualquier carácter ≠ \ y ≠ '
    Abierto --> Escapado: \
    Escapado --> Cerrando: cualquier carácter ≠ u
    Escapado --> MarcadorUnicode: u
    MarcadorUnicode --> MarcadorUnicode: u
    MarcadorUnicode --> EscapeUnicode: Hex
    EscapeUnicode --> Cerrando: (4 dígitos hex en total)
    Cerrando --> [*]: ' Emitir charLiteral
```

Los tres caminos que **no** llegan a `[*]` son los tres errores del charLiteral: `''` (char vacío),
un fin de línea o de archivo antes de cerrar (`sin cerrar`), y un carácter cualquiera donde debía ir
la comilla de cierre (`mal formado`).

### 3.3. `litString`

```text
litString = " ( Z | \ C )* "     con la " de cierre NO antecedida por \

Z = cualquier carácter salvo \, " y salto de línea
C = cualquier carácter salvo salto de línea  (incluye \ y ")
U = escape Unicode (ver §3.5), que se superpone a la regla anterior
```

**El `litString` se especifica distinto que el `charLiteral`, y por eso su ER tiene otra forma.**
`Aspectos Lexicos MiniJava 2026.pdf` §2.3.2 le da al `charLiteral` una **producción con dos
alternativas** (`'x'` y `'\y'`, con `x` e `y` independientes): es una regla de **apareamiento**, la
barra y el carácter siguiente forman una unidad. §2.3.3, en cambio, no da ninguna producción para el
string; da una regla **posicional**:

> *"es posible que en la secuencia de caracteres aparezca una comilla doble siempre y cuando se la
> anteceda por el caracter `\`. Por esto último la comilla doble que marca el final de un string
> correcto nunca estará antecedida por ese caracter."*

O sea: un lookbehind de un carácter sobre el texto crudo. **No existe un conjunto de escapes
válidos** dentro de un `litString` — `"hola \q"` es un string común y corriente, con una barra y una
`q` como dos caracteres cualesquiera.

De que la regla sea posicional y no de apareamiento salen unas cuantas consecuencias que conviene
tener a mano:

| Fuente | Resultado | Por qué |
|---|---|---|
| `"hola \q"` | `litString` válido | la `"` de cierre no está antecedida por `\` |
| `"di \"hola\" chau"` | **un solo** `litString` | las dos `"` internas están antecedidas por `\` |
| `"a\\"` | `litString sin cerrar` | la `"` final está antecedida por `\`, así que no cierra |
| `"a\\\""` | `litString` válido | la primera `"` no cierra, la segunda sí |
| `"barra \\ al medio"` | `litString` válido | la barra sólo importa pegada a la comilla de cierre |

La tercera fila es el único punto donde MiniJava **diverge de Java**, donde `"a\\"` es la barra
invertida: acá una barra no "cancela" a la anterior, porque la regla no habla de pares sino de qué
carácter antecede a la comilla. Está en [`decisiones.md`](decisiones.md) §7.

Un `litString` **no puede contener un salto de línea real**: si aparece uno antes de la comilla de
cierre, el literal quedó sin cerrar y es error.

El **escape Unicode** se superpone a esa regla y es lo único que se valida dentro del literal (es un
logro, §3.5). Un `\u` sin sus cuatro dígitos hexadecimales aborta el literal: se reporta y se
descarta lo que queda **hasta la comilla de cierre**, sin emitir token. El detalle está en
[`manejo-de-errores.md`](manejo-de-errores.md) §2.1.

El autómata implementa el lookbehind con dos estados, que es exactamente el bit de memoria que la
regla necesita:

```mermaid
stateDiagram-v2
    [*] --> Abierto: "
    Abierto --> Abierto: carácter ≠ \ , " y salto
    Abierto --> TrasBarra: \
    TrasBarra --> TrasBarra: \
    TrasBarra --> Abierto: cualquier otro carácter, incluida "
    TrasBarra --> MarcadorUnicode: u
    MarcadorUnicode --> Abierto: (4 dígitos hex)
    Abierto --> [*]: " Emitir litString
```

### 3.4. `floatLiteral`

```text
floatLiteral = Digito+ . Digito* Exponente? Sufijo?
             | Digito+ Exponente Sufijo?
             | Digito+ Sufijo

Exponente = (e|E) (+|-)? Digito+
Sufijo    = f | F | d | D
```

Igual que los floats de Java, con **una** salvedad que fija la consigna: siempre empiezan con un
dígito. Por eso `.5` no es un float en MiniJava — es el token `punto` seguido de `intLiteral(5)`.

Ninguna de las tres partes tiene límite de dígitos, a diferencia del `intLiteral`. El desarrollo de
por qué, con la verificación contra `javac`, está en [`decisiones.md`](decisiones.md) §3.3.

Dos casos que valen como error, sin retroceso: `1e` y `1.5e+`, un exponente anunciado y sin dígitos.
No hay backtracking a un token más corto — una vez consumida la `e` como inicio de exponente, el
literal está comprometido.

### 3.5. El escape Unicode

```text
U = \ u+ Hex Hex Hex Hex
```

El marcador `u` se repite **una o más veces**: `\u0041`, `\uu0041` y `\uuuu0041` son todos válidos y
equivalentes. Es semántica heredada tal cual de Java (JLS §3.3) y no se adivinó: se verificó contra
la especificación. Existe porque el pre-procesador de escapes de Java necesita poder representar un
`\u` literal sin re-traducirlo.

`EstadoMarcadorUnicode` consume la corrida de `u`; `EstadoEscapeUnicode` exige exactamente cuatro
dígitos hexadecimales después de la última. Ambos estados saben **a qué estado volver** al terminar,
porque el mismo escape sirve dentro de un `charLiteral` y dentro de un `litString`.

## 4. Puntuación

| Token | ER | Token | ER | Token | ER | Token | ER |
|---|---|---|---|---|---|---|---|
| `parenA` | `(` | `parenC` | `)` | `llaveA` | `{` | `llaveC` | `}` |
| `corcheteA` | `[` | `corcheteC` | `]` | `puntoYComa` | `;` | `coma` | `,` |
| `punto` | `.` | `dosPuntos` | `:` | | | | |

Los diez se reconocen con una tabla `Map<Character, TipoDeToken>` consultada desde el estado inicial:
son tokens de un solo carácter, sin lookahead, y no justifican un estado propio.

## 5. Operadores

| Token | ER | Token | ER |
|---|---|---|---|
| `op_mayor` | `>` | `op_mayorIgual` | `>=` |
| `op_menor` | `<` | `op_menorIgual` | `<=` |
| `op_negacion` | `!` | `op_distinto` | `!=` |
| `op_asignacion` | `=` | `op_igual` | `==` |
| `op+` | `+` | `op_incremento` | `++` |
| `op-` | `-` | `op_decremento` | `--` |
| `op_mult` | `*` | `op_div` | `/` |
| `op_modulo` | `%` | | |
| `op_and` | `&&` | `op_or` | `\|\|` |

Se dividen en tres grupos por su comportamiento en el autómata:

**Con sufijo opcional** (`>`, `<`, `!`, `=`, `+`, `-`): el primer carácter ya forma un token válido,
y el segundo lo extiende. El estado que los reconoce es **terminal**: si el sufijo no viene, emite el
token corto sin consumir el carácter que miró. Los seis los cubre un único estado parametrizado.

**Con segundo carácter obligatorio** (`&&`, `||`): `&` y `|` sueltos **no** son tokens válidos — no
están en la lista de operadores de la cátedra. El estado que los reconoce es **no terminal**: si el
segundo carácter no viene, es un error léxico.

**De un solo carácter** (`*`, `%`, y `/` cuando no abre un comentario): se emiten directamente.

## 6. Lo que no es un token

### 6.1. Separadores

Espacio, tabulador y salto de línea. Se descartan sin agregarse a ningún lexema.

El form feed (`\f`) y el tabulador vertical (`\v`) **no** son separadores en MiniJava, aunque sí lo
son en Java: no están en la lista de la cátedra. Producen error léxico.

### 6.2. Comentarios

De línea (`// ... hasta el fin de línea`) y multilínea (`/* ... */`). Se descartan por completo: no
producen token y su contenido no forma parte de ningún lexema.

Un comentario multilínea **sin cerrar** es un error léxico, y se reporta con el lexema `/*` y la
línea de **apertura** del comentario, no la del fin de archivo donde se descubrió.

La ER del comentario multilínea es exacta y estándar — la misma que usan `lex`/`flex` para los
comentarios de C:

```text
/\*  ( [^*] | \*+ [^*/] )*  \*+  /
```

En palabras: tras el `/*`, el contenido es cualquier cantidad de (un carácter que no sea `*`) o (uno
o más `*` seguidos de un carácter que no sea ni `*` ni `/`), y cierra con uno o más `*` seguidos de
`/`. El autómata equivalente tiene exactamente dos estados relevantes dentro del comentario:

```mermaid
stateDiagram-v2
    [*] --> SinAsterisco: tras "/*"
    SinAsterisco --> SinAsterisco: carácter ≠ *
    SinAsterisco --> ConAsterisco: *
    ConAsterisco --> ConAsterisco: *
    ConAsterisco --> SinAsterisco: carácter ≠ * y ≠ /
    ConAsterisco --> [*]: / cierra el comentario
```

## 7. El autómata delimita el lexema, la tabla lo nombra

Los 56 tokens se parten en dos grupos según de dónde sale su lexema:

| Grupo | Cuántos | Cuáles |
|---|---|---|
| **Lexema fijo** — el tipo y el lexema se determinan mutuamente | 49 | las 21 palabras clave, los 10 símbolos de puntuación, los 17 operadores y `EOF` |
| **Lexema variable** — se lee del fuente | 7 | `idClase`, `idGen`, `idMV`, `intLiteral`, `charLiteral`, `litString`, `floatLiteral` |

Los 49 del primer grupo no necesitan un estado que lleve su tipo encima: alcanza con que el autómata
delimite el lexema y con consultar una tabla. `TablaDeLexemas` es esa tabla, y expone las dos
direcciones — lexema → tipo, para emitir; tipo → lexema, para el `$` del `EOF`, que no se lee del
fuente.

**Lo único que la tabla no puede resolver es el *maximal munch*.** No sabe si un `>` es `op_mayor` o
el principio de un `op_mayorIgual`: eso lo decide el autómata mirando el carácter siguiente, y recién
cuando terminó de consumir consulta la tabla. El reparto es nítido: **el autómata delimita, la tabla
nombra.**

Es exactamente la técnica que la cátedra recomienda para las palabras clave (Clase 02, slides 62-63),
generalizada a los 49. Para las 21 reservadas el mecanismo es el mismo de siempre: se reconocen como
un `idMV` común y, antes de emitir, se consulta la tabla; si el lexema coincide, se emite la palabra
clave. Modelarlas con estados dedicados exigiría casi un estado por letra de cada palabra, fusionados
con los estados de identificador — decenas de estados frágiles para algo que una tabla resuelve en
tiempo constante.

**`float` está en la lista aunque no figure en ningún PDF de la cátedra**: se agrega por confirmación
directa del profesor. **`double` no está**: MiniJava tiene un único tipo de punto flotante, pero eso
no convierte a la palabra `double` en reservada — sigue siendo un `idMV` común. Ver
[`decisiones.md`](decisiones.md) §3.1.

## 8. El token `EOF`

Es un token más, con lexema `$` — literal de Aspectos Léxicos §2.5: *"El End of File es un token mas,
su y lexema es `$`"*. Su ER es trivial: un solo símbolo sobre la condición de fin de contenido.

**El `$` y el centinela son dos cosas distintas**, y conviene separarlas porque el nombre las
confunde:

- El **centinela** es `SourceManager.END_OF_FILE = (char) 26`, valor que fija la interfaz provista.
  Es el valor que `getNextChar()` devuelve cuando ya no queda contenido.
- El **`$`** es el lexema que se imprime para el token `EOF`. No se lee del fuente y no tiene ninguna
  relación con el centinela. Un `$` **escrito en el fuente** es un símbolo inválido y produce un error
  léxico, por §3.3 (*"cualquier otra entrada que no comforme con las reglas anteriormente presentadas
  debe generar un error"*).

De ahí una decisión del manejador de archivo: **el fin de archivo son dos condiciones unidas por un
`AND`, no una sola.** El centinela sigue siendo la señal, pero recibirlo es condición *necesaria y no
suficiente*, porque `0x1A` es un byte legítimo dentro de un archivo —en un binario aparece a menudo—.
La segunda condición la aporta `estaEnFinDeArchivo()`, que confirma que la posición de lectura se
pasó del contenido:

```text
fin de archivo  ⇔  llegó el centinela  ∧  la posición confirma que se acabó el contenido
```

Si llega el centinela pero la posición dice que todavía queda archivo, entonces **no era** un fin de
archivo: es un carácter del fuente, y se reporta como símbolo inválido. Sin esa segunda condición, un
`0x1A` embebido cortaría el análisis ahí en silencio, informando `[SinErrores]` sobre un fuente que
nunca se terminó de leer.

En el código se testea un solo conjunto porque el segundo implica al primero: el manejador devuelve
el centinela únicamente en la misma rama en que prende la bandera. El `AND` queda implícito. El
detalle está en [`manejador-de-archivo.md`](manejador-de-archivo.md) §2.4.

```mermaid
stateDiagram-v2
    [*] --> e0
    e0 --> eFinal: centinela (no consume)
    eFinal --> [*]: Emitir(EOF, "$")
```

Lo único distinto respecto de cualquier otro token es un detalle del **driver**, no del lenguaje: esa
transición **no avanza la posición de lectura**. No tendría sentido "consumir" el fin de archivo, y
además el Módulo Principal podría pedir otro token después.

**La línea del EOF** es `cantidad de saltos de línea consumidos + 1`. Vale la pena enunciarla porque
es la regla que fija el resultado en los tres casos de ejemplo de la cátedra, incluido el archivo que
no termina en salto de línea — ver [`manejador-de-archivo.md`](manejador-de-archivo.md) §2.

## 9. Sobre la regularidad de los tres casos "especiales"

El EOF, el comentario multilínea y el límite de 9 dígitos parecen, a primera vista, escaparse de lo
que una expresión regular puede describir. Los tres son regulares, y vale la pena mostrar por qué:

- **El EOF** es una ER de un solo símbolo (§8). Lo especial es del driver, no del lenguaje.
- **El comentario multilínea** tiene la ER exacta de §6.2, que no es una aproximación.
- **El límite de 9 dígitos** es trivialmente regular: toda repetición **acotada** lo es. La ER es una
  cadena lineal de nueve estados terminales sin bucle, o equivalentemente `Digito{1,9}` — lo que el
  profesor describió como "una estrella de Kleene de aridad 1-9". Ojo con la diferencia: `Digito*`
  (Kleene **sin** cota) sería incorrecto acá, porque el límite de 9 es justamente lo que se está
  modelando.

La implementación usa, para ese último caso, una variante observacionalmente equivalente pero más
simple: un único estado con bucle que consume toda la corrida y valida el largo al final. Ver §3.1
para por qué esa forma, además, es la única que no rompe la continuación hacia un float.

## 10. Ver también

- [`arquitectura.md`](arquitectura.md) — dónde encaja el léxico en el conjunto.
- [`patron-state.md`](patron-state.md) — cómo cada uno de estos autómatas se convierte en código.
- [`decisiones.md`](decisiones.md) — de dónde sale cada regla de este documento.
- [`manejo-de-errores.md`](manejo-de-errores.md) — qué pasa cuando la entrada no matchea ninguna ER.
