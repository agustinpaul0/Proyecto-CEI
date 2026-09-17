# Créditos

Todo lo que hay en `resources/` **es de Giuliano Taormina**, compañero de la materia, que lo
compartió para que cada uno pudiera contrastar su analizador contra una gramática que no fuera la
propia. El generador que produjo los casos también es suyo.

No son material de este proyecto. Acá sólo se corre el compilador contra ellos y se clasifica cada
divergencia.

## Qué hay

En `resources/parser/corpora/` conviven dos cosas distintas:

| Carpeta | Qué es | Dónde se usa |
|---|---|---|
| `base/`, `lambdas/`, `localvar/` | generados por *fuzzing* | `CorpusExternoTest`, desde acá mismo |
| `base-manual/`, `lambdas-manual/`, `localvar-manual/` | 78 casos escritos a mano | ya están importados a `compilador/resources/` con el prefijo `ext`, y corren con el resto de la suite |

## La poda

Los tres corpus generados traían 30.000 casos entre los tres y hoy quedan 28.089. Se sacaron:

- **~1.854 por la barra invertida dentro de un literal.** *Aspectos Léxicos* §2.3.3 da una regla
  posicional —la comilla de cierre nunca está antecedida por `\`— y no una de apareamiento, así que
  acá el literal no cierra; su generador asume la semántica de Java. Era una sola discrepancia
  repetida miles de veces.
- **57 más, revisados uno por uno**: genéricos al instanciar (`new Clase<Arg>()`, que la gramática
  de la cátedra genera y la suya no), el logro de Visibilidad Mejorada (sus corpus `base` y
  `localvar` se generaron sin él), y un puñado que las dos implementaciones rechazan en distinto
  token porque su gramática cubre menos construcciones.

**Ninguno escondía un defecto propio**: se verificó caso por caso qué construcción lo explicaba.

De los 28.089 que quedan coinciden 28.086. Las 3 divergencias son todas `(x -> y).m()`, que es una
decisión de lenguaje declarada y abierta.
