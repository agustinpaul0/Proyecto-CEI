---
description: Corre la suite de tests del compilador
argument-hint: "[completo | resumen | off | mutacion | cobertura]"
---

Modo pedido: $1 (si está vacío, usá `completo`).

Corré, desde `compilador/`, el comando que corresponda según `guias/comandos-y-entrega.md`:

- `completo` → `mvn test`
- `resumen`  → `mvn test -Dvolcado=resumen`
- `off`      → `mvn test -Dvolcado=off`
- `cobertura`→ `mvn verify` (incluye el umbral de JaCoCo)
- `mutacion` → `mvn -Pmutacion test` (los mutantes sobrevivientes son tests faltantes)

Después reportá el resultado sin adornarlo: si algo falla, mostrá la salida real del fallo e
investigá la causa antes de proponer un fix. Una regresión sin explicar no cuenta como suite
verde (`guias/workflow.md`).
