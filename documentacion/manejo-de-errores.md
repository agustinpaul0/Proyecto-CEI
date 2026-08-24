# Manejo de errores léxicos

Documento autocontenido: cómo se detecta, recupera y reporta un error léxico. Complementa
[`arquitectura.md`](arquitectura.md) (que referencia esta sección desde su ciclo de vida de
ejecución) — para el resto del proyecto, ver ese documento.

## 1. Cuándo y cómo se detecta un error

Un estado detecta un error léxico cuando, mirando el carácter actual, ninguna transición válida
existe desde ese estado (por ejemplo: `EstadoInicial` viendo un `#`, que no es letra, dígito, ni
ningún símbolo conocido). En ese momento se lanza `ExcepcionLexica`, que lleva: el lexema armado
hasta ese punto (incluyendo el carácter ofensivo), la línea, la columna, y el texto completo de la
línea fuente (para el reporte elegante).

## 2. Recuperación (logro Multi-detección de Errores)

La consigna base de la cátedra dice: *"al detectar un error deberá reportarse y finalizar la
ejecución"*. El logro *Multi-detección de Errores* cambia ese comportamiento: en vez de terminar,
el Módulo Principal captura la excepción, la reporta, le pide al Analizador Léxico que **se
recupere** (`recuperarDeError()` — descarta el carácter que rompió todo, o llega a EOF) y sigue
pidiendo tokens. Esto garantiza terminación: cada recuperación avanza al menos un carácter, así que
en el peor caso el análisis termina cuando se acaba el archivo.

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
   cierre ausente, un dígito de exponente faltante, un escape de string no reconocido. El lookahead
   actual **es** el problema, así que su columna es la que corresponde reportar.
2. **"El lexema que ya terminé de construir resultó inválido como un todo"** — un `&`/`|` sin su
   segundo carácter obligatorio, un `intLiteral` de más de 9 dígitos. En el momento de lanzar el
   error, el lookahead actual ya avanzó más allá del lexema completo: puede ser cualquier cosa (un
   espacio, EOF, la letra siguiente) y no tiene ninguna relación con el motivo del error. Reportar
   esa posición es lo que producía el bug.

Para la categoría 2, `ContextoLexico` expone `posicionUltimoConsumido()`: una foto (línea + columna +
línea fuente) tomada dentro de `avanzar()`, justo antes de consumir — es decir, la posición del
**último carácter que sí quedó en el lexema**, no la del próximo. `EstadoEntero` (dígitos de más) y
`EstadoOperadorDoble` (`&`/`|` sin su par) la usan al construir su `ExcepcionLexica`.

El mismo mecanismo resuelve un segundo bug relacionado: un comentario multilínea sin cerrar reportaba
correctamente la línea de **apertura** en el mensaje, pero la línea-fuente y la columna mostradas
eran las de donde el analizador encontró el EOF — que puede estar varias líneas después, mostrando
contenido completamente distinto (a veces una línea vacía). Ahora `EstadoBarra` captura
`posicionUltimoConsumido()` (la posición exacta del `/` de apertura) en el momento de entrar al
comentario, y ese snapshot completo — no sólo el número de línea — es lo que se usa si el comentario
nunca cierra.

## 5. El lexema nunca lleva un carácter de control crudo

Un tercer bug de la misma familia: si el carácter ofensivo agregado al lexema era un salto de línea
real o el centinela de EOF (no imprimibles), ese byte quedaba embebido tal cual en el lexema — y como
el lexema se imprime literal en el mensaje y en `[Error:Lexema|NroLinea]`, un `\n` crudo partía esas
líneas del reporte en dos, violando el formato de una sola línea por elemento. Pasaba típicamente en
cascadas de recuperación: una comilla simple huérfana que queda justo antes de un salto de línea real
abre un char literal que "termina" en ese salto. La regla, ya aplicada consistentemente: si el
carácter ofensivo no es imprimible, se lanza la excepción sin agregarlo al lexema (el lexema queda
compuesto sólo por lo que ya se había consumido antes de él).

## Ver también

- [`arquitectura.md`](arquitectura.md) — visión completa del proyecto, ciclo de vida de ejecución.
- [`especificacion-lexica.md`](especificacion-lexica.md) — ER formal de cada token.
