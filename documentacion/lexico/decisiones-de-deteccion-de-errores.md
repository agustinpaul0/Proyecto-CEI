# Decisiones de detección de errores del Analizador Léxico

Qué se considera un error léxico, dónde exactamente se lo detecta, a qué posición
apunta el reporte y cómo se recupera el análisis. El mecanismo general —común a
todas las etapas— está en
[`../compartido/manejo-de-errores.md`](../compartido/manejo-de-errores.md).

> Estas decisiones son de la **Etapa 1 (Analizador Léxico)**. Las de la Etapa 2 están en
> [`../sintactico/`](../sintactico/README.md).

## 4. Decisiones sobre el manejo de errores

El desarrollo completo está en [`manejo-de-errores.md`](../compartido/manejo-de-errores.md); acá van las
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

### 4.4. Dónde apunta el `^`: criterio de `javac` con excepción para `charLiteral vacío`

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
| `''` | `empty character literal` | la comilla de **cierre** |
| `'a` / `'ab'` | `unclosed character literal` | la comilla de **apertura** |
| `"hola sin cerrar` | `unclosed string literal` | la comilla de **apertura** |
| `1234567890123` | `integer number too large` | el **primer** dígito |
| `1e` | `malformed floating-point literal` | el **primer** carácter |
| `/* sin cerrar` | `unclosed comment` | la barra de **apertura** |

Para `''`, el proyecto se aparta deliberadamente de `javac`: se señala la segunda comilla porque es
el carácter que confirma que el literal está vacío. En los restantes casos tiene sentido más allá de
imitar a Java: en `"hola sin cerrar` lo que hay que corregir es la
comilla de apertura (le falta el par), no la última letra que se alcanzó a leer.

Fijado caso por caso en `PosicionDelCaretTest`, con el diagnóstico de `javac` citado en cada caso
comparable y la excepción de `charLiteral vacío` documentada explícitamente.

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

**Alcance**: vale **sólo** para el lexema de un error y para la representación de la línea en el
detalle del reporte. En este último también se expanden los tabuladores, se escapan los controles,
formatos, marcas combinantes y espacios no convencionales, y se mide un par surrogate bien formado
como la figura visible completa. Si el error cae sobre una de sus unidades UTF-16, el par se
representa excepcionalmente como `\udddd\udddd` para que el `^` señale la unidad exacta sin quedar
dentro del emoji. El lexema de un token se imprime crudo, porque
el contrato de la cátedra es que sea el texto exacto del fuente.

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
las suites `AnalizadorLexico*Test`.

