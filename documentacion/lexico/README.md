# Etapa 1 — Analizador Léxico

Documentación del **Analizador Léxico** de MiniJava y del Módulo Principal que lo consume por
consola. Etapa entregada y corregida, sin errores ni observaciones.

El analizador toma un archivo fuente y produce la secuencia de sus componentes léxicos, un token
por línea, en el formato que fija la cátedra: `(nombreToken,lexema,nroLínea)`.

## Documentos

| Documento | Qué responde |
|---|---|
| [`arquitectura-del-analizador-lexico.md`](arquitectura-del-analizador-lexico.md) | Qué piezas lo componen, cómo se comunican y qué pasa de punta a punta al ejecutarlo. Incluye las reglas operativas para quien modifica el código. **Empezar acá.** |
| [`especificacion-lexica-de-minijava.md`](especificacion-lexica-de-minijava.md) | La expresión regular exacta de los 56 tokens, sus autómatas, y qué queda deliberadamente afuera. |
| [`patron-state-metodo-por-estado.md`](patron-state-metodo-por-estado.md) | Cómo se implementó la técnica *Método por Estado* que exige la cátedra, pieza por pieza, con trazados completos. |
| [`decisiones-de-diseno.md`](decisiones-de-diseno.md) | Por qué está construido así: la técnica obligatoria, las decisiones estructurales y las de proceso. |
| [`decisiones-de-logica.md`](decisiones-de-logica.md) | Qué lenguaje reconoce exactamente: cada caso ambiguo, la alternativa descartada y su fuente. Incluye dónde MiniJava diverge de Java a propósito. |
| [`decisiones-de-deteccion-de-errores.md`](decisiones-de-deteccion-de-errores.md) | Qué es un error léxico, dónde se detecta, a qué posición apunta el reporte y cómo se recupera el análisis. |
| [`resolucion-de-logros.md`](resolucion-de-logros.md) | Los 8 logros de la etapa: qué exige cada uno, dónde está implementado y cómo verificarlo. |

## Ver también

- [`../compartido/manejo-de-errores.md`](../compartido/manejo-de-errores.md) — el mecanismo de
  reporte, común a todas las etapas.
- [`../compartido/manejador-de-archivo-eficiente.md`](../compartido/manejador-de-archivo-eficiente.md)
  — por qué el `SourceManager` provisto no alcanza y qué hace el reemplazo.
- [`../minijava/`](../minijava/README.md) — «Aspectos Léxicos MiniJava 2026», el documento fuente.
