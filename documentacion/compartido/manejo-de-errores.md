# Manejo de errores léxicos

Documento autocontenido: cómo se detecta, cómo se recupera y cómo se reporta un error léxico. Es el
desarrollo completo de dos de los ocho logros de la Etapa 1 —*Multi-detección de Errores Léxicos* y
*Reporte de Error Elegante*— y de los tres defectos reales que hubo que corregir para que funcionaran
bien. Para la visión de conjunto, ver [`arquitectura.md`](../lexico/arquitectura-del-analizador-lexico.md).

## 1. Cuándo y cómo se detecta un error

Un estado detecta un error léxico cuando, mirando el carácter actual, ninguna transición válida
existe desde ese estado (por ejemplo: `EstadoInicial` viendo un `#`, que no es letra, dígito, ni
ningún símbolo conocido). En ese momento se lanza `ExcepcionLexica`, que lleva: el tipo de error, el
lexema, la línea, la columna y el texto completo de la línea fuente (para el reporte elegante).

**Qué contiene el lexema**: siempre **el lexema armado hasta el momento del error**. No es una
decisión del proyecto, lo fija `Pautas Etapa 1.pdf` slide 24, y el código `[Error:Lexema|NroLinea]`
lleva ese mismo lexema. Qué caracteres alcanzaron a entrar en él se sigue del criterio de
recuperación de §2, y hay dos casos:

- Si el carácter ofensivo **es** el problema (un `#` suelto, un `''` vacío, un `\u` sin sus cuatro
  dígitos hexadecimales),
  el estado lo consume y por lo tanto queda incluido en el lexema, como manda la cátedra en Clase 02
  slide 43.
- Si el problema es el lexema **ya construido** (un `&` sin su par, un `intLiteral` de más de nueve
  dígitos, un exponente sin dígitos), el carácter que se está mirando es un token perfectamente
  legítimo que no tiene nada que ver con el error: no se consume ni se incluye. Reportarlo sería
engañoso y, además, hacía desaparecer ese token de la salida.

Dónde apunta la **columna** es una decisión aparte de qué contiene el lexema, y tiene su propia
sección (§4).

## 2. Recuperación: el criterio, en una línea

La consigna base de la cátedra dice: *"al detectar un error deberá reportarse y finalizar la
ejecución"*. El logro *Multi-detección de Errores* cambia ese comportamiento: en vez de terminar, el
Módulo Principal captura la excepción, la reporta y sigue pidiendo tokens. **La cátedra no dice nada
sobre CÓMO recuperarse** — su comportamiento base no tiene recuperación en absoluto — así que aplica
la regla que la propia cátedra escribió —"todo lo que NO esté especificado para MiniJava tiene
comportamiento por defecto igual al de Java"— resuelta como manda el procedimiento del proyecto:
verificando contra `javac`, no de memoria.

> **Todo estado que lanza un error consume del fuente exactamente los caracteres que reporta en el
> lexema, ni uno más** — salvo un escape Unicode mal formado dentro de un `litString`, que además
> descarta lo que queda del literal hasta su comilla de cierre (§2.1). Fuera de esa excepción no hay ningún paso
> de recuperación posterior: el siguiente `proximoToken()` arranca en el primer carácter que **no**
> formó parte del lexema reportado.

Esto coincide además, literalmente, con el código que propone la cátedra en Clase 02 slide 43:
`actualizarLexema()` y después `throw`, sin descartar nada.

**Invariante de terminación**: el lexema de un error nunca es vacío, así que cada error consumió al
menos un carácter y el análisis siempre avanza. Antes esto lo garantizaba un método
`recuperarDeError()` que descartaba un carácter; ese método ya no existe, la garantía es estructural.

### Qué se verificó de `javac`

| fuente | qué hace `javac` |
|---|---|
| `char c = 'abbc';` | dos errores `unclosed character literal` (apertura y cierre) y `bbc` tokenizado como identificador |
| `char d = 'a;` | un error en la comilla; **el `;` sobrevive** |
| `String s = "hola \q mundo";` | un error `illegal escape character`, y **sigue leyendo el literal hasta la comilla de cierre**; sin cascada. En MiniJava ese fuente ni siquiera es un error: §2.3.3 no define un conjunto de escapes válidos |
| `int c = 1234567890123 + 9;` | un error `integer number too large`; **el `+ 9` sobrevive** |

O sea: `javac` **no** sincroniza hasta el delimitador de cierre, y **nunca descarta un carácter que
no formó parte del lexema reportado**. Las dos cosas quedaron reflejadas en el criterio.

### El defecto que esto corrigió

La implementación anterior descartaba siempre el carácter que estaba mirando, incluso cuando ese
carácter nunca había formado parte del lexema ofensivo. Consecuencias reales, todas invisibles en el
corpus de entonces porque cada caso ponía un espacio o un fin de línea detrás del lexema malo:

| fuente | antes | ahora |
|---|---|---|
| `v1 &v2` | `[Error:&\|1]` + `(intLiteral,2,1)` — **se comía la `v`** | `[Error:&\|1]` + `(idMV,v2,1)` |
| `1e;` | `[Error:1e\|1]`, el `;` desaparecía | `[Error:1e\|1]` + `(puntoYComa,;,1)` |
| `1234567890+1` | el `+` desaparecía sin mensaje | `[Error:1234567890\|1]` + `(op+,+,1)` + `(intLiteral,1,1)` |
| `'ab'` | `[Error:'a\|1]` + `[Error:'\|1]`, la `b` desaparecía | `[Error:'a\|1]` + `(idMV,b,1)` + `[Error:'\|1]` |

La propiedad que lo cierra para cualquier entrada está en `PropiedadesDelReporteTest`: sobre un
fuente sin blancos ni comentarios, la concatenación de los lexemas de todos los tokens emitidos y de
todos los errores —más, en el único caso que resincroniza, el resto del `litString` descartado—
**reconstruye el fuente completo**. Que la excepción esté escrita ahí y no reusada del código de
producción es deliberado: si el léxico dejara de descartar exactamente lo que dice descartar, la
propiedad lo detectaría en vez de acompañarlo.

## 2.1. La única excepción: el escape Unicode mal formado dentro de un `litString`

Un escape Unicode sin sus cuatro dígitos hexadecimales dentro de un `litString` es el único error que
**no se limita a consumir lo que reporta**: después de reportarlo se descarta lo que queda del
literal **hasta su comilla de cierre**, sin emitir token.

```text
"a\u12G4" + v1
→ Error Léxico en línea 1, columna 7: escape unicode mal formado, se esperaban 4 dígitos hexadecimales: "a\u12
→ [Error:"a\u12|1]
→ (op+,+,1)
→ (idMV,v1,1)
→ (EOF,$,2)
```

Es **recuperación en modo pánico con token de sincronización**, la técnica clásica de construcción de
compiladores: ante un error se descarta entrada hasta un delimitador que devuelve al analizador a un
estado conocido. Acá el delimitador es la comilla de cierre, y el estado conocido es "fuera de todo
literal". El criterio lo fijó el profesor de la materia; su justificación completa, y la alternativa
que reemplaza, están en [`decisiones.md`](../lexico/decisiones-de-logica.md) §4.3.

Lo que se gana es que el análisis sigue **en el token siguiente al literal roto**, no en mitad de su
contenido. Sin resincronizar, el mismo fuente daría `idMV(G4)` y —lo peor— la comilla de **cierre**
abriría un literal nuevo que consumiría ` + v1` y terminaría sin cerrar: tokens espurios, errores
inventados y dos tokens legítimos perdidos, todo derivado de un único defecto real.

Dos detalles, que vienen de reglas distintas:

- El **lexema** es `"a\u12`, no el literal completo ni lo descartado: `Pautas Etapa 1.pdf` slide 24
  pide "el lexema armado hasta el momento del error". El descarte es recuperación, no parte de lo que
  se reporta, y por eso la excepción se construye **antes** de resincronizar.
- **No se emite token**: el literal quedó mal formado, y emitirlo sería afirmar que el programa
  contiene un `litString` que el compilador no pudo interpretar.

**Qué queda fuera del alcance.** Dentro de un mismo literal sólo se reporta un error, porque el
resto ya se descartó. Entre literales distintos la multi-detección sigue intacta:
`"a\u12G4" "b\u34Z6"` reporta dos errores, uno por literal. Y en `charLiteral` la resincronización
no aplica: no hay contenido de largo arbitrario que descartar, así que la recuperación es
estructural.

Si el literal nunca cierra, el descarte se detiene en el salto de línea: un `litString` no cruza
líneas, así que `"a\q` seguido de un fin de línea reporta su error y deja la línea siguiente intacta.

## 2.2. Los tipos de error

`TipoDeErrorLexico` tiene actualmente diez valores, uno por punto de falla, cada uno con su propia
razón. En una versión anterior se utilizaba una sola frase para todos los errores, por lo que el
reporte no podía decir qué construcción había fallado y los tests no podían validar por tipo, que es
la regla del proyecto (ver [`testing.md`](estrategia-de-testing.md) §1).

| Valor | Razón que imprime |
|---|---|
| `CARACTER_DESCONOCIDO` | `<lexema> no es un símbolo válido` — **texto literal de la slide 43** |
| `CHAR_VACIO` | `charLiteral vacío` |
| `CHAR_MAL_FORMADO` | `charLiteral mal formado, contiene más de un carácter` |
| `CHAR_SIN_CERRAR` | `charLiteral sin cerrar` |
| `STRING_SIN_CERRAR` | `litString sin cerrar` — incluye el caso de la barra pegada a la comilla de cierre (`"a\\"`), que por la regla posicional de Aspectos Léxicos §2.3.3 no cierra el literal |
| `UNICODE_MAL_FORMADO` | `escape unicode mal formado, se esperaban 4 dígitos hexadecimales` — el único que resincroniza, y sólo cuando ocurre dentro de un `litString` (§2.1) |
| `COMENTARIO_SIN_CERRAR` | `comentario multilínea sin cerrar` |
| `ENTERO_DEMASIADO_LARGO` | `intLiteral de más de 9 dígitos` |
| `OPERADOR_INCOMPLETO` | `operador incompleto, se esperaba && o \|\|` |
| `EXPONENTE_SIN_DIGITOS` | `exponente sin dígitos` |

La distinción entre *mal formado* y *sin cerrar* la decide el propio estado mirando el lookahead: si
lo interrumpió un fin de línea o un fin de archivo el literal nunca se cerró; si lo interrumpió un
carácter cualquiera, el literal tiene más de un carácter. Sólo cambia el encabezado legible — el
código `[Error:Lexema|NroLinea]` no cambia ni de formato ni de contenido.

## 3. Los dos formatos de reporte

Cuando hay error, se imprimen dos cosas:

1. Un **reporte legible** (logro *Reporte de Error Elegante*): la razón del error, la línea fuente
   completa, y un `^` apuntando a la columna que corresponde según el criterio de la §4. Ejemplo real
   (tomado de la consigna, fuente `v1 + # chau` en la línea 2):

   ```text
   Error Léxico en línea 2, columna 6: # no es un símbolo válido
   Detalle: v1 + # chau
                 ^
   [Error:#|2]
   ```

El detalle del reporte expande los tabuladores a tabulaciones de ocho columnas, representa los
caracteres de control, de formato, las marcas combinantes y los espacios no convencionales con una
forma visible, y calcula el `^` según el ancho visual del texto ya renderizado. Si el carácter
señalado no tiene una celda visible propia —por ejemplo, una marca combinante como U+20CC— se
muestra en notación `\uXXXX`, y el `^` se ubica sobre el inicio de esa representación. Los pares
surrogate bien formados se conservan como un único carácter visible; si el error cae sobre una de
sus unidades UTF-16, el par se muestra excepcionalmente como `\udddd\udddd` para que el `^` pueda
señalar la unidad exacta sin quedar dentro del emoji. La columna del encabezado sigue siendo la
columna lógica del fuente.

2. Un **código de error** de una sola línea, `[Error:Lexema|NroLinea]` — este es el contrato exacto
   que verifican los testers de la cátedra, y **no lleva columna** (la columna es sólo parte del
   reporte legible, que es un agregado propio más allá de lo pedido).

## 4. A qué posición apunta la columna

La cátedra no pide columna — sólo línea. La columna, y el `^` que la acompaña, son un agregado propio
del logro *Reporte de Error Elegante*. Como no está especificada, aplica la regla de "ante la duda,
como en Java", y el criterio se tomó de `javac` verificándolo contra el compilador real, un caso por
diagnóstico. La regla general es:

> Si el mensaje habla de un **carácter ofensivo puntual**, el `^` va a ese carácter. Si habla de la
> **construcción entera** que falló, el `^` va a donde esa construcción **empieza**.

| fuente | diagnóstico de `javac` | dónde cae el `^` |
|---|---|---|
| `v1 # v2` | `illegal character` | el `#` |
| `'\u12G4'` | `illegal unicode escape` | la `G` — no la barra ni la `u` |
| `''` | `empty character literal` | la comilla de cierre |
| `'a` / `'ab'` | `unclosed character literal` | la comilla de apertura |
| `"hola sin cerrar` | `unclosed string literal` | la comilla de apertura |
| `1234567890123` | `integer number too large` | el primer dígito |
| `1e` | `malformed floating-point literal` | el primer carácter del literal |
| `/* sin cerrar` | `unclosed comment` | la barra de apertura |

La única divergencia deliberada respecto de `javac` es `charLiteral vacío`: se señala la segunda
comilla, porque es el carácter que confirma que el literal está vacío. Tiene sentido más allá de
imitar a Java: en `"hola sin cerrar`, lo que hay que corregir es la comilla
de apertura (falta su par), no la última letra que se alcanzó a leer; en `1234567890`, el problema es
el número entero, y su primer dígito es donde empieza a leerse.

Cuántos errores reporta es una decisión aparte: en `"a\u12G4 b\u34Z6"` MiniJava señala la `G` igual que `javac`,
pero reporta un solo error y no dos, porque al primero descarta el literal entero (§2.1).

### Cómo se implementa

`BuilderDeLexema` —la pieza del contexto léxico que arma el lexema— mantiene **dos** fotos de
posición, ambas tomadas dentro de `avanzar()` justo antes de consumir —cuando línea, columna y línea-fuente todavía describen ese carácter— y ambas del tipo
`PosicionLexica`:

- `posicionPrimerConsumido`: el **primer** carácter del lexema en curso. Es la que usan los errores
  que hablan de la construcción entera, con el ancla `INICIO_DEL_LEXEMA`.
- `posicionUltimoConsumido`: el **último** carácter que quedó en el lexema. Es la que usan los
  errores cuyo carácter ofensivo ya fue consumido antes del `throw` —un símbolo desconocido o la
  segunda comilla de un `charLiteral vacío`, por ejemplo—, con el ancla `ULTIMO_CONSUMIDO`.

La distinción importa porque en el momento del `throw` el lookahead actual puede haber avanzado más
allá del lexema: puede ser un espacio, un EOF o la letra siguiente, sin ninguna relación con el
motivo del error. Reportar su columna producía un defecto real, encontrado al ejecutar la
suite desde IntelliJ: el `^` apuntaba a un carácter distinto del que decía el mensaje.

Un caso necesita más que las dos fotos: el **comentario multilínea sin cerrar**. `EstadoBarra`
descarta el `/*` del lexema para que el comentario no forme parte de ningún token, así que cuando el
error se lanza —posiblemente muchas líneas después, en el EOF— ya no queda rastro de dónde empezaba.
Por eso `EstadoBarra` fotografía la apertura y se la pasa a `EstadoComentarioMultilinea`, que la usa
entera al lanzar. Antes de eso, el mensaje decía la línea de apertura correcta pero mostraba la
línea-fuente y la columna del EOF, que puede caer varias líneas después en contenido no relacionado.

## 5. El `^` nunca puede colgar después del final de la línea impresa

Bug de la misma familia: **cualquier** estado que lanza la excepción cuando el lookahead es EOF o un
`\n` reporta una posición que ni siquiera aparece en la línea impresa, porque `getLineaFuente()`
corta el texto justo antes del salto de línea. El resultado visual: un `^` una posición más allá del
último carácter visible, apuntando al aire en vez de a un carácter concreto. Afectaba a casi todos los
estados que pueden toparse con un EOF o salto de línea inesperado (chars y strings sin cerrar,
escapes interrumpidos, exponentes sin dígitos). Se resolvió en un solo lugar:
`BuilderDeLexema.posicionDe(LOOKAHEAD)` usa
automáticamente `posicionUltimoConsumido()` cuando detecta que el lookahead es EOF o `\n`, sin que
cada estado individual tenga que saberlo.

La mayoría de aquellos estados ya no depende de esa protección: al adoptar el criterio de `javac` de
la §4 pasaron a señalar el inicio de la construcción, que por definición está dentro de la línea que
se imprime. La protección sigue viva para los que sí reportan el lookahead, y la invariante ("el `^`
siempre cae sobre un carácter de la línea mostrada") se verifica caso por caso en
`PosicionDelCaretTest` y sobre entradas generadas en `PropiedadesDelReporteTest`.

## 6. El lexema de un error nunca lleva un carácter de control crudo

El lexema de un error se imprime literal en el encabezado legible y dentro de
`[Error:Lexema|NroLinea]`. Un carácter de control ahí es, en el mejor de los casos, invisible; y si
es un salto de línea, **parte el código de error en dos líneas** y rompe el contrato de la cátedra de
un elemento por línea. Pasaba típicamente en cascadas: una comilla simple huérfana justo antes de un
salto de línea real abre un char literal que "termina" en ese salto.

La regla, aplicada en un solo lugar (`EscapadorDeNoImprimibles`, que la fábrica invoca al construir
la excepción): **todo carácter
invisible del lexema de un error se renderiza con su escape de Java** (`\n`, `\r`, `\t`, `\f`, `\b`, o
`\uXXXX` para el resto). Así el lexema reportado sigue siendo una descripción fiel de lo que se
consumió, y siempre es imprimible.

"Invisible" no son sólo los controles C0: también los caracteres de formato (el BOM entre ellos), los
separadores de espacio que no son el espacio común (el no rompible entre ellos) y los surrogados
sueltos, que `System.out` convertiría en un `?`. Sin eso, el encabezado de un espacio no rompible se
leía `Error Léxico en línea 1, columna 4:   no es un símbolo válido`. Es la misma convención de
`javac`, que para ese caso reporta literalmente `error: illegal character: '\u00a0'` (verificado
ejecutándolo). Un par surrogado bien formado —un emoji— se deja crudo: se ve perfecto y escaparlo
sólo haría el mensaje más difícil de leer.

Esto vale **sólo** para el lexema de un error y para la representación de la línea en el detalle.
El lexema de un **token** se imprime crudo, porque el contrato de la cátedra es que sea el texto
exacto del fuente — un tabulador dentro de un `litString` sale como tabulador (ver
`resources/sinErrores/stringConTabuladorInterior.java`).

Aparte de eso, los estados que siguen a una barra invertida (`EstadoCharEscapado`,
`EstadoStringTrasBarra`) rechazan explícitamente el salto de línea en vez de consumirlo: una barra al
final de una línea no escapa nada, deja el literal sin cerrar.

## 7. Dos entradas que no pueden hacer caer al compilador

Un fuente inválido tiene que producir una `ExcepcionLexica` controlada, nunca una falla de otra
naturaleza: en este proyecto cualquier otra excepción es un defecto (ver
[`testing.md`](estrategia-de-testing.md) §1). Dos entradas concretas obligan a tomar precauciones en la frontera de
entrada, y las dos están fijadas por `DefectosDeRobustezTest`:

- **El byte `0x1A`**: el centinela `SourceManager.END_OF_FILE` es `(char) 26`, que también es un byte
  perfectamente legítimo dentro de un archivo, así que recibirlo no alcanza para concluir que el
  fuente se terminó. El fin de archivo exige además que la posición de lectura lo confirme
  (`ManejadorDeArchivo.estaEnFinDeArchivo()`). Un `0x1A` embebido cumple la primera condición y no la
  segunda: no es fin de archivo, es un carácter desconocido más. Sin esa segunda condición el byte
  **truncaría el análisis en silencio** —el resto del archivo desaparecería y encima se imprimiría
  `[SinErrores]`—.
- **Bytes que no son UTF-8 válido**: `Files.readString` lanza `MalformedInputException`, que saldría
  como un `UncheckedIOException` sin atrapar. Se usa `new String(bytes, UTF_8)`, que sustituye las
  secuencias mal formadas por `U+FFFD`; los bytes residuales llegan al léxico y se reportan como errores
  léxicos normales.

## Ver también

- [`arquitectura.md`](../lexico/arquitectura-del-analizador-lexico.md) — visión completa del proyecto, ciclo de vida de ejecución.
- [`decisiones.md`](../lexico/decisiones-de-logica.md) §4 — cada decisión de este documento, con su fuente.
- [`testing.md`](estrategia-de-testing.md) — las invariantes que verifican todo lo de acá.
- [`logros.md`](../lexico/resolucion-de-logros.md) — los logros *Multi-detección de Errores* y *Reporte de Error Elegante*.
- [`especificacion-lexica.md`](../lexico/especificacion-lexica-de-minijava.md) — ER formal de cada token.

---

# Reglas operativas para quien implementa

Todo lo anterior explica **qué hace** el compilador y **por qué**. Lo que sigue son las **reglas
de trabajo** correspondientes: lo que hay que respetar al escribir o modificar código de esta
parte. Antes vivía en un archivo aparte; se unificó acá para que el tema tenga un solo lugar.

### El criterio de recuperación (leer primero)

**Todo estado que lanza un error consume del fuente exactamente los caracteres que reporta en el
lexema, ni uno más — con una única excepción, abajo.** No existe `recuperarDeError()`: la
recuperación es estructural, el siguiente `proximoToken()` arranca en el primer carácter que no formó
parte del lexema. Verificado contra `javac` (ver `documentacion/manejo-de-errores.md` §2), porque la
cátedra no dice nada sobre cómo recuperarse — su comportamiento base ni siquiera tiene recuperación.

**Regla para código nuevo**: si vas a agregar un `throw` en un estado, preguntate si el carácter que
estás mirando forma parte del error. Si sí, `avanzar()` y anclar en `ULTIMO_CONSUMIDO`. Si no (el
lexema ya construido es el que está mal), no lo toques. Dónde apunta el `^` en cada caso es una decisión aparte, y está más abajo.
`agregarCaracterOfensivo()` ya no existe: era justamente el método que rompía la correspondencia
entre lo que se reporta y lo que se consume, y por eso la recuperación se comía tokens válidos.

**La única excepción: el escape unicode mal formado dentro de un `litString`.** Ahí, además de
reportar, se descarta lo que queda del literal hasta su comilla de cierre (`RecuperacionDeLitString
.descartarHastaElCierre()`) y no se emite token. Es `UNICODE_MAL_FORMADO`, y sólo cuando ocurre
dentro de un `litString` — que es lo que le dice el flag `dentroDeLitString` que arrastran
`EstadoMarcadorUnicode` y `EstadoEscapeUnicode`. Desde que §2.3.3 se lee como regla posicional, una
barra seguida de cualquier otra cosa NO es un error, así que éste es el único error de contenido que
puede tener un litString. Criterio del **profesor**, no de `javac`; justificación completa en
`documentacion/decisiones.md` §4.3.

**Regla para código nuevo**: la excepción se construye **antes** de resincronizar. El lexema
reportado tiene que ser el armado hasta el error (`"a\u12`), no lo que se descartó después; y el
descarte usa `descartar()`, nunca `avanzar()`. Ya no existen errores no fatales: `registrarError()` y
`tomarErroresNoFatales()` fueron eliminados.

### El contrato de `ExcepcionLexica`

Cada `ExcepcionLexica` lleva cinco datos: tipo (`TipoDeErrorLexico`), lexema, línea, columna y la
línea fuente completa (para el Reporte de Error Elegante). Los tres datos de posición viajan juntos en una `PosicionLexica`, que es lo
que `ExcepcionLexica` guarda: mezclar la línea de un carácter con la línea-fuente de otro deja el
`^` señalando texto ajeno. `FabricaDeExcepcionesLexicas` es el punto único de construcción — ningún
estado arma una `ExcepcionLexica` a mano, siempre pasa por ahí, para que los campos sean
consistentes entre sí; el escapado de invisibles vive aparte, en
`errores/EscapadorDeNoImprimibles`.

**El lexema es el armado hasta el momento del error, siempre.** No es una elección nuestra: lo fija
`Pautas Etapa 1.pdf` slide 24 (*"Deberá reportarse un mensaje adecuado a la naturaleza del error
indicando el lexema armado hasta el momento del error y en qué número de línea del fuente MiniJava se
produjo"*), y el `[Error:Lexema|NroLinea]` tiene que llevar ese mismo lexema. De ahí que un escape
unicode roto en `"a\u12G4" + v1` reporte `"a\u12` y no el literal entero, aunque el estado haya
consumido hasta la comilla de cierre para resincronizar.

**Invariante de la cátedra** (`convenciones-catedra.md`): el código de contrato
`[Error:Lexema|NroLinea]` **no lleva columna** — la columna sólo aparece en el mensaje legible que lo
antecede, nunca dentro del código que compara el tester.

### `PosicionLexica` — dónde apunta el `^`

La cátedra no pide columna: es un agregado del proyecto (logro *Reporte de Error Elegante*). Como no
está especificada, aplica `AGENTS.md` §0.1 y el criterio es el de `javac`, **verificado
ejecutándolo**, no de memoria. Y `javac` sigue una sola regla:

> Si el mensaje habla de un **carácter ofensivo puntual**, el `^` va a ese carácter. Si habla de la
> **construcción entera** que falló, el `^` va a donde esa construcción **empieza**.

Evidencia, un caso por diagnóstico (fijada además en `PosicionDelCaretTest`, que es el test que
impide que esto vuelva a divergir por partes):

| fuente | diagnóstico de `javac` | señala |
|---|---|---|
| `v1 # v2` | `illegal character` | el `#` |
| `"a\q b\z c"` | `illegal escape character` (dos veces) | la `q` y la `z` — **no** la barra |
| `'\u12G4'` | `illegal unicode escape` | la `G` |
| `''` | `empty character literal` | la comilla de **cierre** |
| `'a` / `'ab'` | `unclosed character literal` | la comilla de **apertura** |
| `"hola sin cerrar` | `unclosed string literal` | la comilla de **apertura** |
| `1234567890123` | `integer number too large` | el **primer** dígito |
| `1e` | `malformed floating-point literal` | el **primer** carácter del literal |
| `/* sin cerrar` | `unclosed comment` | la barra de **apertura** |

De `javac` se copió **dónde cae el `^`**, no cuántos errores reporta: en `"a\q b\z c"` MiniJava
señala la `q` igual que él, pero reporta un solo error porque al primero descarta el literal.

Este criterio corrige un bug real: la mitad de los errores señalaba el inicio de la construcción y la
otra mitad el último carácter consumido, porque `posicionUltimoConsumido()` —introducido para
arreglar carets que colgaban en el aire, ver la sección siguiente— quedó aplicado también donde
correspondía la apertura. Para `charLiteral vacío`, la segunda comilla es el carácter que hace vacío
al literal y se señala esa comilla; los demás errores de construcción mantienen el inicio como punto
de diagnóstico.

#### El ancla, y cuál usar

Lo único que distingue entre sí a las formas de construir una `ExcepcionLexica` es **dónde cae el
`^`**. Eso es un dato, no cinco métodos distintos: viaja como `AnclaDeError`, y `BuilderDeLexema`
—que es quien tiene las dos fotos y el lookahead— lo resuelve a una `PosicionLexica`. Las dos fotos
(`posicionPrimerConsumido` y `posicionUltimoConsumido`) se capturan dentro de `avanzar()` **antes**
de consumir, cuando línea, columna y línea fuente todavía describen ese carácter.

| Ancla | Cuándo | Quién la usa |
|---|---|---|
| `ULTIMO_CONSUMIDO` | el carácter ofensivo ya se consumió y **es** el problema | `EstadoInicial` (carácter desconocido), `EstadoCharAbierto` (`CHAR_VACIO`) |
| `INICIO_DEL_LEXEMA` | el mensaje habla de la construcción entera | `EstadoCharCerrando`, `EstadoCharEscapado`, `EstadoStringAbierto` y `EstadoStringTrasBarra` (string sin cerrar), `EstadoEntero`, los dos de exponente, `EstadoOperadorDoble` |
| `LOOKAHEAD` | el carácter ofensivo es el lookahead y **todavía no** se consumió | `EstadoEscapeUnicode` |

La fábrica queda entonces en dos métodos: `crearExcepcionLexica(tipo, ancla)` y
`crearExcepcionLexicaEn(tipo, lexema, posicion)` — la única con posición explícita, para
`EstadoComentarioMultilinea`.

Las dos degradaciones viven en `BuilderDeLexema.posicionDe()` y no repartidas en la fábrica:
`INICIO_DEL_LEXEMA` sin lexema armado todavía cae en `LOOKAHEAD`, y `LOOKAHEAD` sobre un EOF o un
`'\n'` cae en `ULTIMO_CONSUMIDO` (ver la sección siguiente).

El comentario multilínea es el único caso que necesita la variante explícita, y por una razón
concreta: `EstadoBarra` llama `iniciarNuevoToken()` para descartar el `/*` del lexema, así que cuando
el error se lanza —posiblemente muchas líneas después, en el EOF— ya no queda rastro de dónde
empezaba. Sin esa foto, el mensaje decía la línea de apertura correcta pero mostraba la línea-fuente
y la columna del EOF, que puede caer en contenido no relacionado.

**Regla para código nuevo**: antes de agregar un `throw` en un estado nuevo, leé el mensaje que va a
imprimir y elegí el ancla con eso. ¿Nombra un carácter? Va sobre ese carácter (`ULTIMO_CONSUMIDO` si
ya lo consumiste, `LOOKAHEAD` si no). ¿Nombra una construcción (`litString sin cerrar`, `intLiteral
de más de 9 dígitos`)? `INICIO_DEL_LEXEMA`. Si dudás, escribí el caso en un `.java` y corré `javac`.

### El `^` nunca puede colgar después del final de la línea impresa

Bug de la misma familia, y la razón por la que existe `posicionUltimoConsumido`: **cualquier** estado
que ancla en `LOOKAHEAD` cuando el lookahead actual es EOF o un salto de
línea real reporta una posición ambiente (`getColumna()`) que apunta a un carácter que
`getLineaFuente()` ni siquiera incluye (esa función corta el texto antes del `\n`). El resultado: un
`^` una posición más allá del último carácter imprimible de la línea, señalando el aire. Afectaba a
`EstadoCharAbierto`, `EstadoStringAbierto`, `EstadoCharCerrando`, `EstadoCharEscapado`,
`EstadoStringTrasBarra`, `EstadoEscapeUnicode`, `EstadoFloatExponenteInicio` y
`EstadoFloatExponenteRequiereDigito` — prácticamente cualquier estado que puede toparse con un EOF o
un `\n` inesperado.

En vez de parchear cada uno, se centralizó en `BuilderDeLexema.posicionDe(LOOKAHEAD)`: si el
lookahead actual es EOF o `\n`, devuelve `posicionUltimoConsumido()` en lugar de la posición
ambiente — ningún estado individual necesita saberlo.

De aquellos ocho estados hoy queda **uno** usando ese ancla: los otros pasaron a
`INICIO_DEL_LEXEMA` al adoptar el criterio de `javac`, que es inmune al
problema por construcción (el primer carácter del lexema siempre está dentro de la línea que se
imprime). La protección sigue viva igual, para `EstadoEscapeUnicode` y para cualquier estado nuevo
que la necesite; `PosicionDelCaretTest` verifica la invariante caso por caso y
`PropiedadesDelReporteTest` la verifica sobre entradas generadas.

### El lexema de un error nunca debe llevar un carácter de control crudo

Segundo bug real, encontrado en la misma sesión que el de arriba: `EstadoCharAbierto` llamaba
`agregarCaracterOfensivo()` también para sus casos de EOF y salto de línea real (no sólo para la
comilla vacía). Eso metía un `\n` o el centinela de EOF crudo dentro del lexema — y como el lexema
se imprime tal cual en el encabezado legible y en `[Error:Lexema|NroLinea]`, un `\n` embebido partía
esas líneas del reporte en dos, rompiendo el formato de una sola línea por elemento que espera la
cátedra. Se manifestaba típicamente en errores en cascada: una recuperación deja una comilla simple
huérfana justo antes de un salto de línea real, y esa comilla abre un char literal "vacío" que
termina en `\n`.

`EstadoStringAbierto` ya tenía el criterio correcto (no agrega el `\n`/EOF que lo interrumpe al
lexema, sólo lanza). Se alineó `EstadoCharAbierto` al mismo criterio: `agregarCaracterOfensivo()`
sólo para la comilla de cierre inmediata (`''`, printable, sin problema); para EOF y salto de línea,
lanzar sin agregar nada. **Regla para código nuevo**: nunca llamar `agregarCaracterOfensivo()` con un
carácter no imprimible (`\n`, `\r`, el centinela de EOF) — si el carácter ofensivo es uno de esos, el
lexema se construye sin él.

### `FormateadorDeError` y el Reporte de Error Elegante

`FormateadorDeError.formatear(ExcepcionLexica)` arma las líneas de salida: el encabezado legible
(línea + columna + motivo, vía `TipoDeErrorLexico.describir(lexema)`), el detalle con tabs
expandidos a tabulaciones de ocho columnas, todos los controles, formatos, marcas combinantes y
espacios no convencionales convertidos a representaciones visibles, el `^` alineado con el ancho
visual del detalle y, a continuación, el código de contrato `[Error:Lexema|NroLinea]`. Si
el carácter señalado no tiene una celda visible propia —por ejemplo, una marca combinante— se lo
representa como `\uXXXX` en el detalle y el `^` se ubica sobre el inicio de esa representación. Los
pares surrogate bien formados se mantienen como una figura visible; si el error cae en una de sus
unidades UTF-16, el par se representa excepcionalmente como `\udddd\udddd` para que el puntero señale
la unidad exacta sin quedar dentro de la figura. El reporte de éxito conserva el renglón en blanco
antes de `[SinErrores]`, como en la slide 26. La
columna es un agregado del usuario más allá
de lo que pide la cátedra (que sólo exige línea) — ver `especificacion-lexica.md`.

```text
Error Léxico en línea 7, columna 4: litString con secuencia de escape inválida \q: "a\q
Detalle: "a\q b\z c"
            ^
[Error:"a\q|7]
```

El encabezado nombra la secuencia (`\q`) y el lexema armado (`"a\q`); el `^` cae en la `q`, como en
`javac`; el código de contrato lleva sólo el lexema, que es lo único que compara el tester.
