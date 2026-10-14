#!/usr/bin/env python3
"""Genera el corpus combinatorio de la Etapa 4 y lo contrasta con javac.

    python3 herramientas/sentencias/generar_corpus_sentencias.py

Recorre matrices de tipos por contexto: la conformidad del documento §9 en la
asignación, el argumento y el retorno; los operadores del §11.2; la condición del
§12.4; el índice y el tamaño de un arreglo del §11.8. Cada celda sale con su
código exacto, que da `minijava`, un modelo de las reglas de MiniJava escrito
aparte del compilador.

Cada celda se compila además con javac 25 (OraculoJavac.java). Donde MiniJava y
Java coinciden, javac tiene que dar el mismo veredicto que el modelo; donde
difieren a propósito —char se promueve a int en Java, hay boxing, los arreglos
son covariantes, + concatena cadenas, == compara una clase con una interfaz que no
implementa—, javac tiene que dar el veredicto opuesto. Si algo no cierra, el
script falla y no escribe nada: el modelo, o el documento, está mal leído.

El corpus queda empaquetado en resources/sentencias-generado.tar.gz, que corre
CorpusGeneradoSentenciasTest.
"""
import shutil
import subprocess
import sys
import tempfile
from collections import Counter
from pathlib import Path

AQUI = Path(__file__).resolve().parent
RAIZ = AQUI.parent.parent
sys.path.insert(0, str(AQUI))
sys.path.insert(0, str(AQUI.parent))

from catalogo import Caso  # noqa: E402
from empaquetar_corpus import empaquetar  # noqa: E402

DESTINO = RAIZ / "resources" / "sentencias-generado.tar.gz"

PRELUDIO = """
interface Ix { }
interface Iy extends Ix { }
class Aa { }
class Bb extends Aa { }
class Cc extends Bb { }
class Dd implements Iy { }
class Caja<T> { }
"""

PRIMITIVOS = ["int", "char", "boolean"]
CLASES = ["String", "Object", "Aa", "Bb", "Cc", "Ix", "Iy", "Dd", "Caja<String>", "Caja<Object>", "Caja<Aa>"]
ARREGLOS = ["int[]", "int[][]", "char[]", "String[]", "Object[]", "Aa[]", "Bb[]", "Ix[]", "Caja<String>[]"]
DESTINOS = PRIMITIVOS + CLASES + ARREGLOS
NULO, VACIO = "null", "void"
ORIGENES = DESTINOS + [NULO, VACIO]

PADRES = {"Bb": ["Aa"], "Cc": ["Bb"], "Dd": ["Iy"], "Iy": ["Ix"]}


def ancestros(tipo):
    resultado = {tipo}
    for padre in PADRES.get(tipo, []):
        resultado |= ancestros(padre)
    return resultado


def es_arreglo(tipo):
    return tipo.endswith("[]")


def es_referencia(tipo):
    return tipo not in PRIMITIVOS and tipo not in (NULO, VACIO)


# --- Las reglas de MiniJava (documento §9) ----------------------------------

def conforma_minijava(origen, destino):
    if origen == VACIO:
        return False
    if origen == NULO:
        return es_referencia(destino) or destino == NULO
    if origen == destino:
        return True
    if origen in PRIMITIVOS or destino in PRIMITIVOS:
        return False
    if destino == "Object":
        return True
    if es_arreglo(origen) or es_arreglo(destino):
        return False
    return destino in ancestros(origen)


# --- Las reglas de Java, para el contraste (JLS §5.2) -----------------------

def conforma_java(origen, destino):
    if origen == VACIO:
        return False
    if origen == NULO:
        return es_referencia(destino)
    if origen == destino:
        return True
    if origen == "char" and destino == "int":
        return True
    if origen in PRIMITIVOS and destino == "Object":
        return True  # boxing (JLS §5.1.7)
    if origen in PRIMITIVOS or destino in PRIMITIVOS:
        return False
    if destino == "Object":
        return True
    if es_arreglo(origen) and es_arreglo(destino):
        base_o, base_d = origen[:-2], destino[:-2]
        return es_referencia(base_o) and es_referencia(base_d) and conforma_java(base_o, base_d)
    if es_arreglo(origen) or es_arreglo(destino):
        return False
    return destino in ancestros(origen)


OPERANDOS = ["int", "char", "boolean", "String", "Aa", "Bb", "Ix", NULO, "int[]"]
ARITMETICOS = ["+", "-", "*", "/", "%"]
RELACIONALES = ["<", ">", "<=", ">="]
LOGICOS = ["&&", "||"]
IGUALDADES = ["==", "!="]
BINARIOS = ARITMETICOS + RELACIONALES + LOGICOS + IGUALDADES


def binario_minijava(operador, izquierda, derecha):
    if operador in ARITMETICOS + RELACIONALES:
        return izquierda == derecha == "int"
    if operador in LOGICOS:
        return izquierda == derecha == "boolean"
    return conforma_minijava(izquierda, derecha) or conforma_minijava(derecha, izquierda)


def comparables_java(izquierda, derecha):
    numericos = {"int", "char"}
    if izquierda in numericos and derecha in numericos:
        return True
    if izquierda == derecha == "boolean":
        return True
    if NULO in (izquierda, derecha):
        otro = derecha if izquierda == NULO else izquierda
        return otro == NULO or es_referencia(otro)
    if not (es_referencia(izquierda) or izquierda == NULO) or not (es_referencia(derecha) or derecha == NULO):
        return False
    if conforma_java(izquierda, derecha) or conforma_java(derecha, izquierda):
        return True
    # JLS §5.5: una clase no final y una interfaz siempre se pueden convertir.
    clases_no_finales = {"Aa", "Bb", "Cc", "Dd", "Object"}
    interfaces = {"Ix", "Iy"}
    return (izquierda in clases_no_finales and derecha in interfaces) or \
        (derecha in clases_no_finales and izquierda in interfaces)


def binario_java(operador, izquierda, derecha):
    numericos = {"int", "char"}
    if operador == "+" and NULO not in (izquierda, derecha) and "String" in (izquierda, derecha):
        return True
    if operador == "+" and "String" in (izquierda, derecha) and {izquierda, derecha} == {"String", NULO}:
        return True
    if operador in ARITMETICOS + RELACIONALES:
        return izquierda in numericos and derecha in numericos
    if operador in LOGICOS:
        return izquierda == derecha == "boolean"
    return comparables_java(izquierda, derecha)


UNARIOS = ["+", "-", "!"]


def unario_minijava(operador, tipo):
    return tipo == ("boolean" if operador == "!" else "int")


def unario_java(operador, tipo):
    return tipo == "boolean" if operador == "!" else tipo in ("int", "char")


# --- Los fuentes --------------------------------------------------------------

NOMBRES = {"int": "Int", "char": "Char", "boolean": "Boolean", "String": "String", "Object": "Object",
           "Aa": "Aa", "Bb": "Bb", "Cc": "Cc", "Ix": "Ix", "Iy": "Iy", "Dd": "Dd",
           "Caja<String>": "CajaString", "Caja<Object>": "CajaObject", "Caja<Aa>": "CajaAa",
           NULO: "Null", VACIO: "Void"}
OPERADORES = {"+": "Suma", "-": "Resta", "*": "Mult", "/": "Div", "%": "Modulo", "<": "Menor",
              ">": "Mayor", "<=": "MenorIgual", ">=": "MayorIgual", "&&": "And", "||": "Or",
              "==": "Igual", "!=": "Distinto", "!": "Negacion"}


def nombre_de(tipo):
    if es_arreglo(tipo):
        return nombre_de(tipo[:-2]) + "Arreglo"
    return NOMBRES[tipo]


def valor(tipo, variable):
    """La expresión de ese tipo y el lexema donde javac ubica su error."""
    if tipo == NULO:
        return "null", "null"
    if tipo == VACIO:
        return "v()", "("
    return variable, variable


def parametro(tipo, variable):
    return [] if tipo in (NULO, VACIO) else [f"{tipo} {variable}"]


def caso(nombre, descripcion, cuerpo, valido, lexema, parametros):
    fuente = f"""
class Uso {{
    void v() {{ }}
    void m({", ".join(parametros)}) {{
{cuerpo}
    }}
}}
{PRELUDIO}"""
    if valido:
        fuente = fuente.replace(" // <-", "")
    return Caso(nombre, descripcion, fuente, None if valido else lexema)


def casos_de_conformidad():
    for origen in ORIGENES:
        for destino in DESTINOS:
            par = f"{nombre_de(origen)}A{nombre_de(destino)}"
            mini, java = conforma_minijava(origen, destino), conforma_java(origen, destino)
            expresion, lexema = valor(origen, "s")
            parametros = parametro(destino, "d") + parametro(origen, "s")
            yield (caso(f"asignacion{par}", f"{origen} a un destino {destino} (§9)",
                        f"        d = {expresion}; // <-", mini, "=", parametros), java)
            yield (caso(f"argumento{par}", f"{origen} a un parametro {destino} (§9, §11.6)",
                        f"        f({expresion}); // <-", mini, "f", parametros)
                   ._replace_fuente(f"    void f({destino} p) {{ }}\n"), java)
            yield (caso(f"retorno{par}", f"retornar {origen} donde se declara {destino} (§9, §12.5)",
                        f"        return {expresion}; // <-", mini, lexema, parametro(origen, "s"))
                   ._retorno(destino), java)


def casos_de_operadores():
    for operador in BINARIOS:
        for izquierda in OPERANDOS:
            for derecha in OPERANDOS:
                a, _ = valor(izquierda, "a")
                b, _ = valor(derecha, "b")
                parametros = parametro(izquierda, "a") + parametro(derecha, "b")
                mini, java = binario_minijava(operador, izquierda, derecha), binario_java(operador, izquierda, derecha)
                yield (caso(f"binario{OPERADORES[operador]}{nombre_de(izquierda)}{nombre_de(derecha)}",
                            f"{izquierda} {operador} {derecha} (§11.2)",
                            f"        var r = {a} {operador} {b}; // <-", mini, operador, parametros), java)
    for operador in UNARIOS:
        for tipo in OPERANDOS:
            a, _ = valor(tipo, "a")
            nombre = OPERADORES[operador] if operador == "!" else ("Mas" if operador == "+" else "Menos")
            yield (caso(f"unario{nombre}{nombre_de(tipo)}", f"{operador} sobre {tipo} (§11.2)",
                        f"        var r = {operador}{a}; // <-", unario_minijava(operador, tipo), operador,
                        parametro(tipo, "a")), unario_java(operador, tipo))


def casos_de_condiciones_e_indices():
    for tipo in ORIGENES:
        expresion, lexema = valor(tipo, "c")
        es_boolean, es_int = tipo == "boolean", tipo == "int"
        for sentencia, nombre in [("if", "If"), ("while", "While")]:
            yield (caso(f"condicion{nombre}{nombre_de(tipo)}", f"{tipo} como condicion de un {sentencia} (§12.4)",
                        f"        {sentencia} ({expresion}) ; // <-", es_boolean, lexema, parametro(tipo, "c")),
                   es_boolean)
        yield (caso(f"indice{nombre_de(tipo)}", f"{tipo} como indice de un arreglo (§11.8)",
                    f"        var x = v[{expresion}]; // <-", es_int, lexema, ["int[] v"] + parametro(tipo, "c"))
               ._sin_metodo_v(), es_int or tipo == "char")
        yield (caso(f"tamanio{nombre_de(tipo)}", f"{tipo} como tamanio de un arreglo (§11.8)",
                    f"        var x = new int[{expresion}]; // <-", es_int, lexema, parametro(tipo, "c")),
               es_int or tipo == "char")


def _reemplazar_fuente(self, metodo_extra):
    return Caso(self.nombre, self.descripcion, self.fuente.replace("    void v() { }\n", "    void v() { }\n" + metodo_extra), self.lexema)


def _retorno(self, destino):
    fuente = self.fuente.replace("    void m(", f"    {destino} m(")
    return Caso(self.nombre, self.descripcion, fuente, self.lexema)


def _sin_metodo_v(self):
    return Caso(self.nombre, self.descripcion, self.fuente.replace("    void v() { }\n", "    void v() { }\n"),
                self.lexema)


Caso._replace_fuente = _reemplazar_fuente
Caso._retorno = _retorno
Caso._sin_metodo_v = _sin_metodo_v


def contrastar_con_javac(casos, carpeta):
    resultado = subprocess.run(["java", str(AQUI / "OraculoJavac.java"), str(carpeta)],
                               capture_output=True, text=True, check=True)
    veredictos = {}
    for linea in resultado.stdout.splitlines():
        ruta, veredicto, _ = linea.split("\t")
        veredictos[Path(ruta).stem] = veredicto == "ACEPTA"
    problemas, divergencias = [], Counter()
    for caso_, java in casos:
        acepta_javac = veredictos[caso_.nombre]
        if acepta_javac != java:
            problemas.append(f"{caso_.nombre}: el modelo de Java dice {java} y javac {acepta_javac}")
        if java != (not caso_.con_errores()):
            divergencias[caso_.descripcion.split(" (")[-1]] += 1
    return problemas, sum(divergencias.values())


def main():
    casos = list(casos_de_conformidad()) + list(casos_de_operadores()) + list(casos_de_condiciones_e_indices())
    nombres = Counter(c.nombre for c, _ in casos)
    repetidos = [n for n, veces in nombres.items() if veces > 1]
    if repetidos:
        raise SystemExit(f"Nombres repetidos: {repetidos[:5]}")
    with tempfile.TemporaryDirectory() as temporal:
        corpus = Path(temporal) / "corpus"
        para_javac = Path(temporal) / "javac"
        for caso_, _ in casos:
            carpeta = corpus / ("conErrores" if caso_.con_errores() else "sinErrores")
            carpeta.mkdir(parents=True, exist_ok=True)
            contenido = caso_.contenido()
            (carpeta / f"{caso_.nombre}.java").write_text(contenido, encoding="utf-8")
            para_javac.mkdir(exist_ok=True)
            (para_javac / f"{caso_.nombre}.java").write_text(contenido, encoding="utf-8")
        problemas, divergentes = contrastar_con_javac(casos, para_javac)
        if problemas:
            raise SystemExit("javac no coincide con el modelo de Java:\n" + "\n".join(problemas[:30]))
        cantidad = empaquetar(corpus, DESTINO)
    con = sum(c.con_errores() for c, _ in casos)
    print(f"{cantidad} casos ({len(casos) - con} correctos, {con} con errores); "
          f"{divergentes} donde MiniJava difiere de Java a proposito, verificados con javac")


if __name__ == "__main__":
    shutil.which("java") or sys.exit("Hace falta java 25 en el PATH")
    main()
