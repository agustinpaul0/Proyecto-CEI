# Workflow del agente

Índice: [`../AGENTS.md`](../AGENTS.md).

## Antes de implementar algo

1. Identificar qué fase del compilador toca.
2. **Si el comportamiento no está ya resuelto en [`convenciones-catedra.md`](convenciones-catedra.md)
   o [`especificacion-lexica.md`](especificacion-lexica.md), seguir el
   [protocolo de investigación](investigacion.md) antes de escribir código** — MiniJava primero, Java
   verificado contra la especificación después, informar al usuario, recién ahí implementar.
3. Identificar qué tipo(s) de test corresponde(n) según [`testing.md`](testing.md).
4. Revisar tests existentes relacionados.
5. Implementar.
6. Agregar/actualizar tests.
7. Correr los tests relevantes; correr la suite completa cuando corresponda.
8. Investigar y explicar cualquier regresión.

**No declarar una tarea terminada sin haber corrido los tests relevantes, ni con un fallo sin
explicar.**

## Ante un bug encontrado

Reproducirlo → escribir el test de regresión → confirmar que falla antes del fix (cuando sea
posible) → implementar el fix → confirmar que el test pasa → correr tests relacionados → correr la
suite completa cuando corresponda.

## Regla vital al terminar cualquier feature

Ninguna tarea se considera terminada hasta que se cumplen **las tres** condiciones:

1. **Deuda técnica en 0**: sin TODOs colgados, sin tests deshabilitados o comentados, sin
   implementaciones a medias, sin código muerto dejado "por las dudas". Si algo queda pendiente a
   propósito (por alcance, no por falta de tiempo), se anota explícitamente en `todos/todos.md`, no
   se deja implícito en el código.
2. **Documentación actualizada**: `AGENTS.md`, los archivos de `agents/` y
   `documentacion/*.md` reflejan el estado real del código en ese momento — ninguno puede describir
   algo que ya cambió o algo que todavía no existe. Esto se verifica en cada sesión antes de dar una
   tarea por cerrada, no sólo al final del proyecto.
3. **El usuario fue informado** de cualquier decisión tomada por el [protocolo de
   investigación](investigacion.md) (MiniJava silente → Java verificado) — no alcanza con que quede
   sólo en un comentario de código o en el mensaje de commit.

El historial de decisiones y el calendario **no viven en estos archivos**: viven en `git log`
(mensajes de commit descriptivos) y en el repositorio de GitHub. Estos documentos describen el estado
actual del proyecto, no cómo se llegó a él.
