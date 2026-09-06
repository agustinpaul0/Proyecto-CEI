# CLAUDE.md

La memoria operativa de este proyecto vive en `AGENTS.md`. Se importa entera acá abajo.

Antes de tocar nada, leer también
[`documentacion/proyecto/plan-de-accion-etapa-2.md`](documentacion/proyecto/plan-de-accion-etapa-2.md):
es el estado vivo de la etapa activa —qué se hizo, qué falta, qué leer y qué consultar— y se
actualiza al cerrar cada sesión de trabajo.

Precedencia ante conflicto: `AGENTS.md` y `documentacion/proyecto/*.md` (las normas de este
proyecto) ganan siempre sobre cualquier skill de `.claude/skills/`, que son genéricas y de
terceros.

Toda la documentación vive bajo `documentacion/`, organizada por fase del compilador:
`lexico/` y `sintactico/` para lo propio de cada etapa, `compartido/` para lo que las cruza
(testing, manejo de errores, manejador de archivo), `proyecto/` para las normas de trabajo, y
`minijava/` para los PDF de la cátedra que describen el lenguaje.
[`documentacion/README.md`](documentacion/README.md) es el índice.

@AGENTS.md
