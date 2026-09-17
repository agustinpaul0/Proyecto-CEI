#!/usr/bin/env python3
"""Compara el simulador de la gramática contra la expectativa de cada caso del corpus.

    python3 herramientas/comparar_con_gramatica.py                    # el corpus entero
    python3 herramientas/comparar_con_gramatica.py \'ruta/*.java\'      # otro conjunto

El corpus tiene su expectativa escrita en cada caso, y el analizador en Java la
verifica dentro de `mvn verify`. Esto cierra el triángulo con el TERCER
reconocedor: el simulador del Esquema Simple, escrito en Python sobre la
gramática. Que los tres coincidan —mismo veredicto y, cuando rechazan, el mismo
token— es lo que sostiene que el analizador reconozca EXACTAMENTE el lenguaje que
la gramática define, y no uno parecido.

DIVERGENCIAS DECLARADAS
-----------------------
El analizador es deliberadamente MÁS restrictivo que la gramática en los cuatro
chequeos que una gramática libre de contexto no puede expresar. La gramática
acepta y el analizador rechaza; nunca al revés. Se reconocen por el mensaje del
caso, no por que el fuente contenga una flecha.
"""
import concurrent.futures
import glob
import os
import re
import subprocess
import sys

sys.path.insert(0, "herramientas")
from aceptar_fuente import aceptar_con_posicion, tokenizar  # noqa: E402
from primeros import analizar, parsear  # noqa: E402

GRAMATICA = "gramatica/minijava-ll1-final.g"
JAR = "target/Compilador.jar"

# Los dos chequeos que el analizador hace y la gramática no puede expresar. Un
# caso que los ejercita declara el motivo en el mensaje, así que se reconocen por
# ahí y no por un patrón sobre el fuente, que amnistiaba de más.
MOTIVOS_DECLARADOS = (
    "un parametro de lambda tiene que ser un identificador",
    "una lambda no puede ser operando de",
)


def divergencia_declarada(salida_java):
    for motivo in MOTIVOS_DECLARADOS:
        if motivo in salida_java:
            return motivo
    return None


def veredicto_de_java(ruta):
    salida = subprocess.run(["java", "-jar", JAR, ruta],
                            capture_output=True, text=True).stdout
    return ruta, salida


def main():
    patron = sys.argv[1] if len(sys.argv) > 1 else "resources/*Errores/*.java"
    with open(GRAMATICA, encoding="utf-8") as archivo:
        g = parsear(archivo.read())
    vaciables, primeros_nt, _ = analizar(g)

    rutas = sorted(glob.glob(patron))
    # Una JVM por fuente sobre un corpus de miles tarda demasiado en serie.
    hilos = min(32, (os.cpu_count() or 4) * 4)
    with concurrent.futures.ThreadPoolExecutor(max_workers=hilos) as ejecutor:
        salidas = dict(ejecutor.map(veredicto_de_java, rutas))

    comparados = fallos = declaradas = salteados = 0
    for ruta in rutas:
        tokens, errores_lexicos = tokenizar(ruta)
        if errores_lexicos:
            salteados += 1
            continue

        ok_gramatica, motivo, posicion = aceptar_con_posicion(g, vaciables, primeros_nt, tokens)
        salida = salidas[ruta]
        ok_java = "[SinErrores]" in salida
        primera = next((l for l in salida.splitlines() if l.startswith("[")), "(sin salida)")
        comparados += 1

        if ok_gramatica and not ok_java:
            if divergencia_declarada(salida):
                declaradas += 1
                continue
            fallos += 1
            print(f"DIVERGE {ruta}: la gramatica acepta y el analizador rechaza")
            print(f"        java: {primera}")
        elif ok_java and not ok_gramatica:
            fallos += 1
            print(f"DIVERGE {ruta}: el analizador acepta y la gramatica rechaza")
            print(f"        gramatica: {motivo}")
        elif not ok_gramatica:
            codigo = f"[Error:{posicion[0]}|{posicion[1]}]"
            if codigo != primera:
                fallos += 1
                print(f"DIVERGE {ruta}: rechazan los dos, pero en tokens distintos")
                print(f"        gramatica={codigo}  java={primera}")

    print()
    print(f"{comparados} fuentes comparadas, {declaradas} divergencias declaradas, "
          f"{fallos} sin declarar, {salteados} salteadas por errores lexicos")
    print("EL ANALIZADOR Y LA GRAMATICA COINCIDEN." if not fallos
          else f"{fallos} DIVERGENCIA(S) NO DECLARADA(S).")
    return fallos


if __name__ == "__main__":
    sys.exit(main())
