---
description: Relee el material de una etapa como imágenes antes de diseñar nada
argument-hint: "[número de etapa, p. ej. 2]"
---

Etapa: $1

Aplicá la regla de `AGENTS.md` §0 antes de escribir una sola línea de diseño o de código:

1. Listá todo el material de la etapa: `ls etapas/etapa-$1/` y los PDFs técnicos de MiniJava
   en `documentacion/minijava-tecnico/`.
2. Leé **cada PDF como imágenes, página por página** con la herramienta Read. **No** uses
   `pdftotext` ni ninguna extracción de texto: los autómatas, las capturas de código con globos
   de explicación y el orden real de las anotaciones son la mitad de la información técnica y
   la extracción de texto los pierde.
3. Leé también `etapas/etapa-$1/README.md` si existe.
4. Recién entonces resumime: qué pide la consigna, qué logros hay en juego, qué decisiones
   quedan ambiguas y cuáles caen bajo el protocolo de `guias/investigacion.md`.

No propongas arquitectura ni empieces a implementar en este mismo turno: terminá con el resumen.
