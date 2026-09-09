#!/usr/bin/env python3
"""Genera el oráculo JavaCC desde la gramática del proyecto.

    python3 herramientas/gramatica_a_javacc.py gramatica/minijava-ll1-logros.g \
            --logros todos > herramientas/fuzz/MiniJavaParser.jj

El parser que sale de acá lee uno o varios fuentes y, por cada uno, imprime
`<ruta>\\t[SinErrores]` o `<ruta>\\t[Error:lexema|linea]`. Es un TERCER
reconocedor de la misma gramática, independiente del analizador en Java y del
simulador en Python: sirve para etiquetar los casos negativos que genera el
fuzzer y para cruzar veredictos con los otros dos.

El lexema del EOF es `$`, como pide Pautas slide 26.
"""
import argparse
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import gramaticas_externas as externas  # noqa: E402
from primeros import EPSILON, parsear  # noqa: E402

CABECERA = """options {
    STATIC = false;
    LOOKAHEAD = 1;
    FORCE_LA_CHECK = false;
}

PARSER_BEGIN(MiniJavaParser)

import java.io.*;
import java.nio.charset.StandardCharsets;

// GENERADO por herramientas/gramatica_a_javacc.py. No editar a mano.
public class MiniJavaParser {

    public static void main(String[] args) throws Exception {
        for (String ruta : args) {
            System.out.println(ruta + "\\t" + analizar(ruta));
        }
    }

    private static String analizar(String ruta) {
        try (Reader lector = new InputStreamReader(new FileInputStream(ruta),
                StandardCharsets.UTF_8)) {
            MiniJavaParser parser = new MiniJavaParser(lector);
            parser.inicial();
            return "[SinErrores]";
        } catch (ParseException e) {
            Token encontrado = e.currentToken.next;
            if (encontrado == null) {
                encontrado = e.currentToken;
            }
            String lexema = encontrado.kind == 0 ? "$" : encontrado.image;
            return "[Error:" + lexema + "|" + encontrado.beginLine + "]";
        } catch (TokenMgrError e) {
            return "[ErrorLexico]";
        } catch (IOException e) {
            return "[NoSePudoLeer]";
        }
    }
}

PARSER_END(MiniJavaParser)
"""


def parser_javacc(g):
    agrupadas = {}
    orden = []
    for izquierda, cuerpo in g.producciones:
        if izquierda not in agrupadas:
            agrupadas[izquierda] = []
            orden.append(izquierda)
        agrupadas[izquierda].append(cuerpo)

    bloques = []
    for izquierda in orden:
        alternativas = []
        for cuerpo in agrupadas[izquierda]:
            piezas = []
            for simbolo in cuerpo:
                if simbolo == EPSILON:
                    continue
                piezas.append(externas.nombre_de_no_terminal(simbolo) + "()"
                              if g.es_no_terminal(simbolo)
                              else f"<{externas.nombre_de_regla(simbolo)}>")
            alternativas.append(" ".join(piezas) if piezas else "{}")
        cuerpo_java = "\n    | ".join(alternativas)
        bloques.append(f"void {externas.nombre_de_no_terminal(izquierda)}() :\n"
                       f"{{}}\n{{\n    {cuerpo_java}\n}}")
    return "\n\n".join(bloques)


def main():
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument("gramatica")
    ap.add_argument("--logros", default="todos")
    args = ap.parse_args()

    if args.logros == "todos":
        logros = {"visibilidad", "atrinit", "varclasica", "for", "ternario",
                  "posfijo", "lambda", "arrinit", "generico", "float"}
    elif args.logros == "ninguno":
        logros = set()
    else:
        logros = {nombre.strip() for nombre in args.logros.split(",") if nombre.strip()}

    g = parsear(Path(args.gramatica).read_text(encoding="utf-8"), logros=logros)
    print(CABECERA)
    print(externas.lexer_javacc(logros))
    print(parser_javacc(g))


if __name__ == "__main__":
    main()
