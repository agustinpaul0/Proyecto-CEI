---
description: Corre la suite de tests del compilador
argument-hint: "[completo | resumen | off | mutacion | cobertura]"
---

Modo pedido: $1 (si está vacío, usá `completo`).

Corré, desde `compilador/`, el comando que corresponda según `documentacion/proyecto/comandos-compilacion-y-entrega.md`:

- `completo` → `mvn test`
- `resumen`  → `mvn test -Dvolcado=resumen`
- `off`      → `mvn test -Dvolcado=off`
- `cobertura`→ `mvn verify` (incluye el umbral de JaCoCo)
- `mutacion` → `mvn -Pmutacion verify` (los mutantes sobrevivientes son tests faltantes).
  **`verify`, no `test`**: PIT está atado a la fase `verify` en el `pom.xml`, así que con `test`
  corre la suite normal, no muta nada y termina en verde sin haber analizado un solo mutante.

Después reportá el resultado sin adornarlo: si algo falla, mostrá la salida real del fallo e
investigá la causa antes de proponer un fix. Una regresión sin explicar no cuenta como suite
verde (`documentacion/proyecto/flujo-de-trabajo.md`).
