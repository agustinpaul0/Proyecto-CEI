#!/usr/bin/env bash
# Hook Stop: corre la suite al terminar el turno, sólo si hubo cambios en el código.
# La regla del proyecto es que los tests importan más que el código (AGENTS.md §1);
# esto la hace cumplir el harness en vez de depender de que el agente se acuerde.
set -uo pipefail

raiz="${CLAUDE_PROJECT_DIR:-$(git rev-parse --show-toplevel)}"

if git -C "$raiz" diff --quiet HEAD -- compilador/src 2>/dev/null; then
  exit 0   # nada tocado bajo compilador/src, no hay nada que verificar
fi

cd "$raiz/compilador" || exit 0
salida="$(mvn -q test -Dvolcado=off 2>&1)"
if [ $? -ne 0 ]; then
  echo "La suite falló tras los cambios en compilador/src:" >&2
  echo "$salida" | tail -40 >&2
  exit 2   # exit 2 devuelve el stderr al agente para que lo resuelva
fi
