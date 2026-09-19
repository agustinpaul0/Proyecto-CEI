# Créditos

Todo lo que hay en esta carpeta **es de Giuliano Taormina**, compañero de la materia, que lo
compartió para que cada uno pudiera contrastar su analizador contra una gramática que no fuera la
propia. El generador que produjo los casos también es suyo.

No son material de este proyecto. Acá sólo se corre el compilador contra ellos. La carpeta tiene el
corpus ya podado; el original, tal como llegó, es `vendor/corpora.zip`.

## Qué hay

| Carpeta | Qué es | Dónde se usa |
|---|---|---|
| `base/`, `localvar/`, `fors/`, `visibility/` | generados por *fuzzing* | `CorpusExternoTest`, desde acá mismo |
| `*-manual/` | casos escritos a mano | los que coinciden están importados a `compilador/resources/` con el prefijo `ext`, y corren con el resto de la suite |

Cada corpus generado se produjo con la gramática de un logro distinto de la suya. Este compilador
tiene todos sus logros a la vez, así que el veredicto de un caso sólo vale si no depende de un
logro que su corpus no tenía.

## La poda

El zip trae 50.234 fuentes y quedan 29.101 generados y 129 escritos a mano. Se sacaron, en este
orden:

| Poda | Casos | Por qué |
|---|---:|---|
| Carpetas `lambdas/` y `lambdas-manual/` | 10.024 | MiniLambdas no está entre los logros de este proyecto. |
| Fuentes con `->` en las demás carpetas | 9.190 | La gramática de `fors`, `localvar` y `visibility` también genera lambdas: cerca del 30 % de cada una las usa. |
| Repetidos | 339 | Todos los corpus se generaron con la **misma semilla**, así que las derivaciones que no tocan el logro propio salen idénticas en varios. Queda la primera aparición, en el orden `base`, `localvar`, `fors`, `visibility`. |
| Idénticos a un caso de `compilador/resources/` | 52 | Los casos escritos a mano de una versión anterior de este corpus, que ya estaban importados. |
| Divergencias clasificadas | 1.399 | La tabla que sigue. |

La comparación es estricta: el **primer** código que emite el compilador tiene que ser el que el
caso declara. Buscarlo en cualquier parte de la salida deja pasar 358 casos en que los dos
analizadores rechazan en tokens distintos y la recuperación en Modo Pánico termina emitiendo el
código esperado más adelante.

| Divergencia | Casos | Qué pasa | Fuente | Corrige |
|---|---:|---|---|---|
| `public static public` | 747 | Su gramática admite la visibilidad a los dos lados del `static`. | `javac`: *«repeated modifier»* | él |
| Barra invertida en un literal | 480 | Su léxico cierra `'\\'` y `"a\\"`; §2.3.3 da una regla posicional y acá el literal no cierra. Se verificó reemplazando `\\` por `\t`: los 480 coinciden. | *Aspectos Léxicos* §2.3.3 | él |
| Dimensiones por declarador: `T[] y, [] x;` | 55 | Su gramática deja que cada declarador agregue dimensiones delante del nombre. | `javac`: *«<identifier> expected»* | él |
| Varios atributos por declaración: `int a, b;` | 35 | La base declara un atributo por `<Atributo>`; el logro de declaradores múltiples es el de variables locales. Java sí lo admite. | gramática base | él, salvo que el profesor diga otra cosa |
| Corpus generado sin un logro | 29 | `base` y `localvar` no tienen Visibilidad Mejorada, Variables Locales Clásicas ni Inicialización de Arreglos; este compilador sí. | alcance | nadie |
| Asignación anidada | 20 | Admite un `=` en el operando derecho del ternario o con un ternario como lado izquierdo. | JLS §15.25 y §15.26, `javac`: *«unexpected type»* | él |
| Paréntesis vacíos: `( )` | 3 | Restos de la lambda sin parámetros tras una mutación. | MiniLambdas fuera de alcance | nadie |
| `new int[2][]` sin inicializador | 2 | Sin inicializador, la base exige tamaño en toda dimensión. Java lo admite. | gramática base | él, salvo que el profesor diga otra cosa |
| Diamante al crear arreglo: `new C<>[5]` | 2 | Su gramática lo deriva. | Pautas: el diamante es sólo al instanciar una clase. JLS §15.10.1 y §15.9: `<>` sólo existe en `TypeArgumentsOrDiamond`, que es de la creación de instancias; `javac` (`--release` 8 a 25): *«cannot create array with '<>'»* | él |
| Genérico al instanciar: `new C<T>()` en `base` | 1 | Su gramática base no lo deriva; la de la cátedra sí. | gramática base | él |
| Escritos a mano | 25 | Revisados uno por uno: las mismas categorías, más los `BaseRechaza*` escritos para su gramática sin logros. | — | — |

**Ninguna divergencia escondía un defecto propio.**
