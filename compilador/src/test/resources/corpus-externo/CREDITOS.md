# Créditos

Todo lo que hay en esta carpeta **es de Giuliano Taormina**, compañero de la materia, que lo
compartió para que cada uno pudiera contrastar su analizador contra una gramática que no fuera la
propia. El generador que produjo los casos también es suyo.

No son material de este proyecto. Acá sólo se corre el compilador contra ellos, con
`CorpusExternoTest`. La carpeta tiene el corpus ya podado.

## Qué hay

| Carpeta | Qué es |
|---|---|
| `base/`, `localvar/`, `fors/`, `visibility/` | Los casos generados por *fuzzing*, cada uno con `sinErrores/` y `conErrores/`. |

Los casos que escribió a mano venían en carpetas `*-manual/`. Los que coinciden están importados a
`compilador/resources/` con el prefijo `ext`, y corren con el resto del corpus.

Cada corpus generado se produjo con la gramática de un logro distinto de la suya. Este compilador
tiene todos sus logros a la vez, así que el veredicto de un caso sólo vale si no depende de un
logro que su corpus no tenía.
