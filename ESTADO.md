# Estado del proyecto

Dónde está cada etapa. El detalle técnico vive en `documentacion/`; lo que indicó el profesor, en
[`CONVENCIONES-CATEDRA.md`](CONVENCIONES-CATEDRA.md).

**Última actualización:** 29/09/2026

## Etapa 1 — Analizador Léxico: TERMINADA

Entregada y corregida sin errores ni observaciones, con sus 8 logros. Corre aislada con `--etapa=1`
y su corpus (158 casos) está en `compilador/resources/lexico/` (`mvn test -Pfase-lexico`).
`--etapa=1` reconoce también `for`, `private` y `?`, que agrega la Etapa 2.

## Etapa 2 — Analizador Sintáctico: TERMINADA

Entrega 23/09/2026. Descendente recursivo predictivo LL(1), Esquema Simple de Clase 5. Corre
aislada con `--etapa=2`, y su corpus está en `compilador/resources/sintactico/`
(`mvn test -Pfase-sintactico`).

| | |
|---|---|
| Gramática | 97 no terminales, 197 producciones, 59 terminales; LL(1), 2 ambigüedades declaradas |
| Corpus propio | 10.405 fuentes, 75 casos de gramática y los 8 de la cátedra |
| Corpus externo | 29.101 fuentes de Giuliano Taormina; en todos coincide el primer código |
| Suite | 26.648 tests; 98,4 % de líneas, 93,8 % de ramas; mutación 90 % |
| Informe | `etapas/etapa-2/entregables/`, 22 páginas |

Dos lecturas siguen a la base y no a Java, a sabiendas y documentadas en el informe: un atributo
por declaración (`int a, b;` no vale) y `new int[2][]` sin inicializador no vale. Para lo segundo,
el profesor acepta las dos variantes.

**Pendiente abierto:** que la corrección de la Etapa 2 confirme el criterio de `new int[2][]`
(`CONVENCIONES-CATEDRA.md` §4). Si la entrega sale aprobada sin nada que corregir, queda
confirmado y no se toca nada; si no, el cambio es de gramática y sigue el circuito de la Etapa 2.
Falta que el usuario avise el resultado.

## Etapa 3 — Analizador Semántico, Parte I: TERMINADA

Entrega 09/10/2026; la anticipada vence el 07/10 a las 16:00. Corre aislada con `--etapa=3`, que
es también la etapa por defecto, y su corpus está en la raíz de `compilador/resources/`
(`mvn test -Pfase-semantico`). Tabla de símbolos estática,
construida con acciones de una EDT sobre la gramática de la Etapa 2, y chequeo de declaraciones en
dos pasadas más la consolidación. Sin logros de código: sólo Entrega Anticipada, Imbatibilidad y
Kudos.

| | |
|---|---|
| Controles | 21, más el constructor por defecto |
| Corpus | 90 casos con error, 41 correctos y los 8 de la cátedra |
| Robustez | el compilador completo corre sin caerse sobre los ~39.600 fuentes de las etapas anteriores |
| Informe | `etapas/etapa-3/entregables/` |

Las resoluciones que esperan confirmación del profesor están en [`dudas.md`](dudas.md). Todas están
implementadas; si una respuesta es distinta, se ajusta con un caso de test.

## Etapa 4: SIN EMPEZAR

Sin especificación publicada. Queda pendiente todo lo que se chequea en los cuerpos, que la Etapa 2
dejó pasar a propósito y la Etapa 3 no mira:
- resolver los nombres contra su declaración;
- que el lado izquierdo de un `=` y el operando de `++`/`--` sean variables;
- que una sentencia-expresión sea una asignación o una llamada;
- la compatibilidad de tipos y la aridad de las llamadas;
- las variables locales repetidas;
- qué inicializa `int x, y, z = 10;` (en Java, sólo `z`);
- que el inicializador de un arreglo coincida con sus dimensiones;
- la precedencia de los operadores binarios, que la gramática de la cátedra pone en un solo nivel;
- el rango de los literales `float`;
- las expresiones que inicializan atributos.

La sección B de [`dudas.md`](dudas.md) le consulta todo esto al profesor.

## Etapa 5: SIN EMPEZAR
