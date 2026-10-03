#!/usr/bin/env python3
"""Verifica los diagramas de clases de la documentación contra el código y los
renderiza para el informe.

    python3 herramientas/diagramas_de_clases.py --verificar   # sólo compara
    python3 herramientas/diagramas_de_clases.py               # compara y renderiza

Los diagramas viven en un solo lugar: los bloques ```mermaid``` de
documentacion/semantico/arquitectura-del-analizador-semantico.md, cada uno
precedido por `<!-- diagrama: nombre -->`. El informe no los copia: incluye el
PDF que esta herramienta renderiza de esos mismos bloques en
etapas/etapa-3/entregables/informe/generado/, así la documentación y el informe
muestran siempre el mismo diagrama.

La verificación exige que cada campo, constructor y método declarado en el
código del semántico aparezca en el diagrama de su clase: como miembro de la
caja o, si es una referencia a otra clase dibujada, como etiqueta de una
relación que sale de ella. También señala lo que el diagrama muestra y el código
ya no tiene.

Renderizar usa mermaid-cli (`npx @mermaid-js/mermaid-cli`) con el Chrome del
sistema.
"""
import json
import os
import re
import subprocess
import sys
import tempfile
from pathlib import Path

RAIZ = Path(__file__).resolve().parent.parent
REPOSITORIO = RAIZ.parent
CODIGO = RAIZ / "src" / "main" / "java" / "minijava" / "semantico"
DOCUMENTO = REPOSITORIO / "documentacion" / "semantico" / "arquitectura-del-analizador-semantico.md"
SALIDA = REPOSITORIO / "etapas" / "etapa-3" / "entregables" / "informe" / "generado"
CHROME = "/usr/bin/google-chrome"
MERMAID_CLI = "@mermaid-js/mermaid-cli@11"

# Monocromático, como el resto del informe.
CONFIGURACION = {
    "theme": "neutral",
    "class": {"hideEmptyMembersBox": True},
    "themeVariables": {"fontSize": "14px"},
}

PALABRAS_DE_CONTROL = {"if", "for", "while", "switch", "return", "new", "throw", "catch",
                       "else", "do", "try", "case", "default"}


# --- El código ---------------------------------------------------------------

def sin_comentarios_ni_literales(fuente):
    fuente = re.sub(r"//[^\n]*", "", fuente)
    fuente = re.sub(r"/\*.*?\*/", "", fuente, flags=re.S)
    fuente = re.sub(r'"(\\.|[^"\\])*"', '""', fuente)
    return re.sub(r"'(\\.|[^'\\])'", "''", fuente)


DECLARACION_DE_TIPO = re.compile(
    r"\b(class|interface|record|enum)\s+(\w+)\s*(\([^)]*\))?[^{;]*\{")
CAMPO = re.compile(r"^\s*(?:(?:public|protected|private|static|final)\s+)*"
                   r"[\w<>\[\],.? ]+?\s+(\w+)\s*(?:=[^;]*)?;$")
METODO = re.compile(r"^\s*(?:@\w+\s+)*(?:(?:public|protected|private|static|final|abstract|"
                    r"default|sealed|synchronized)\s+)*(?:<[^>]+>\s+)?(?:[\w<>\[\],.? ]+?\s+)?"
                    r"(\w+)\s*\(")


def aridad(firma):
    """Cuántos parámetros hay entre los primeros paréntesis de `firma`. Las comas
    de un genérico (`Map<String, Tipo>` en Java, `Map~String, Tipo~` en mermaid)
    no separan parámetros."""
    parametros = firma[firma.index("(") + 1:firma.index(")")]
    if not parametros.strip():
        return 0
    profundidad, comas, adentro_de_tildes = 0, 0, False
    for caracter in parametros:
        if caracter == "<":
            profundidad += 1
        elif caracter == ">":
            profundidad -= 1
        elif caracter == "~":
            adentro_de_tildes = not adentro_de_tildes
        elif caracter == "," and profundidad == 0 and not adentro_de_tildes:
            comas += 1
    return comas + 1


def miembros_del_codigo():
    """{clase: {miembro}} de todo el paquete del semántico. Un miembro es
    ("campo", nombre) o ("método", nombre, aridad): los constructores son
    métodos, y las constantes de un enum y los componentes de un record, campos."""
    clases = {}
    for archivo in sorted(CODIGO.rglob("*.java")):
        fuente = sin_comentarios_ni_literales(archivo.read_text(encoding="utf-8"))
        pila = []
        profundidad = 0
        sentencia = ""
        for linea in fuente.splitlines():
            for declaracion in DECLARACION_DE_TIPO.finditer(linea):
                tipo, nombre, componentes = declaracion.groups()
                miembros = clases.setdefault(nombre, set())
                if componentes:
                    for componente in componentes.strip("()").split(","):
                        if componente.strip():
                            miembros.add(("campo", componente.split()[-1]))
                pila.append((nombre, tipo, profundidad + 1))
            if pila and profundidad == pila[-1][2] and not DECLARACION_DE_TIPO.search(linea):
                nombre, tipo, _ = pila[-1]
                sentencia = (sentencia + " " + linea.strip()).strip()
                if tipo == "enum" and re.match(r"^[A-Z_]+\s*(\(|,|;|$)", sentencia):
                    for constante in re.findall(r"\b([A-Z][A-Z_]+)\s*(?:\([^)]*\))?\s*[,;]?",
                                                sentencia.split(";")[0]):
                        clases[nombre].add(("campo", constante))
                    sentencia = ""
                else:
                    metodo = METODO.match(sentencia)
                    if metodo and metodo.group(1) not in PALABRAS_DE_CONTROL:
                        if ")" in sentencia:
                            clases[nombre].add(("método", metodo.group(1), aridad(sentencia)))
                            sentencia = ""
                    elif sentencia.endswith(";"):
                        campo = CAMPO.match(sentencia)
                        if campo:
                            clases[nombre].add(("campo", campo.group(1)))
                        sentencia = ""
                    elif sentencia.endswith(("{", "}")) or sentencia == "":
                        sentencia = ""
            profundidad += linea.count("{") - linea.count("}")
            while pila and profundidad < pila[-1][2]:
                pila.pop()
    return clases


# --- Los diagramas -------------------------------------------------------------

BLOQUE = re.compile(r"<!-- diagrama: ([\w-]+) -->\s*```mermaid\n(.*?)```", re.S)
# Sólo las asociaciones representan un campo: su etiqueta es el nombre del campo
# de la clase de la izquierda. Una dependencia (`..>`) no es un campo.
ASOCIACION = re.compile(r'^\s*(\w+)\s*(?:"[^"]*"\s*)?(?:\*--|o--|-->)\s*'
                        r'(?:"[^"]*"\s*)?\w+\s*:\s*(\w+)\s*$')


def diagramas():
    return BLOQUE.findall(DOCUMENTO.read_text(encoding="utf-8"))


def miembros_de_los_diagramas():
    """{clase: {miembro}}, con los miembros como en `miembros_del_codigo`: lo que
    muestra cada caja y las etiquetas de las asociaciones que salen de ella, en
    todos los diagramas juntos."""
    clases = {}
    for _, codigo in diagramas():
        actual = None
        for linea in codigo.splitlines():
            apertura = re.match(r"^\s*class\s+(\w+)\s*\{\s*$", linea)
            if apertura:
                actual = apertura.group(1)
                clases.setdefault(actual, set())
                continue
            if actual and linea.strip() == "}":
                actual = None
                continue
            if actual:
                texto = linea.strip()
                nombre = re.match(r"^[-+#~]?(\w+)", texto)
                if nombre and not texto.startswith("<<"):
                    if "(" in texto:
                        clases[actual].add(("método", nombre.group(1), aridad(texto)))
                    else:
                        clases[actual].add(("campo", nombre.group(1)))
                continue
            sola = re.match(r"^\s*class\s+(\w+)\s*$", linea)
            if sola:
                clases.setdefault(sola.group(1), set())
                continue
            asociacion = ASOCIACION.match(linea)
            if asociacion:
                clases.setdefault(asociacion.group(1), set()).add(("campo", asociacion.group(2)))
    return clases


def describir(miembro):
    if miembro[0] == "campo":
        return f"el campo {miembro[1]}"
    return f"el método {miembro[1]} con {miembro[2]} parámetros"


def verificar():
    codigo = miembros_del_codigo()
    dibujado = miembros_de_los_diagramas()
    problemas = []
    for clase, miembros in sorted(codigo.items()):
        if clase not in dibujado:
            problemas.append(f"{clase}: no aparece en ningún diagrama")
            continue
        for faltante in sorted(miembros - dibujado[clase]):
            problemas.append(f"{clase}: falta {describir(faltante)}")
    for clase, miembros in sorted(dibujado.items()):
        if clase in codigo:
            for sobrante in sorted(miembros - codigo[clase]):
                problemas.append(f"{clase}: el diagrama muestra {describir(sobrante)}, que el código no tiene")
    return problemas


def renderizar():
    SALIDA.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory() as trabajo:
        trabajo = Path(trabajo)
        (trabajo / "configuracion.json").write_text(json.dumps(CONFIGURACION))
        (trabajo / "puppeteer.json").write_text(
            json.dumps({"executablePath": CHROME, "args": ["--no-sandbox"]}))
        for nombre, codigo in diagramas():
            fuente = trabajo / f"{nombre}.mmd"
            fuente.write_text(codigo, encoding="utf-8")
            subprocess.run(["npx", "-y", MERMAID_CLI, "-q",
                            "-p", trabajo / "puppeteer.json", "-c", trabajo / "configuracion.json",
                            "-i", fuente, "-o", SALIDA / f"{nombre}.pdf", "--pdfFit"],
                           check=True, env={**os.environ, "PUPPETEER_SKIP_DOWNLOAD": "1"})
            print(f"  {SALIDA.relative_to(REPOSITORIO)}/{nombre}.pdf")


def main():
    problemas = verificar()
    for problema in problemas:
        print(problema)
    if problemas:
        sys.exit(f"{len(problemas)} diferencias entre los diagramas y el código.")
    print("Los diagramas coinciden con el código.")
    if "--verificar" not in sys.argv:
        renderizar()


if __name__ == "__main__":
    main()
