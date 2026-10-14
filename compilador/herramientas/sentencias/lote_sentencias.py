#!/usr/bin/env python3
"""Escribe el corpus a mano de la Etapa 4 en resources/sinErrores y conErrores.

    python3 herramientas/sentencias/lote_sentencias.py

Los casos viven en los módulos casos_*.py; cada uno trae su expectativa
predicha a mano (ver catalogo.py). Reescribe sólo los archivos de esos casos y
falla si dos casos se llaman igual.
"""
import importlib
import sys
from pathlib import Path

AQUI = Path(__file__).resolve().parent
sys.path.insert(0, str(AQUI))

import catalogo  # noqa: E402

MODULOS = ["casos_operadores", "casos_asignacion", "casos_nombres", "casos_llamadas",
           "casos_creacion_y_arreglos", "casos_sentencias", "casos_genericidad",
           "casos_contexto_y_orden"]

RECURSOS = AQUI.parent.parent / "resources"


def main():
    for modulo in MODULOS:
        importlib.import_module(modulo)
    nombres = [caso.nombre for caso in catalogo.CASOS]
    repetidos = {n for n in nombres if nombres.count(n) > 1}
    if repetidos:
        raise SystemExit(f"Casos repetidos: {sorted(repetidos)}")
    for caso in catalogo.CASOS:
        carpeta = RECURSOS / ("conErrores" if caso.con_errores() else "sinErrores")
        (carpeta / f"{caso.nombre}.java").write_text(caso.contenido(), encoding="utf-8")
    con = sum(c.con_errores() for c in catalogo.CASOS)
    print(f"{len(catalogo.CASOS)} casos: {len(catalogo.CASOS) - con} correctos y {con} con errores")


if __name__ == "__main__":
    main()
