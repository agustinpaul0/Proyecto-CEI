# Protocolo de investigación — MiniJava primero, Java después, nunca de memoria

Regla de trabajo **obligatoria**, no una sugerencia. Se estableció después de un incidente real: se
implementó un comportamiento (repetición del marcador `u` en el escape Unicode) basándose en un
recuerdo general del JLS, sin citar la especificación real ni informar al usuario antes de programar.
El comportamiento en sí resultó correcto, pero el *proceso* estuvo mal — y separadamente, una
decisión relacionada (si `double` debía ser palabra reservada) se interpretó mal por no chequear con
el usuario antes de implementar. Esta sección existe para que no vuelva a pasar.

## El procedimiento, en orden, sin saltearse pasos

Ante **cualquier** pregunta de comportamiento léxico (o, en etapas futuras, sintáctico/semántico) que
no esté ya resuelta en `guias/convenciones-catedra.md` o `guias/especificacion-lexica.md`:

1. **Primero, ¿hay una regla escrita para esto en MiniJava?** Buscar explícitamente en los PDFs de la
   cátedra (como imágenes, `AGENTS.md` §0) y en las decisiones ya tomadas en `guias/*.md`. Si la hay,
   se usa esa regla — punto, no se sigue investigando Java.
2. **Si no la hay, investigar a fondo cómo lo hace Java real** — no recordar de memoria, **verificar
   contra una fuente primaria**: la especificación (JLS, `docs.oracle.com/javase/specs/`) vía
   `WebFetch`/`WebSearch`, o documentación oficial equivalente. "Creo que es así" no alcanza; hay que
   poder citar la sección exacta (ej. "JLS §3.10.7, verificado contra la especificación").
3. **Adecuar la implementación exactamente a eso** — ni más permisivo ni más estricto que lo que Java
   realmente hace, salvo que el paso 1 ya haya dicho algo distinto para ese caso puntual.
4. **Informar al usuario** qué se investigó, qué dice la fuente, y qué se decidió — **antes de dar la
   tarea por terminada**, con la cita concreta. No alcanza con dejarlo únicamente en un comentario de
   código o en la bitácora de un commit: tiene que quedar dicho explícitamente en la respuesta.

Si la pregunta es sobre un comportamiento que **cambia lo que ya está implementado y probado**
(no una feature nueva), preferir explicar el hallazgo y esperar confirmación antes de tocar código,
salvo que el usuario ya haya dado autorización explícita para seguir trabajando de forma autónoma
mientras no está — en ese caso, implementar vía TDD (para que el diff sea reversible y quede
documentado en el commit) y dejarlo remarcado con prioridad en la respuesta, no enterrado en el medio.

## Por qué "de memoria" no alcanza

Java tiene reglas viejas, específicas y poco intuitivas que un resumen mental fácilmente comprime mal
o completa con una regla "razonable" que no es la real. Ejemplos concretos ya encontrados en este
proyecto:

- El marcador `u` del escape Unicode se repite (`\uu0041` es válido) — fácil de no saber si no se lee
  el JLS.
- Los escapes válidos dentro de un string en Java real son un conjunto **cerrado y específico**
  (`b s t n f r " ' \` + octal 0-7), no "cualquier carácter" — algo que se podría asumir mal por
  analogía con un lenguaje más permisivo.
- `\s` (espacio) es un escape válido en Java — un detalle fácil de omitir de memoria.

Ninguno de estos se adivina razonando en abstracto "cómo sería razonable que funcione Java"; hay que
leer la especificación.

## Relación con la regla del README de la cátedra

Este protocolo es la forma operativa de aplicar la regla que la cátedra escribió explícitamente en
`etapas/etapa-1/README.md`: *"todo lo que NO esté especificado para MiniJava tiene comportamiento por
defecto igual al de Java"*. Esa frase por sí sola no dice *cómo* averiguar el comportamiento de Java
con precisión — este documento fija el procedimiento para hacerlo bien, en vez de asumir.

## Casos ya resueltos con este protocolo (ver `guias/especificacion-lexica.md` para el detalle completo)

- Escape Unicode: marcador `u` repetible (JLS §3.3).
- `float` como palabra reservada: **no** se resuelve por este protocolo — es una respuesta directa
  del profesor, no un default de Java (Java no tiene "float" como comportamiento por defecto de algo
  no especificado; es una decisión explícita de la cátedra por otra vía). `double` explícitamente
  **no** es palabra reservada, aclarado por el usuario tras una interpretación inicial equivocada.
- Escapes dentro de `litString`: restringidos al conjunto válido de Java (JLS §3.10.7) — a diferencia
  de `charLiteral`, que la cátedra hace explícitamente más permisivo (`Aspectos Lexicos MiniJava
  2026.pdf` §2.3.2: "`'\x'` con `x` cualquier carácter").

## Ver también

- [`convenciones-catedra.md`](convenciones-catedra.md) — qué SÍ está explícitamente resuelto por la
  cátedra (paso 1 de este protocolo).
- [`especificacion-lexica.md`](especificacion-lexica.md) — todas las decisiones léxicas ya tomadas,
  incluidas las resueltas con este protocolo.
