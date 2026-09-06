# Documentación compartida entre etapas

Lo que **no pertenece a una sola fase del compilador**. Vive acá para que no haya que repetirlo ni
buscarlo en dos lugares.

| Documento | Qué responde |
|---|---|
| [`estrategia-de-testing.md`](estrategia-de-testing.md) | Qué tipo de test corresponde a cada cosa, la taxonomía de errores, el fuzzing, el oráculo externo `javac`, y las reglas de trabajo al escribir tests. |
| [`manejo-de-errores.md`](manejo-de-errores.md) | Cómo se detecta, reporta y recupera un error: los dos formatos de reporte, a qué posición apunta el `^`, y las reglas operativas de `ExcepcionLexica`, `PosicionLexica` y `FormateadorDeError`. |
| [`manejador-de-archivo-eficiente.md`](manejador-de-archivo-eficiente.md) | Por qué el `SourceManager` que provee la cátedra no alcanza, qué hace el reemplazo, y el costo de ubicar un error. |

## Lo específico de cada etapa está en su carpeta

El mecanismo de reporte es común, pero **qué se considera un error** no lo es:

- errores léxicos → [`../lexico/decisiones-de-deteccion-de-errores.md`](../lexico/decisiones-de-deteccion-de-errores.md)
- errores sintácticos → [`../sintactico/decisiones-de-deteccion-de-errores.md`](../sintactico/decisiones-de-deteccion-de-errores.md)
