# Decisiones de lógica del Analizador Léxico

Qué lenguaje reconoce el compilador exactamente: cada caso donde la especificación
admitía más de una lectura, cuál se eligió y por qué. Incluye los puntos donde
MiniJava diverge de Java a propósito.

> Estas decisiones son de la **Etapa 1 (Analizador Léxico)**. Las de la Etapa 2 están en
> [`../sintactico/`](../sintactico/README.md).

## 3. Decisiones sobre el lenguaje reconocido

### 3.1. `float` es palabra reservada; `double` no

**Fuente**: consulta directa al profesor. `float` **no figura en ningún PDF** de la cátedra — se
buscó explícitamente — y se agrega por confirmación del docente.

**Lectura descartada**: interpretar "tratemos `float` y `double` como lo mismo" como "ambas
palabras son reservadas". Lo que significa es que MiniJava tiene **un solo** tipo de dato de punto
flotante, así que `double` es un identificador común (`idMV`). El único lugar donde el concepto
aparece en la sintaxis son los sufijos `d`/`D` de un literal float, heredados de Java.

### 3.2. `floatLiteral` — la gramática completa se derivó de una sola frase

**Fuente**: Pautas Etapa 1: *"Estos deben tener la misma estructura que los floats de Java, con la
salvedad que siempre deben comenzar con un dígito"*. `Aspectos Lexicos MiniJava 2026.pdf` **no
menciona floats en absoluto** — no están entre sus "cinco tipos de literales". Los floats son
puramente un logro.

**Decisión**: se adopta la gramática de Java (parte entera, parte fraccionaria, exponente con signo
opcional, sufijo `f`/`F`/`d`/`D`) con la única restricción de que siempre empiece con dígito. La ER
completa está en [`especificacion-lexica-de-minijava.md`](especificacion-lexica-de-minijava.md) §3.4.

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
no corresponde a la Etapa 1. Fijado en `resources/lexico/sinErrores/floatFueraDelRangoDeJava.java`, donde
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
[`especificacion-lexica-de-minijava.md`](especificacion-lexica-de-minijava.md) §3.2.

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

Fijado en `resources/lexico/conErrores/tresComillasSimples.java`, que además ejercita la cascada: sobre
`'''`, las dos primeras comillas dan el error de char vacío y la tercera abre un `charLiteral` que
nunca cierra.

### 3.9. El escape Unicode admite el marcador `u` repetido

**Fuente**: JLS §3.3, verificado contra la especificación.

**Decisión**: `\u0041`, `\uu0041` y `\uuuu0041` son todos escapes válidos y equivalentes. La ER real
es `\u+[0-9a-fA-F]{4}` (una o más `u`), no `\u[0-9a-fA-F]{4}`.

Es un detalle que se descubrió leyendo la especificación y que uno no adivinaría: existe porque en
Java el pre-procesador de escapes Unicode debe poder representar un `\u` literal sin re-traducirlo.

### 3.10. El logro *Unicodes* es el escape, no los caracteres crudos

**Fuente**: criterio propio.

El logro *Unicodes* cubre dos cosas: caracteres no-ASCII crudos dentro de literales (`'ñ'`,
`"letra ü"`) y la secuencia escapada `\uXXXX`. Lo que el logro **agrega** es sólo la segunda: los
crudos se aceptan por la ER misma, que admite cualquier carácter no reservado.

**Por qué**: distinguirlos exigiría meter clasificadores "sólo ASCII" en cada
estado del autómata, ensuciando todo el léxico para demostrar algo que el escape ya demuestra por sí
solo.

### 3.11. Maximal munch, sin backtracking

**Fuente**: técnica estándar, coincide con el lexer de Java.

**Decisión**: se arma el lexema más largo posible y, cuando ya no se puede extender, se emite el
token. Consecuencias verificadas:

- `1abc` → dos tokens: `intLiteral(1)` + `idMV(abc)`. No es un error.
- `1e` → **error**, sin retroceder a emitir `intLiteral(1)` + `idMV(e)`. Una vez que se consumió la
  `e` como inicio de exponente, no se vuelve atrás.


## 7. Dónde MiniJava diverge de Java, a propósito

Ninguna de estas divergencias es un descuido: todas están cubiertas por casos de test escritos a
propósito, y todas tienen su razón.

| Caso | Java | MiniJava | Por qué |
|---|---|---|---|
| `\uXXXX` fuera de un literal | se traduce **antes** de tokenizar, en todo el archivo (JLS §3.3), así que un `\u12G4` en un comentario no compila | el escape sólo significa algo dentro de un `charLiteral` o un `litString`; en un comentario es texto | implementar el pre-lexer de Java es una fase entera aparte, y la cátedra no la pide |
| `"\u0022"` | el escape se traduce primero y la comilla **cierra** el string | no cierra nada: son seis caracteres del lexema | consecuencia directa de lo anterior |
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
| `"a\u12G4"` | `illegal unicode escape`, y el literal se sigue leyendo | un error, y el literal se descarta hasta su comilla de cierre sin emitir token | criterio fijado por el profesor ([`decisiones-de-deteccion-de-errores.md`](decisiones-de-deteccion-de-errores.md) §4.3) |
| Dígitos árabe-índicos (`١٢٣`) | no valen en un literal numérico | son un `intLiteral` | `Character.isDigit` los acepta; restringir a ASCII contradiría el logro Unicodes |
| Dígitos no ASCII en `\uXXXX` | inválidos | aceptados | `Character.digit(c,16)` los acepta |
| `中` al empezar un identificador | válido | error — aunque **sí** puede continuar un identificador | la ER de la cátedra dice "letra mayúscula" / "letra minúscula", y `中` no es ninguna de las dos |
| Form feed y tabulador vertical | son blancos | son errores léxicos | no están en la lista de separadores de la cátedra |
| BOM al inicio del archivo | se descarta | es un carácter desconocido | ídem |

Y una coincidencia que también vale documentar, porque costó trabajo verificarla: **la columna a la
que apunta el `^` coincide con `javac` en los nueve diagnósticos comparables**
([`decisiones-de-deteccion-de-errores.md`](decisiones-de-deteccion-de-errores.md) §4.4).

