# Manejo de errores léxicos

Documento autocontenido: cómo se detecta, recupera y reporta un error léxico. Complementa
[`arquitectura.md`](arquitectura.md) (que referencia esta sección desde su ciclo de vida de
ejecución) — para el resto del proyecto, ver ese documento.

## 1. Cuándo y cómo se detecta un error

Un estado detecta un error léxico cuando, mirando el carácter actual, ninguna transición válida
existe desde ese estado (por ejemplo: `EstadoInicial` viendo un `#`, que no es letra, dígito, ni
ningún símbolo conocido). En ese momento se lanza `ExcepcionLexica`, que lleva: el tipo de error, el
lexema, la línea, la columna, y el texto completo de la línea fuente (para el reporte elegante).

**Qué contiene el lexema** — se sigue del criterio de recuperación de §2, y hay dos casos:

- Si el carácter ofensivo **es** el problema (un `#` suelto, un `''` vacío, un escape `\q` inválido),
  el estado lo consume y por lo tanto queda incluido en el lexema, como manda la cátedra en Clase 02
  slide 43.
- Si el problema es el lexema **ya construido** (un `&` sin su par, un `intLiteral` de más de nueve
  dígitos, un exponente sin dígitos), el carácter que se está mirando es un token perfectamente
  legítimo que no tiene nada que ver con el error: no se consume ni se incluye. Reportarlo sería
  engañoso y, peor, hacía desaparecer ese token de la salida.

## 2. Recuperación: el criterio, en una línea

La consigna base de la cátedra dice: *"al detectar un error deberá reportarse y finalizar la
ejecución"*. El logro *Multi-detección de Errores* cambia ese comportamiento: en vez de terminar, el
Módulo Principal captura la excepción, la reporta y sigue pidiendo tokens. **La cátedra no dice nada
sobre CÓMO recuperarse** — su comportamiento base no tiene recuperación en absoluto — así que aplica
la regla de `AGENTS.md` §0.1: como en Java, verificado contra `javac`, no de memoria.

> **Todo estado que lanza un error consume del fuente exactamente los caracteres que reporta en el
> lexema, ni uno más.** No hay ningún paso de recuperación posterior: el siguiente `proximoToken()`
> arranca en el primer carácter que **no** formó parte del lexema reportado.

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
todos los errores fatales **reconstruye el fuente completo**.

## 2.1. El único error no fatal

`javac` reporta un escape inválido dentro de un `litString` pero **no aborta el literal**: lo sigue
leyendo hasta su comilla de cierre. MiniJava hace lo mismo, y es el único error del léxico que
produce un token válido además del mensaje:

```text
"hola \q mundo"
→ Error Léxico en línea 1, columna 8: litString con secuencia de escape inválida: "hola \q
→ [Error:"hola \q|1]
→ (litString,"hola \q mundo",1)
```

`ContextoLexico.registrarError()` acumula estos errores y `ModuloPrincipal` los drena y los reporta
justo antes de imprimir el token dentro del cual ocurrieron.

**El alcance es deliberadamente acotado a ese caso**, que es exactamente donde `javac` lo hace. El
escape unicode mal formado sigue siendo fatal, porque Java lo resuelve en un nivel completamente
distinto (pre-lexer, JLS §3.3) y ahí no hay comportamiento de Java que copiar. En `charLiteral` el
caso no existe: `'\x'` con `x` cualquiera es válido por permiso explícito de la cátedra (Aspectos
Léxicos §2.3.2).

## 2.2. Los tipos de error

`TipoDeErrorLexico` tiene un valor por punto de falla, cada uno con su propia razón. Antes había una
sola frase para los catorce, así que el reporte no podía decir qué construcción había fallado y los
tests no podían validar por tipo (que es lo que pide `agents/testing.md`).

| Valor | Razón que imprime |
|---|---|
| `CARACTER_DESCONOCIDO` | `<lexema> no es un símbolo válido` — **texto literal de la slide 43** |
| `CHAR_VACIO` | `charLiteral vacío` |
| `CHAR_MAL_FORMADO` | `charLiteral mal formado, contiene más de un carácter` |
| `CHAR_SIN_CERRAR` | `charLiteral sin cerrar` |
| `STRING_ESCAPE_INVALIDO` | `litString con secuencia de escape inválida` (no fatal) |
| `STRING_SIN_CERRAR` | `litString sin cerrar` |
| `UNICODE_MAL_FORMADO` | `escape unicode mal formado, se esperaban 4 dígitos hexadecimales` |
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
   completa, y un `^` apuntando exactamente a la columna del carácter ofensivo. Ejemplo real
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

## 4. A qué posición apunta la columna — dos categorías de error

No todo error se reporta sobre "el carácter que se está mirando ahora mismo". Confundir esto produjo
un bug real (el `^` apuntaba a un carácter distinto del que decía el mensaje), encontrado por el
usuario corriendo la suite desde IntelliJ. Hay dos categorías:

1. **"Lo que estoy mirando ahora mismo es inválido"** — un carácter desconocido, una comilla de
   cierre ausente, un escape de string no reconocido. El lookahead actual **es** el problema, así que
   su columna es la que corresponde reportar.
2. **"El lexema que ya terminé de construir resultó inválido como un todo"** — un `&`/`|` sin su
   segundo carácter obligatorio, un `intLiteral` de más de 9 dígitos. En el momento de lanzar el
   error, el lookahead actual ya avanzó más allá del lexema completo: puede ser cualquier cosa (un
   espacio, EOF, la letra siguiente) y no tiene ninguna relación con el motivo del error. Reportar
   esa posición es lo que producía el bug.

Para la categoría 2, `ContextoLexico` expone `posicionUltimoConsumido()`: una foto (línea + columna +
línea fuente) tomada dentro de `avanzar()`, justo antes de consumir — es decir, la posición del
**último carácter que sí quedó en el lexema**, no la del próximo. `EstadoEntero` (dígitos de más),
`EstadoOperadorDoble` (`&`/`|` sin su par) y los dos estados de exponente la usan al construir su
`ExcepcionLexica`.

Los estados de exponente estaban mal clasificados en la categoría 1 y se corrigieron: `1e ` reportaba
la columna del espacio (que no es parte de `1e` ni tiene nada que ver con el error) mientras que
`1e` al final de una línea reportaba bien la columna de la `e`, porque el fallback automático de la
§5 entraba en acción. Dos columnas distintas para el mismo error según lo que viniera detrás.

El mismo mecanismo resuelve un segundo bug relacionado: un comentario multilínea sin cerrar reportaba
correctamente la línea de **apertura** en el mensaje, pero la línea-fuente y la columna mostradas
eran las de donde el analizador encontró el EOF — que puede estar varias líneas después, mostrando
contenido completamente distinto (a veces una línea vacía). Ahora `EstadoBarra` captura
`posicionUltimoConsumido()` (la posición exacta del `/` de apertura) en el momento de entrar al
comentario, y ese snapshot completo — no sólo el número de línea — es lo que se usa si el comentario
nunca cierra.

## 5. El `^` nunca puede colgar después del final de la línea impresa

Cuarto bug de la misma familia, más sistémico: **cualquier** estado que lanza la excepción cuando el
lookahead es EOF o un `\n` cae en la misma trampa que la categoría 2 de arriba, pero por otra razón —
`getLineaFuente()` corta el texto justo antes del salto de línea, así que esa posición ni siquiera
aparece en la línea impresa. El resultado visual: un `^` una posición más allá del último carácter
visible, apuntando al aire en vez de a algo concreto. Afectaba a casi todos los estados que pueden
toparse con un EOF o salto de línea inesperado (chars y strings sin cerrar, escapes interrumpidos,
exponentes sin dígitos). Se resolvió en un solo lugar: `ContextoLexico.crearExcepcionLexica()` (la
variante sin argumentos) ahora usa automáticamente `posicionUltimoConsumido()` cuando detecta que el
lookahead es EOF o `\n`, sin que cada estado individual tenga que saberlo.

## 6. El lexema de un error nunca lleva un carácter de control crudo

El lexema de un error se imprime literal en el encabezado legible y dentro de
`[Error:Lexema|NroLinea]`. Un carácter de control ahí es, en el mejor de los casos, invisible; y si
es un salto de línea, **parte el código de error en dos líneas** y rompe el contrato de la cátedra de
un elemento por línea. Pasaba típicamente en cascadas: una comilla simple huérfana justo antes de un
salto de línea real abre un char literal que "termina" en ese salto.

La regla, aplicada en un solo lugar (`ContextoLexico`, al construir la excepción): **todo carácter
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
peores, y que según `agents/testing.md` son siempre defectos:

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
- [`especificacion-lexica.md`](especificacion-lexica.md) — ER formal de cada token.
