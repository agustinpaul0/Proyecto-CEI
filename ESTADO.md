# Estado del proyecto

Dónde está cada etapa. El detalle técnico vive en `documentacion/`; lo que indicó el profesor, en
[`CONVENCIONES-CATEDRA.md`](CONVENCIONES-CATEDRA.md).

**Última actualización:** 11/11/2026

## Etapa 1 — Analizador Léxico: TERMINADA

Entregada y corregida sin errores ni observaciones, con sus 8 logros: 26 puntos, el máximo. Corre aislada con `--etapa=1`
y su corpus (158 casos) está en `compilador/resources/lexico/` (`mvn test -Pfase-lexico`).
`--etapa=1` reconoce también `for`, `private` y `?`, que agrega la Etapa 2.

## Etapa 2 — Analizador Sintáctico: TERMINADA

Entregada y corregida sin observaciones, con los 12 logros perseguidos: 32 puntos, el máximo sin MiniLambdas. Descendente recursivo predictivo LL(1), Esquema Simple de Clase 5. Corre
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
el profesor acepta las dos variantes, y la corrección lo confirmó (`CONVENCIONES-CATEDRA.md` §17).

## Etapa 3 — Analizador Semántico, Parte I: TERMINADA

Entregada el 05/10/2026, dentro del plazo de la Entrega Anticipada (07/10), con el commit
`fc7a341f`, y corregida sin comentarios. Corre aislada con `--etapa=3`, y su corpus está en
`compilador/resources/declaraciones/` (`mvn test -Pfase-semantico`). Tabla de símbolos estática,
construida con acciones de una EDT sobre la gramática de la Etapa 2, y chequeo de declaraciones en
dos pasadas más la consolidación. Sin logros de código: sólo Entrega Anticipada, Imbatibilidad y
Kudos.

Puntaje: los 3 logros sin código, 12 + 3 × 2 = 18 puntos, y 76 en total en la Tabla general de
Puntos.

| | |
|---|---|
| Controles | 22, más el constructor por defecto |
| Corpus | 100 casos con error, 43 correctos y los 8 de la cátedra |
| Robustez | el compilador completo corre sin caerse sobre los ~39.600 fuentes de las etapas anteriores |
| Informe | `etapas/etapa-3/entregables/` |

El profesor contestó todas las consultas de la etapa (`CONVENCIONES-CATEDRA.md` §8 a §13). El
documento completo de semántica, publicado con la Etapa 4, prohíbe los tipos raw que la Parte I
permitía, y el profesor aclaró que un tipo que hereda sin argumento no puede declarar su propio
parámetro (el control 22). El chequeo de declaraciones sigue los dos, también con `--etapa=3`
(`documentacion/sentencias/decisiones-de-logica.md` §4).

## Etapa 4 — Analizador Semántico, Parte II: TERMINADA

Entregada dentro del plazo de la Entrega Anticipada (28/10/2026) y corregida sin comentarios.
Corre con `--etapa=4`, y su corpus está en la raíz de `compilador/resources/`
(`mvn test -Pfase-sentencias`) hasta que el de la Etapa 5 ocupe su lugar. El sintáctico arma el AST de cada cuerpo con acciones de una EDT y lo cuelga de su unidad; después de
las declaraciones y la consolidación, cada nodo se chequea por delegación. Sin logros de código: sólo
Imbatibilidad, Entrega Anticipada y Grandioso Laburo!.

Puntaje: 20 puntos, con los 3 logros acreditados, y 96 en total en la Tabla general de Puntos
(`etapas/Tabla general de Puntos.pdf`).

| | |
|---|---|
| Corpus a mano | 397 casos propios (88 correctos, 309 con error) y 126 de Santiago Salamanca |
| Corpus generado | 2.905 casos, cada uno contrastado con `javac` 25 |
| Cátedra | 10 casos: todos dan su código, con `semIICorrecto03` corregido por el profesor |
| Propiedades y fuzz | 4.500 expresiones al azar y 3.000 mutaciones |
| Robustez | el compilador completo corre sin caerse sobre los ~40.000 fuentes de las etapas anteriores |
| Informe | `etapas/etapa-4/entregables/` |

El profesor contestó todas las consultas de la etapa (`CONVENCIONES-CATEDRA.md` §1 a §7).

## Etapa 5 — Generación de código: PLANIFICADA

Entrega: 20/11/2026 a las 16:00; Entrega Anticipada, hasta el 18/11 a las 16:00, que es el
objetivo. Traduce el programa a CeIASM, el lenguaje de la CeIVM, y escribe el archivo de salida:
`java -jar Compilador.jar programa.java salida.txt`. Sin logros de código: sólo Entrega Anticipada,
Imbatibilidad y Congrats!.

El material está leído y el plan, escrito: [`etapas/etapa-5/plan.md`](etapas/etapa-5/plan.md).
Todavía no hay código.

**Pendiente abierto:** mandarle al profesor las cinco consultas de
`etapas/etapa-5/consultas/dudas.pdf`: el error si falta `main` o hay dos, el mensaje de los
errores de ejecución, el valor de los escapes, el orden de evaluación de una asignación y si dos
literales String iguales son el mismo objeto. Cada una tiene una propuesta que se implementa
mientras tanto.
