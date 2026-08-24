# Manejo de errores léxicos

Índice: [`../AGENTS.md`](../AGENTS.md). Para el flujo completo (detección → recuperación → los dos
formatos de reporte) con diagramas, ver
[`../documentacion/arquitectura.md`](../documentacion/arquitectura.md) §5 — este archivo cubre el
detalle de implementación de `ExcepcionLexica`/`PosicionLexica`/`FormateadorDeError`, no el tutorial.

## El contrato de `ExcepcionLexica`

Cada `ExcepcionLexica` lleva cuatro datos: lexema, línea, columna y la línea fuente completa (para el
Reporte de Error Elegante). `ContextoLexico.crearExcepcionLexica()` es el punto único de construcción
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
la posición del último carácter que sí quedó en el lexema. `EstadoEntero` y `EstadoOperadorDoble` lo
usan para el `throw`. El mismo mecanismo resuelve un segundo bug relacionado: el comentario
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
