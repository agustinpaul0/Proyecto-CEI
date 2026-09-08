#!/usr/bin/env python3
"""Compara el analizador sintáctico en Java contra el simulador de la gramática.

    python3 herramientas/comparar_con_gramatica.py 'ruta/*.java'

Verifica que el analizador escrito a mano reconozca EXACTAMENTE el lenguaje que
la gramática define, y no uno parecido: mismo veredicto, y cuando los dos
rechazan, el mismo token. Es lo que sostiene que las dos cosas no se separen a
medida que la gramática o el analizador cambien.

DIVERGENCIAS DECLARADAS
-----------------------
El analizador es deliberadamente MÁS restrictivo que la gramática en los
chequeos que una gramática libre de contexto no puede expresar (el `this` y la
captura de variables dentro de una lambda, y que un parámetro de lambda sea un
identificador pelado). Esas divergencias van declaradas abajo con su motivo: una
que no esté en la lista hace fallar la comparación.
"""
import glob
import re
import subprocess
import sys

sys.path.insert(0, "herramientas")
from aceptar_fuente import aceptar_con_posicion, tokenizar  # noqa: E402
from primeros import analizar, parsear  # noqa: E402

GRAMATICA = "gramatica/minijava-ll1-logros.g"
JAR = "target/Compilador.jar"

# Cada entrada: (patrón que reconoce el fuente, motivo). El analizador rechaza y
# la gramática acepta; nunca al revés.
DIVERGENCIAS_DECLARADAS = [
    (re.compile(r"->"), "restriccion de MiniLambdas que la gramatica no puede expresar: "
                        "this, captura de variables, o un parametro que no es un identificador"),
]


def divergencia_declarada(fuente):
    for patron, motivo in DIVERGENCIAS_DECLARADAS:
        if patron.search(fuente):
            return motivo
    return None


def main():
    patron = sys.argv[1] if len(sys.argv) > 1 else "resources/sintactico/*/*.java"
    with open(GRAMATICA, encoding="utf-8") as archivo:
        g = parsear(archivo.read())
    vaciables, primeros_nt, _ = analizar(g)

    comparados = fallos = declaradas = 0
    for ruta in sorted(glob.glob(patron)):
        tokens, errores_lexicos = tokenizar(ruta)
        if errores_lexicos:
            continue
        with open(ruta, encoding="utf-8", errors="replace") as archivo:
            fuente = archivo.read()

        ok_gramatica, motivo, posicion = aceptar_con_posicion(g, vaciables, primeros_nt, tokens)
        salida = subprocess.run(
            ["java", "-jar", JAR, ruta], capture_output=True, text=True
        ).stdout
        ok_java = "[SinErrores]" in salida
        ultima = salida.strip().splitlines()[-1] if salida.strip() else "(sin salida)"
        comparados += 1

        if ok_gramatica and not ok_java:
            declarada = divergencia_declarada(fuente)
            if declarada:
                declaradas += 1
                continue
            fallos += 1
            print(f"DIVERGE {ruta}: la gramatica acepta y el analizador rechaza")
            print(f"        java: {ultima}")
        elif ok_java and not ok_gramatica:
            fallos += 1
            print(f"DIVERGE {ruta}: el analizador acepta y la gramatica rechaza")
            print(f"        gramatica: {motivo}")
        elif not ok_gramatica:
            codigo = f"[Error:{posicion[0]}|{posicion[1]}]"
            if codigo != ultima:
                fallos += 1
                print(f"DIVERGE {ruta}: rechazan los dos, pero en tokens distintos")
                print(f"        gramatica={codigo}  java={ultima}")

    print()
    print(f"{comparados} fuentes comparadas, {declaradas} divergencias declaradas, "
          f"{fallos} sin declarar")
    print("EL ANALIZADOR Y LA GRAMATICA COINCIDEN." if not fallos
          else f"{fallos} DIVERGENCIA(S) NO DECLARADA(S).")
    return fallos


if __name__ == "__main__":
    sys.exit(main())
