# Resolución de los logros de la Etapa 3

Pautas Etapa 3, slides 38-41.

## Los que se persiguen

Son los que no piden código.

| Logro | Qué exige | Cómo se busca |
|---|---|---|
| **Entrega Anticipada Semántica I** | Entregar 48 horas antes del cierre: hasta el 07/10/2026 a las 16:00 | Fecha |
| **Imbatibilidad Semántica I** | Pasar toda la batería de la cátedra | Los 8 casos adjuntos dan el código exacto (`CasosDeLaCatedraSemanticoTest`), y el corpus propio cubre cada control con al menos un caso |
| **Kudos** | Sólo errores menores | Ídem |

## Los que no

Los que piden código quedan fuera por decisión de alcance:

| Logro | Qué pediría |
|---|---|
| Sobrecarga! E3 | Los modificadores `sealed` y `final` |
| Herencia Múltiple E3 | Que una interfaz extienda varias, como en Java |
| Métodos Genéricos E3 | Métodos con sus propios parámetros de tipo |
| Multi-Detección Errores Semánticos E3 | Seguir después del primer error semántico |
| Genericidad Avanzada E3 | Controlar los genéricos anidados en las declaraciones |

Los logros de la Etapa 2 que el parser implementa no arrastran controles a esta etapa
([`decisiones-de-logica.md`](decisiones-de-logica.md) §1).
