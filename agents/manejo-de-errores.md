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
mal), no lo toques y usá `crearExcepcionLexica(tipo, lexema, posicionUltimoConsumido())`.
`agregarCaracterOfensivo()` ya no existe: era justamente el método que rompía la correspondencia
entre lo que se reporta y lo que se consume, y por eso la recuperación se comía tokens válidos.

El único error **no fatal** es el escape inválido dentro de un `litString`: se registra con
`contexto.registrarError(...)` y el literal se sigue leyendo, igual que hace `javac`.

## El contrato de `ExcepcionLexica`

Cada `ExcepcionLexica` lleva cinco datos: tipo (`TipoDeErrorLexico`), lexema, línea, columna y la
línea fuente completa (para el Reporte de Error Elegante). `ContextoLexico.crearExcepcionLexica()` es el punto único de construcción
— ningún estado arma una `ExcepcionLexica` a mano, siempre pasa por ahí, para que los cuatro campos
sean consistentes entre sí.

**Invariante de la cátedra** (`convenciones-catedra.md`): el código de contrato
`[Error:Lexema|NroLinea]` **no lleva columna** — la columna sólo aparece en el mensaje legible que lo
antecede, nunca dentro del código que compara el tester.

## `PosicionLexica` — cuándo un error reporta el lookahead actual y cuándo no

Bug real encontrado por el usuario corriendo la suite desde IntelliJ (el `^` apuntaba a un carácter
distinto del que decía el mensaje, en varios casos). Hay dos categorías de error, y confundirlas fue
la causa del bug:

1. **"Lo que estoy mirando ahora mismo es inválido"** (un carácter desconocido, una comilla de cierre
   ausente, un dígito de exponente faltante, un escape de string no reconocido): el lookahead actual
   (`contexto.getColumna()`/`getLineaFuente()`) **es** el problema — reportarlo es correcto.
2. **"El lexema que ya terminé de construir resultó inválido como un todo"** (un `&`/`|` sin su
   segundo carácter, un `intLiteral` de más de 9 dígitos): en el momento del `throw`, el lookahead
   actual ya avanzó más allá del lexema completo — puede ser cualquier cosa (un espacio, EOF, letra
   siguiente) y **no tiene relación con el error**. Reportar su columna es lo que producía el bug.

`ContextoLexico.posicionUltimoConsumido()` resuelve la categoría 2: devuelve un `PosicionLexica`
(línea + columna + línea fuente) capturado dentro de `avanzar()`, **antes** de consumir — es decir,
la posición del último carácter que sí quedó en el lexema. `EstadoEntero`, `EstadoOperadorDoble` y los dos
estados de exponente lo usan para el `throw` (los de exponente estaban mal clasificados en la
categoría 1: reportaban la columna del carácter siguiente cuando había uno, y la correcta cuando
venía un fin de línea). El mismo mecanismo resuelve un segundo bug relacionado: el comentario
multilínea sin cerrar reportaba la línea de apertura correcta en el mensaje, pero la línea-fuente y
la columna mostradas eran las de la posición del EOF (que puede estar varias líneas después, en
contenido no relacionado) — `EstadoBarra` ahora captura `posicionUltimoConsumido()` (la posición del
`/` de apertura) al construir `EstadoComentarioMultilinea`, y el `throw` usa esa foto completa en vez
de mezclar una línea overrideada con una columna/línea-fuente que seguían siendo las del lookahead
actual.

**Regla para código nuevo**: antes de agregar un `throw contexto.crearExcepcionLexica...` en un
estado nuevo, preguntarse a cuál de las dos categorías de arriba pertenece. Si es la 2 (el lexema ya
construido es el que está mal, no el carácter que se está mirando ahora), usar
`contexto.posicionUltimoConsumido()`, no la posición ambiente.

## El `^` nunca puede colgar después del final de la línea impresa

Tercer bug de la misma familia, más sistémico que los dos anteriores: **cualquier** estado que lanza
`crearExcepcionLexica()` (sin argumentos) cuando el lookahead actual es EOF o un salto de línea real
cae en la misma trampa que categoría 2 de arriba, pero por una razón distinta — no es que el lexema
completo esté mal, es que la posición ambiente (`getColumna()`) apunta a un carácter que
`getLineaFuente()` ni siquiera incluye (esa función corta el texto antes del `\n`). El resultado: un
`^` una posición más allá del último carácter imprimible de la línea, señalando el aire. Afectaba a
`EstadoCharAbierto`, `EstadoStringAbierto`, `EstadoCharCerrando`, `EstadoCharEscapado`,
`EstadoStringEscapado`, `EstadoEscapeUnicode`, `EstadoFloatExponenteInicio` y
`EstadoFloatExponenteRequiereDigito` — prácticamente cualquier estado que puede toparse con un EOF o
un `\n` inesperado.

En vez de parchear cada uno, se centralizó en `ContextoLexico.crearExcepcionLexica()` (la variante sin
argumentos): si el lookahead actual es EOF o `\n`, usa `posicionUltimoConsumido()` automáticamente en
lugar de la posición ambiente — ningún estado individual necesita saberlo. **Esta es la razón por la
que la variante sin argumentos sigue siendo la opción por defecto para código nuevo**: ya incorpora
esta protección; sólo hace falta pasar a la variante con `PosicionLexica` explícita para la categoría
2 de arriba (lexema completo inválido, no una interrupción por EOF/salto de línea).

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
legible (línea + columna + motivo), la línea fuente completa, el `^` posicionado con
`" ".repeat("Detalle: ".length() + columna - 1)`, y el código de contrato
`[Error:Lexema|NroLinea]`. La columna es un agregado del usuario más allá de lo que pide la cátedra
(que sólo exige línea) — ver `especificacion-lexica.md`.
