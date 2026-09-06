---
description: Regenera el PDF del informe entregable
argument-hint: "[número de etapa, por defecto 1]"
---

Etapa: $1 (si está vacío, 1).

1. `cd etapas/etapa-$1/entregables/informe && latexmk -pdf informe.tex`
2. Si LaTeX tira warnings de referencias o de índice, corré `latexmk` otra vez antes de dar
   el PDF por bueno.
3. Copiá el resultado al nombre de entrega que ya usa la carpeta
   (`etapas/etapa-$1/entregables/Informe-Etapa$1-*.pdf`).
4. Confirmá que el PDF respeta lo que fija `documentacion/proyecto/comandos-compilacion-y-entrega.md`: paleta monocromática,
   portada minimalista, índice.

No commitees los auxiliares de LaTeX (`.aux`, `.log`, `.fls`, `.fdb_latexmk`, `.out`, `.toc`) —
los cubre el `.gitignore` de la carpeta del informe.
