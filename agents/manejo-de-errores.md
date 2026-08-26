# Manejo de errores léxicos

Índice: [`../AGENTS.md`](../AGENTS.md). Para el flujo completo (detección → recuperación → los dos
formatos de reporte) con diagramas, ver
[`../documentacion/arquitectura.md`](../documentacion/arquitectura.md) §5 — este archivo cubre el
detalle de implementación de `ExcepcionLexica`/`PosicionLexica`/`FormateadorDeError`, no el tutorial.

## El criterio de recuperación (leer primero)

**Todo estado que lanza un error consume del fuente exactamente los caracteres que reporta en el
lexema, ni uno más.** No existe `recuperarDeError()`: la recuperación es estructural, el siguiente
`proximoToken()` arranca en el primer carácter que no formó parte del lexema. Verificado contra
`javac` (ver `documentacion/manejo-de-errores.md` §2), porque la cátedra no dice nada sobre cómo
recuperarse — su comportamiento base ni siquiera tiene recuperación.

**Regla para código nuevo**: si vas a agregar un `throw` en un estado, preguntate si el carácter que
estás mirando forma parte del error. Si sí, `avanzar()` y usar
`crearExcepcionLexicaSobreElUltimoConsumido(tipo)`. Si no (el lexema ya construido es el que está
mal), no lo toques. Dónde apunta el `^` en cada caso es una decisión aparte, y está más abajo.
`agregarCaracterOfensivo()` ya no existe: era justamente el método que rompía la correspondencia
entre lo que se reporta y lo que se consume, y por eso la recuperación se comía tokens válidos.

El único error **no fatal** es el escape inválido dentro de un `litString`: se registra con
`contexto.registrarError(...)` y el literal se sigue leyendo, igual que hace `javac`.

## El contrato de `ExcepcionLexica`

Cada `ExcepcionLexica` lleva cinco datos obligatorios: tipo (`TipoDeErrorLexico`), lexema, línea,
columna y la línea fuente completa (para el Reporte de Error Elegante), más uno opcional, el
**fragmento ofensivo**. `ContextoLexico.crearExcepcionLexica()` es el punto único de construcción
— ningún estado arma una `ExcepcionLexica` a mano, siempre pasa por ahí, para que los campos
sean consistentes entre sí.

**El lexema es el armado hasta el momento del error, siempre.** No es una elección nuestra: lo fija
`Pautas Etapa 1.pdf` slide 24 (*"Deberá reportarse un mensaje adecuado a la naturaleza del error
indicando el lexema armado hasta el momento del error y en qué número de línea del fuente MiniJava se
produjo"*), y el `[Error:Lexema|NroLinea]` tiene que llevar ese mismo lexema. De ahí que dos escapes
inválidos en un mismo `litString` reporten `"a\q` y después `"a\q b\z`, con el segundo conteniendo al
primero: es exactamente lo pedido, no un defecto.

**El fragmento ofensivo existe justamente por eso.** Cuando el lexema armado es más grande que la
parte que está mal, el encabezado se lee al revés (`secuencia de escape inválida: "a\q` da a entender
que `"a\q` es la secuencia). El fragmento viaja aparte, desde el estado que lo detecta, y sólo se usa
en el encabezado legible: `litString con secuencia de escape inválida \q: "a\q`. El código de
contrato de la cátedra no lo ve. Hoy lo usa un único tipo, `STRING_ESCAPE_INVALIDO`, y es el único
con una segunda plantilla en `TipoDeErrorLexico`; los demás lo dejan en `null` y `describir()` se
degrada solo al mensaje de un dato.

**Invariante de la cátedra** (`convenciones-catedra.md`): el código de contrato
`[Error:Lexema|NroLinea]` **no lleva columna** — la columna sólo aparece en el mensaje legible que lo
antecede, nunca dentro del código que compara el tester.

## `PosicionLexica` — dónde apunta el `^`

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
| `''` | `empty character literal` | la comilla de **apertura** |
| `'a` / `'ab'` | `unclosed character literal` | la comilla de **apertura** |
| `"hola sin cerrar` | `unclosed string literal` | la comilla de **apertura** |
| `1234567890123` | `integer number too large` | el **primer** dígito |
| `1e` | `malformed floating-point literal` | el **primer** carácter del literal |
| `/* sin cerrar` | `unclosed comment` | la barra de **apertura** |

Este criterio corrige un bug real: la mitad de los errores señalaba el inicio de la construcción y la
otra mitad el último carácter consumido, porque `posicionUltimoConsumido()` —introducido para
arreglar carets que colgaban en el aire, ver la sección siguiente— quedó aplicado también donde
correspondía la apertura. Un `charLiteral vacío: ''` señalaba la comilla de cierre; un `litString sin
cerrar` de veinte caracteres señalaba el vigésimo.

### Las variantes de `ContextoLexico`, y cuál usar

Todas devuelven una `ExcepcionLexica` ya armada; las dos posiciones (`posicionPrimerConsumido` y
`posicionUltimoConsumido`) son `PosicionLexica` —línea + columna + línea fuente— capturadas dentro de
`avanzar()` **antes** de consumir, cuando esos tres datos todavía describen ese carácter.

| Variante | Cuándo | Quién la usa |
|---|---|---|
| `crearExcepcionLexicaSobreElUltimoConsumido(tipo)` | el carácter ofensivo ya se consumió y **es** el problema | `EstadoInicial` (carácter desconocido) |
| `crearExcepcionLexicaSobreElUltimoConsumido(tipo, fragmento)` | ídem, nombrando además la parte ofensiva dentro del lexema armado | `EstadoStringEscapado` (escape inválido) |
| `crearExcepcionLexicaSobreElInicioDelLexema(tipo)` | el mensaje habla de la construcción entera | los dos estados de char sin cerrar/vacío/mal formado, `EstadoStringAbierto`, `EstadoEntero`, los dos de exponente, `EstadoOperadorDoble` |
| `crearExcepcionLexica(tipo)` | el carácter ofensivo es el lookahead y **todavía no** se consumió | `EstadoEscapeUnicode` |
| `crearExcepcionLexica(tipo, lexema, posicion)` | la posición no es reconstruible en el `throw` | `EstadoComentarioMultilinea`, que recibe la apertura fotografiada por `EstadoBarra` |

El comentario multilínea es el único caso que necesita la variante explícita, y por una razón
concreta: `EstadoBarra` llama `iniciarNuevoToken()` para descartar el `/*` del lexema, así que cuando
el error se lanza —posiblemente muchas líneas después, en el EOF— ya no queda rastro de dónde
empezaba. Sin esa foto, el mensaje decía la línea de apertura correcta pero mostraba la línea-fuente
y la columna del EOF, que puede caer en contenido no relacionado.

**Regla para código nuevo**: antes de agregar un `throw` en un estado nuevo, leé el mensaje que va a
imprimir. ¿Nombra un carácter? Va sobre ese carácter. ¿Nombra una construcción (`litString sin
cerrar`, `intLiteral de más de 9 dígitos`)? Va sobre su primer carácter. Si dudás, escribí el caso en
un `.java` y corré `javac`.

## El `^` nunca puede colgar después del final de la línea impresa

Bug de la misma familia, y la razón por la que existe `posicionUltimoConsumido`: **cualquier** estado
que lanza `crearExcepcionLexica()` (sin argumentos) cuando el lookahead actual es EOF o un salto de
línea real reporta una posición ambiente (`getColumna()`) que apunta a un carácter que
`getLineaFuente()` ni siquiera incluye (esa función corta el texto antes del `\n`). El resultado: un
`^` una posición más allá del último carácter imprimible de la línea, señalando el aire. Afectaba a
`EstadoCharAbierto`, `EstadoStringAbierto`, `EstadoCharCerrando`, `EstadoCharEscapado`,
`EstadoStringEscapado`, `EstadoEscapeUnicode`, `EstadoFloatExponenteInicio` y
`EstadoFloatExponenteRequiereDigito` — prácticamente cualquier estado que puede toparse con un EOF o
un `\n` inesperado.

En vez de parchear cada uno, se centralizó en `ContextoLexico.crearExcepcionLexica()` (la variante sin
argumentos): si el lookahead actual es EOF o `\n`, usa `posicionUltimoConsumido()` automáticamente en
lugar de la posición ambiente — ningún estado individual necesita saberlo.

De aquellos ocho estados hoy quedan **dos** usando esa variante: los otros seis pasaron a
`crearExcepcionLexicaSobreElInicioDelLexema()` al adoptar el criterio de `javac`, que es inmune al
problema por construcción (el primer carácter del lexema siempre está dentro de la línea que se
imprime). La protección sigue viva igual, para `EstadoEscapeUnicode` y para cualquier estado nuevo
que la necesite; `PosicionDelCaretTest` verifica la invariante caso por caso y
`PropiedadesDelReporteTest` la verifica sobre entradas generadas.

## El lexema de un error nunca debe llevar un carácter de control crudo

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

## `FormateadorDeError` y el Reporte de Error Elegante

`FormateadorDeError.formatear(ExcepcionLexica)` arma las cuatro líneas de salida: el encabezado
legible (línea + columna + motivo, vía `TipoDeErrorLexico.describir(lexema, fragmento)`), la línea
fuente completa, el `^` posicionado con `" ".repeat("Detalle: ".length() + columna - 1)`, y el código
de contrato `[Error:Lexema|NroLinea]`. La columna es un agregado del usuario más allá de lo que pide
la cátedra (que sólo exige línea) — ver `especificacion-lexica.md`. Las cuatro líneas para un escape
inválido, que es el caso que ejercita todo junto:

```text
Error Léxico en línea 7, columna 4: litString con secuencia de escape inválida \q: "a\q
Detalle: "a\q b\z c"
            ^
[Error:"a\q|7]
```

El encabezado nombra la secuencia (`\q`) y el lexema armado (`"a\q`); el `^` cae en la `q`, como en
`javac`; el código de contrato lleva sólo el lexema, que es lo único que compara el tester.
