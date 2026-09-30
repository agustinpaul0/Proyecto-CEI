#!/usr/bin/env python3
"""Corre la gramática LL(1) sobre un fuente MiniJava real.

Tokeniza con el analizador léxico de la Etapa 1 (el jar del compilador) y le da
la secuencia de tokens al simulador del Esquema Simple. Permite validar la
gramática contra fuentes de verdad —incluidos los casos de la cátedra— antes de
que exista el analizador sintáctico en Java.

    python3 herramientas/aceptar_fuente.py resources/catedra/sintactico/sinErrores/*.java
"""
import re
import subprocess
import sys

sys.path.insert(0, "herramientas")
from primeros import aceptar_detallado, analizar, parsear  # noqa: E402


GRAMATICA = "gramatica/minijava-ll1-final.g"
JAR = "target/Compilador.jar"
# --etapa=1 porque el jar corre el sintáctico por defecto y acá se lo quiere sólo
# como tokenizador.

# Los tokens de lexema fijo se identifican por su propio lexema, que es el
# terminal de la gramática. Sólo los de lexema variable necesitan traducción.
POR_TIPO = {
    "idClase": "idClase",
    "idGen": "idGen",
    "idMV": "idMetVar",
    "intLiteral": "intLiteral",
    "charLiteral": "charLiteral",
    "floatLiteral": "floatLiteral",
    "litString": "stringLiteral",
    "EOF": "eof",
}
LINEA = re.compile(r"^\((?P<tipo>[^,]+),(?P<lexema>.*),(?P<linea>\d+)\)$")


def tokenizar(ruta):
    salida = subprocess.run(
        ["java", "-jar", JAR, "--etapa=1", ruta],
        capture_output=True,
        text=True,
    ).stdout
    tokens, errores = [], []
    for renglon in salida.splitlines():
        m = LINEA.match(renglon.strip())
        if m:
            tipo, lexema, linea = m.group("tipo"), m.group("lexema"), m.group("linea")
            tokens.append((POR_TIPO.get(tipo, lexema), lexema, int(linea)))
        elif renglon.startswith("[Error:"):
            errores.append(renglon.strip())
    return tokens, errores


def expectativa(ruta):
    """Los casos de la cátedra declaran el resultado esperado en su 1ra línea."""
    primera = open(ruta, encoding="utf-8", errors="replace").readline().strip()
    return primera[3:] if primera.startswith("///") else "(sin expectativa)"


def aceptar_con_posicion(g, vaciables, primeros_nt, tokens):
    """Devuelve además el lexema y la línea del token que provocó el rechazo,
    para poder compararlo con el código [Error:lexema|linea] que pide la
    cátedra."""
    ok, motivo, indice = aceptar_detallado(
        g, vaciables, primeros_nt, [t[0] for t in tokens]
    )
    if ok:
        return True, motivo, None
    indice = min(indice, len(tokens) - 1)
    return False, motivo, (tokens[indice][1], tokens[indice][2])


def main():
    with open(GRAMATICA, encoding="utf-8") as archivo:
        g = parsear(archivo.read())
    vaciables, primeros_nt, _ = analizar(g)

    fallos = 0
    for ruta in sys.argv[1:]:
        esperado = "SI" if "/sinErrores/" in ruta else "NO"
        tokens, errores_lexicos = tokenizar(ruta)
        nombre = ruta.rsplit("/", 1)[-1]
        if errores_lexicos:
            print(f"  ---   {nombre:24} el fuente tiene errores LÉXICOS: {errores_lexicos[0]}")
            continue
        ok, motivo, posicion = aceptar_con_posicion(
            g, vaciables, primeros_nt, tokens
        )
        obtenido = "SI" if ok else "NO"
        if obtenido == esperado:
            if ok:
                print(f"  ok    {nombre:24} acepta")
            else:
                lexema, linea = posicion
                codigo = f"[Error:{lexema}|{linea}]"
                esperada = expectativa(ruta)
                marca = "==" if esperada == codigo else f"!= esperaba {esperada}"
                print(f"  ok    {nombre:24} rechaza  {codigo} {marca}")
                print(f"        {motivo}")
        else:
            fallos += 1
            print(f"  FALLA {nombre:24} esperado={esperado} obtenido={obtenido} -- {motivo}")
    print()
    print("TODOS LOS CASOS PASAN." if not fallos else f"{fallos} CASO(S) FALLAN.")
    return fallos


if __name__ == "__main__":
    sys.exit(main())
