# Resolución de los logros de la Etapa 5

Pautas Etapa 5, slides 36-42.

## Los que se persiguen

Son los que no piden código.

| Logro | Qué exige | Cómo se busca |
|---|---|---|
| **Imbatibilidad Generación** | Pasar toda la batería de la cátedra | Los cuatro casos adjuntos pasan con su tester. Cada construcción del lenguaje tiene casos contrastados con Java, y el contraste diferencial recorre miles de programas generados. |
| **Entrega Anticipada Generación** | Entregar 48 horas antes del cierre: hasta el 18/11/2026 a las 16:00 | Fecha |
| **Congrats!** | Sólo errores menores | Ídem Imbatibilidad |

## Los que no

Los que piden código quedan fuera por decisión de alcance:

| Logro | Qué pediría |
|---|---|
| Floats y Coerción E5 | Código para operar con `float` y las coerciones, incluida la concatenación de Strings |
| Herencia Múltiple E5 | Clases que implementan varias interfaces |
| toString REAL | Que `toString` devuelva el nombre de la clase del receptor |
| Fors! - E5 | Ejecutar el `for` y el `for` de iteradores |
| Minilambdas E5 | Ejecutar las minilambdas |
| Atributos Inicializados E5 | Inicializar los atributos al crear el objeto |
| Super! - Ejecutado | Llamar a métodos del ancestro con `super` |
| Ternario E5 | Ejecutar el ternario |
| Variables Locales Clásicas E5 | Reservar e inicializar `int x, y = 3;` |
| Postfijos E5 | Ejecutar `x++` y `x--` |

Las construcciones de los logros de la Etapa 2 que el parser implementa siguen dando un error
semántico ([`decisiones-de-logica.md`](decisiones-de-logica.md) §1).
