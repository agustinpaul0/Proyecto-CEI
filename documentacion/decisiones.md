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
numéricos, escapes Unicode) es lleno de sorpresas, y una suposición razonable resulta equivocada con
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

### 2.4. Las palabras clave se resuelven con una tabla, no con estados

**Fuente**: cátedra (Clase 02, slides 62-63), que recomienda explícitamente esta técnica.

**Decisión**: se reconoce un identificador común y, antes de emitir el token, se consulta
`TablaDePalabrasClave`. Si el lexema coincide con una de las 21 palabras reservadas, se emite esa
palabra clave en vez del identificador.

**Descartado**: un autómata dedicado por palabra. Requeriría casi un estado por letra de cada
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

### 3.5. Escapes en `litString`: sólo los válidos de Java

**Fuente**: JLS §3.10.7, verificado contra la especificación.

**El razonamiento**, que es el caso de manual de la Regla 2: a diferencia de `charLiteral`, la
cátedra **no** autoriza "cualquier carácter" para `litString` — §2.3.3 sólo cita a Java para el caso
puntual de `\"`. Ante esa falta de especificación se aplica "ante la duda, como en Java", y el
conjunto de escapes válidos es el del JLS: `b s t n f r " ' \` o un dígito octal `0`-`7`, más el
escape Unicode.

**Consecuencia**: `"a\q"` es un error léxico, mientras que `'\q'` es válido. La asimetría es
intencional y viene de que la cátedra especificó una de las dos y no la otra.

### 3.6. La barra invertida consume el carácter siguiente dentro de un string

**Fuente**: semántica estándar de Java (Regla 2).

**El conflicto**: la lectura literal de la consigna ("la comilla final nunca estará antecedida por
`\`") daría que `"a\\"` es un string sin cerrar. Con la semántica de Java, la primera barra escapa a
la segunda y la comilla cierra correctamente.

**Decisión**: gana la regla "ante la duda, como en Java". `"a\\"` es un `litString` válido.

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

**El bug que esto arregló**: la implementación anterior descartaba siempre el carácter que estaba
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

**Decisión**: al primer escape mal formado dentro de un `litString` se reporta el error y se
**descarta lo que queda del literal hasta su comilla de cierre**, sin emitir token. Vale tanto para
un escape inválido (`"a\q`, `"a\8`) como para un escape Unicode sin sus cuatro dígitos
hexadecimales (`"a\u12G4"`): son el mismo defecto y tienen la misma recuperación.

**Qué reemplaza**: la primera versión copiaba a `javac` — reportaba el escape como error **no fatal**
y seguía leyendo el literal hasta emitirlo entero como token. Con `"a\q b\z c"` eso imprimía dos
errores y después el `litString` completo. El profesor consideró esa salida incorrecta: el compilador
afirma haber reconocido un literal cuyo contenido no supo interpretar, y el ruido crece con cada
escape roto.

**Por qué el criterio nuevo se sostiene**, más allá de la indicación: es **recuperación en modo
pánico con token de sincronización**, la técnica clásica ante un error de análisis — se descarta
entrada hasta un delimitador que devuelve al analizador a un estado conocido. El delimitador natural
de un `litString` es su comilla de cierre.

**Descartado**: reportar el primer escape y dejar que la recuperación estructural de §4.2 siga su
curso, sin descartar nada. Sobre `"a\q b\z c" + v1` eso produce `idMV(b)`, un error por la barra
suelta, `idMV(z)`, `idMV(c)`, y —lo decisivo— la comilla de **cierre** abre un literal nuevo que se
come ` + v1` y muere sin cerrar: tres tokens fantasma, dos errores inventados, y dos tokens legítimos
(`+` y `v1`) perdidos. Descartar hasta el cierre los conserva.

**Consecuencia sobre §4.2**: es la única excepción a "se consume exactamente lo que se reporta". El
lexema reportado sigue siendo el armado hasta el error (`"a\q`, como exige §4.1); lo descartado es
recuperación y no aparece en ningún lexema. La excepción está documentada y verificada en la
propiedad de reconstrucción del fuente.

**Qué queda afuera**: dentro de un mismo literal sólo se reporta el primer error, porque el segundo
es un defecto derivado de un literal que ya se descartó. Entre literales distintos la multi-detección
sigue intacta: `"a\q" "b\z"` reporta dos errores. En `charLiteral` la decisión no aplica — `'\x'`
con cualquier `x` es válido por permiso explícito de la cátedra (§3.4), y un escape Unicode mal
formado ahí conserva la recuperación estructural, porque no hay literal delimitado que descartar.

**Divergencia con Java, asumida**: `javac` sobre `"a\q b\z c"` da dos `illegal escape character` y
ningún `unclosed string literal` — sigue leyendo el literal. MiniJava da un solo error y no emite el
literal. Está en la tabla de §7.

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

Es una regla explícita del proyecto, no una aspiración. Todo código nuevo lleva tests; todo bug
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
| `"a\q b\z c"` | dos `illegal escape character`, y el literal se sigue leyendo | **un** error, y el literal se descarta hasta su comilla de cierre sin emitir token | criterio fijado por el profesor (§4.3) |
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
