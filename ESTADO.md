# Estado del proyecto

Dónde está cada etapa. El detalle técnico vive en `documentacion/`; lo que indicó el profesor, en
[`CONVENCIONES-CATEDRA.md`](CONVENCIONES-CATEDRA.md).

**Última actualización:** 19/09/2026

## Etapa 1 — Analizador Léxico: TERMINADA

Entregada y corregida sin errores ni observaciones, con sus 8 logros. Corre aislada con `--etapa=1`
y su corpus (158 casos) está en `compilador/resources/lexico/` (`mvn test -Pfase-lexico`).
`--etapa=1` reconoce también `for`, `private` y `?`, que agrega la Etapa 2.

## Etapa 2 — Analizador Sintáctico: TERMINADA

Entrega 23/09/2026. Descendente recursivo predictivo LL(1), Esquema Simple de Clase 5.

| | |
|---|---|
| Gramática | 97 no terminales, 197 producciones, 59 terminales; LL(1), 2 ambigüedades declaradas |
| Corpus propio | 10.405 fuentes, 75 casos de gramática y los 8 de la cátedra |
| Corpus externo | 29.101 fuentes de Giuliano Taormina; en todos coincide el primer código |
| Suite | 26.642 tests; 98,6 % de líneas, 94,1 % de ramas; mutación 91 % |
| Informe | `etapas/etapa-2/entregables/`, 32 páginas |

Dos lecturas siguen a la base y no a Java, a sabiendas y documentadas en el informe: un atributo
por declaración (`int a, b;` no vale) y `new int[2][]` sin inicializador no vale. Para lo segundo,
el profesor acepta las dos variantes.

## Etapas 3 a 5: SIN EMPEZAR

Sin especificación publicada. Lo que la Etapa 2 les deja pendiente está en el informe (§12.2):
sobre todo, resolver nombres contra su declaración, que el lado izquierdo de un `=` y el operando de
`++`/`--` sean variables, y qué inicializa `int x, y, z = 10;` (en Java, sólo `z`).
