# Resolución de los logros de la Etapa 4

Pautas Etapa 4, slides 39-48.

## Los que se persiguen

Son los que no piden código.

| Logro | Qué exige | Cómo se busca |
|---|---|---|
| **Imbatibilidad Semántica II** | Pasar toda la batería de la cátedra | Los casos adjuntos dan el código exacto (`CasosDeLaCatedraSentenciasTest`), salvo `semIICorrecto03`, consultado (duda 2). Cada control del documento tiene casos con y sin error, y el corpus generado recorre las matrices de tipos contrastadas con `javac`. |
| **Entrega Anticipada** | Entregar 48 horas antes del cierre: hasta el 28/10/2026 a las 16:00 | Fecha |
| **Grandioso Laburo!** | Sólo errores menores | Ídem Imbatibilidad |

## Los que no

Los que piden código quedan fuera por decisión de alcance:

| Logro | Qué pediría |
|---|---|
| Minilambdas E4 | Controlar expresiones lambda |
| For E4 | Controlar el `for` y el `for` de iteradores con `MiniIterable<T>` |
| Todos los Caminos llevan al Retorno sin código muerto | Exigir retorno en todos los caminos y rechazar el código inalcanzable |
| Floats y Coerción | Controlar los `float` y las coerciones entre `int`, `char`, `float` y `String` |
| Ternario E4 | Tipar el ternario |
| Genericidad Avanzada | Genéricos anidados y el diamante en expresiones |
| Instanceof | El operador `instanceof` |
| Visibilidad - Chequeada | Respetar `public` y `private` en los accesos |
| Super! E4 | `super` para miembros y constructores del ancestro |
| Métodos Genéricos E4 | Métodos con sus propios parámetros de tipo |
| Variables Locales Clásicas Controladas | Tipar `int x, y = 3;` |
| Multi-Detección Errores Semánticos en Sentencias | Seguir después del primer error |
| Atributos Inicializados – Chequeado | Tipar la inicialización de un atributo |
| Postfijos - Chequeado | Tipar `x++` y `x--` |

Las construcciones de los logros de la Etapa 2 que el parser implementa no se controlan: dan un
error semántico ([`decisiones-de-logica.md`](decisiones-de-logica.md) §1).
