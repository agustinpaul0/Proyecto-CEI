# Manejo de errores léxicos

Documento autocontenido: cómo se detecta, cómo se recupera y cómo se reporta un error léxico. Es el
desarrollo completo de dos de los ocho logros de la Etapa 1 —*Multi-detección de Errores Léxicos* y
*Reporte de Error Elegante*— y de los tres bugs reales que hubo que arreglar para que funcionaran
bien. Para la visión de conjunto, ver [`arquitectura.md`](arquitectura.md).

## 1. Cuándo y cómo se detecta un error

Un estado detecta un error léxico cuando, mirando el carácter actual, ninguna transición válida
existe desde ese estado (por ejemplo: `EstadoInicial` viendo un `#`, que no es letra, dígito, ni
ningún símbolo conocido). En ese momento se lanza `ExcepcionLexica`, que lleva: el tipo de error, el
lexema, la línea, la columna, el texto completo de la línea fuente (para el reporte elegante) y, en
un solo tipo de error, la secuencia ofensiva concreta (ver §2.1).

**Qué contiene el lexema**: siempre **el lexema armado hasta el momento del error**. No es una
decisión del proyecto, lo fija `Pautas Etapa 1.pdf` slide 24, y el código `[Error:Lexema|NroLinea]`
lleva ese mismo lexema. Qué caracteres alcanzaron a entrar en él se sigue del criterio de
recuperación de §2, y hay dos casos:

- Si el carácter ofensivo **es** el problema (un `#` suelto, un `''` vacío, un escape `\q` inválido),
  el estado lo consume y por lo tanto queda incluido en el lexema, como manda la cátedra en Clase 02
  slide 43.
- Si el problema es el lexema **ya construido** (un `&` sin su par, un `intLiteral` de más de nueve
  dígitos, un exponente sin dígitos), el carácter que se está mirando es un token perfectamente
  legítimo que no tiene nada que ver con el error: no se consume ni se incluye. Reportarlo sería
  engañoso y, peor, hacía desaparecer ese token de la salida.

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
> lexema, ni uno más** — salvo un escape mal formado dentro de un `litString`, que además descarta lo
> que queda del literal hasta su comilla de cierre (§2.1). Fuera de esa excepción no hay ningún paso
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
| `String s = "hola \q mundo";` | un error `illegal escape character`, y **sigue leyendo el literal hasta la comilla de cierre**; sin cascada |
| `int c = 1234567890123 + 9;` | un error `integer number too large`; **el `+ 9` sobrevive** |

O sea: `javac` **no** sincroniza hasta el delimitador de cierre, y **nunca descarta un carácter que
no formó parte del lexema reportado**. Las dos cosas quedaron reflejadas en el criterio.

### El bug que esto arregló

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

## 2.1. La única excepción: el escape mal formado dentro de un `litString`

Un escape mal formado dentro de un `litString` —`"a\q`, `"a\8`, o un escape Unicode sin sus cuatro
dígitos hexadecimales— es el único error que **no se limita a consumir lo que reporta**: reporta el
primero que encuentra y después descarta lo que queda del literal **hasta su comilla de cierre**, sin
emitir token.

```text
"a\q b\z c" + v1
→ Error Léxico en línea 1, columna 4: litString con secuencia de escape inválida \q: "a\q
→ [Error:"a\q|1]
→ (op+,+,1)
→ (idMV,v1,1)
→ (EOF,$,2)
```

Es **recuperación en modo pánico con token de sincronización**, la técnica clásica de construcción de
compiladores: ante un error se descarta entrada hasta un delimitador que devuelve al analizador a un
estado conocido. Acá el delimitador es la comilla de cierre, y el estado conocido es "fuera de todo
literal". El criterio lo fijó el profesor de la materia; su justificación completa, y la alternativa
que reemplaza, están en [`decisiones.md`](decisiones.md) §4.3.

Lo que se gana es que el análisis sigue **en el token siguiente al literal roto**, no en mitad de su
contenido. Sin resincronizar, el mismo fuente daría `idMV(b)`, un error por la barra suelta,
`idMV(z)`, `idMV(c)` y —lo peor— la comilla de **cierre** abriría un literal nuevo que se comería
` + v1` y moriría sin cerrar: tres tokens fantasma, dos errores inventados y dos tokens legítimos
perdidos, todo derivado de un único defecto real.

Tres detalles del encabezado, que vienen de reglas distintas:

- El **lexema** es `"a\q`, no el literal completo ni lo descartado: `Pautas Etapa 1.pdf` slide 24
  pide "el lexema armado hasta el momento del error". El descarte es recuperación, no parte de lo que
  se reporta, y por eso la excepción se construye **antes** de resincronizar.
- La **secuencia ofensiva** (`\q`) se nombra aparte porque ese lexema armado no *es* la secuencia, y
  sin nombrarla el mensaje se lee al revés. Es el único error con este dato extra.
- **No se emite token**: el literal quedó mal formado, y emitirlo sería afirmar que el programa
  contiene un `litString` que el compilador no pudo interpretar.

**Qué queda fuera del alcance.** Dentro de un mismo literal sólo se reporta un error: el segundo
escape roto de `"a\q b\z c"` no aparece, porque es un defecto derivado del mismo literal ya
descartado. Entre literales distintos la multi-detección sigue intacta — `"a\q" "b\z"` reporta dos
errores, uno por literal. Y en `charLiteral` el caso directamente no existe: `'\x'` con `x`
cualquiera es válido por permiso explícito de la cátedra (Aspectos Léxicos §2.3.2), y un escape
Unicode mal formado ahí conserva la recuperación estructural, porque no hay literal delimitado que
descartar.

Si el literal nunca cierra, el descarte se detiene en el salto de línea: un `litString` no cruza
líneas, así que `"a\q` seguido de un fin de línea reporta su error y deja la línea siguiente intacta.

## 2.2. Los tipos de error

`TipoDeErrorLexico` tiene un valor por punto de falla, cada uno con su propia razón. Antes había una
sola frase para los catorce, así que el reporte no podía decir qué construcción había fallado y los
tests no podían validar por tipo, que es la regla del proyecto (ver [`testing.md`](testing.md) §1).

| Valor | Razón que imprime |
|---|---|
| `CARACTER_DESCONOCIDO` | `<lexema> no es un símbolo válido` — **texto literal de la slide 43** |
| `CHAR_VACIO` | `charLiteral vacío` |
| `CHAR_MAL_FORMADO` | `charLiteral mal formado, contiene más de un carácter` |
| `CHAR_SIN_CERRAR` | `charLiteral sin cerrar` |
| `STRING_ESCAPE_INVALIDO` | `litString con secuencia de escape inválida <secuencia>` — el único que nombra la secuencia además del lexema, y uno de los dos que resincronizan (§2.1) |
| `STRING_SIN_CERRAR` | `litString sin cerrar` |
| `UNICODE_MAL_FORMADO` | `escape unicode mal formado, se esperaban 4 dígitos hexadecimales` — resincroniza sólo cuando ocurre dentro de un `litString` (§2.1) |
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

2. Un **código de error** de una sola línea, `[Error:Lexema|NroLinea]` — este es el contrato exacto
   que verifican los testers de la cátedra, y **no lleva columna** (la columna es sólo parte del
   reporte legible, que es un agregado propio más allá de lo pedido).

## 4. A qué posición apunta la columna

La cátedra no pide columna — sólo línea. La columna, y el `^` que la acompaña, son un agregado propio
del logro *Reporte de Error Elegante*. Como no está especificada, aplica la regla de "ante la duda,
como en Java", y el criterio se tomó de `javac` verificándolo contra el compilador real, un caso por
diagnóstico. `javac` sigue **una sola regla**:

> Si el mensaje habla de un **carácter ofensivo puntual**, el `^` va a ese carácter. Si habla de la
> **construcción entera** que falló, el `^` va a donde esa construcción **empieza**.

| fuente | diagnóstico de `javac` | dónde cae el `^` |
|---|---|---|
| `v1 # v2` | `illegal character` | el `#` |
| `"a\q b\z c"` | `illegal escape character` (dos veces) | la `q` y la `z` — no la barra |
| `'\u12G4'` | `illegal unicode escape` | la `G` |
| `''` | `empty character literal` | la comilla de apertura |
| `'a` / `'ab'` | `unclosed character literal` | la comilla de apertura |
| `"hola sin cerrar` | `unclosed string literal` | la comilla de apertura |
| `1234567890123` | `integer number too large` | el primer dígito |
| `1e` | `malformed floating-point literal` | el primer carácter del literal |
| `/* sin cerrar` | `unclosed comment` | la barra de apertura |

Tiene sentido más allá de imitar a Java: en `"hola sin cerrar`, lo que hay que corregir es la comilla
de apertura (falta su par), no la última letra que se alcanzó a leer; en `1234567890`, el problema es
el número entero, y su primer dígito es donde empieza a leerse.

La tabla describe **dónde pone `javac` el `^`**, que es lo único que se le copió. Cuántos errores
reporta es una decisión aparte: en `"a\q b\z c"` MiniJava señala la `q` igual que `javac`, pero
reporta un solo error y no dos, porque al primero descarta el literal entero (§2.1).

### Cómo se implementa

`BuilderDeLexema` —la pieza del contexto léxico que arma el lexema— mantiene **dos** fotos de
posición, ambas tomadas dentro de `avanzar()` justo antes de consumir —cuando línea, columna y línea-fuente todavía describen ese carácter— y ambas del tipo
`PosicionLexica`:

- `posicionPrimerConsumido`: el **primer** carácter del lexema en curso. Es la que usan los errores
  que hablan de la construcción entera, vía `crearExcepcionLexicaSobreElInicioDelLexema()`.
- `posicionUltimoConsumido`: el **último** carácter que quedó en el lexema. Es la que usan los
  errores cuyo carácter ofensivo ya fue consumido antes del `throw` — un símbolo desconocido, un
  escape inválido —, vía `crearExcepcionLexicaSobreElUltimoConsumido()`.

La distinción importa porque en el momento del `throw` el lookahead actual puede haber avanzado más
allá del lexema: puede ser un espacio, un EOF o la letra siguiente, sin ninguna relación con el
motivo del error. Reportar su columna producía un bug real, encontrado por el usuario corriendo la
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
último carácter visible, apuntando al aire en vez de a algo concreto. Afectaba a casi todos los
estados que pueden toparse con un EOF o salto de línea inesperado (chars y strings sin cerrar,
escapes interrumpidos, exponentes sin dígitos). Se resolvió en un solo lugar:
`FabricaDeExcepcionesLexicas.crearExcepcionLexica()` (la variante sin argumentos) usa
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

La regla, aplicada en un solo lugar (`FabricaDeExcepcionesLexicas`, al construir la excepción): **todo carácter
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

Esto vale **sólo** para el lexema de un error. El lexema de un **token** se imprime crudo, porque el
contrato de la cátedra es que sea el texto exacto del fuente — un tabulador dentro de un `litString`
sale como tabulador (ver `resources/sinErrores/stringConTabuladorInterior.java`).

Aparte de eso, los estados de escape (`EstadoCharEscapado`, `EstadoStringEscapado`) rechazan
explícitamente el salto de línea en vez de consumirlo: una barra invertida al final de una línea no
escapa nada, deja el literal sin cerrar.

## 7. Los dos defectos de robustez que trataba como fin de archivo

Dos casos en los que el compilador no fallaba con una `ExcepcionLexica` controlada sino de formas
peores, y que en este proyecto son siempre defectos (ver [`testing.md`](testing.md) §1):

- **El byte `0x1A`**: el centinela `SourceManager.END_OF_FILE` de la cátedra es `(char) 26`, que
  también es un byte perfectamente legítimo dentro de un archivo. Comparar el carácter devuelto
  contra el centinela hacía que un `0x1A` embebido **truncara el análisis en silencio** — el resto
  del archivo desaparecía y encima se imprimía `[SinErrores]`. Ahora
  `ManejadorDeArchivo.estaEnFinDeArchivo()` responde por **posición** y no por valor, y ese byte es
  un carácter desconocido más.
- **Bytes que no son UTF-8 válido**: `Files.readString` lanza `MalformedInputException`, que salía
  como un `UncheckedIOException` sin atrapar. Se cambió por `new String(bytes, UTF_8)`, que sustituye
  las secuencias mal formadas por `U+FFFD`; los bytes basura llegan al léxico y se reportan como
  errores léxicos normales.

## Ver también

- [`arquitectura.md`](arquitectura.md) — visión completa del proyecto, ciclo de vida de ejecución.
- [`decisiones.md`](decisiones.md) §4 — cada decisión de este documento, con su fuente.
- [`testing.md`](testing.md) — las invariantes que verifican todo lo de acá.
- [`logros.md`](logros.md) — los logros *Multi-detección de Errores* y *Reporte de Error Elegante*.
- [`especificacion-lexica.md`](especificacion-lexica.md) — ER formal de cada token.
