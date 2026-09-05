---
description: Checklist de deuda-cero antes de dar una tarea por terminada
---

Verificá la «regla vital al terminar cualquier feature» de `guias/workflow.md`. Una tarea no
está cerrada hasta que se cumplen **las tres** condiciones. Chequealas de verdad, no de memoria:

1. **Deuda técnica en 0**
   - `grep -rn "TODO\|FIXME\|XXX" compilador/src` → vacío.
   - `grep -rn "@Disabled\|@Ignore" compilador/src/test` → vacío.
   - Sin implementaciones a medias ni código muerto dejado «por las dudas».
   - Si algo queda pendiente a propósito (por alcance), va explícito en el commit, no implícito
     en el código.

2. **Documentación al día** — `AGENTS.md`, `guias/*.md` y `documentacion/*.md` describen el
   estado real del código *ahora*: ninguno puede describir algo que ya cambió ni algo que
   todavía no existe. Revisá los que toca el cambio y decime cuáles actualizaste.

3. **Usuario informado** de toda decisión tomada por el protocolo de `guias/investigacion.md`
   (MiniJava silente → Java verificado contra la especificación). No alcanza con dejarlo en un
   comentario de código o en el mensaje de commit.

Cerrá con `mvn verify` desde `compilador/` y reportá el resultado. Si alguna de las tres
condiciones falla, decilo explícitamente en vez de dar la tarea por terminada.
