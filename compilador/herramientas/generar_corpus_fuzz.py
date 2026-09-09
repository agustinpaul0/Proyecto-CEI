#!/usr/bin/env python3
"""Genera el corpus de fuentes del analizador sintáctico con Grammarinator.

    make fuzz                      # desde compilador/
    python3 herramientas/generar_corpus_fuzz.py --positivos 600 --negativos 600

Cómo funciona, de punta a punta:

1. Se emiten desde gramatica/minijava-ll1-logros.g las dos gramáticas que las
   herramientas externas entienden: `MiniJava.g4` para Grammarinator y
   `MiniJavaParser.jj` para el oráculo JavaCC.
2. Grammarinator deriva fuentes MiniJava al azar. Son los casos POSITIVOS.
3. Los fuentes válidos se vuelven a parsear en árboles y Grammarinator los MUTA:
   cambia, borra o duplica subárboles. Casi siempre eso rompe la sintaxis, y ahí
   sale un caso NEGATIVO.
4. El oráculo JavaCC —un tercer reconocedor de la misma gramática, independiente
   del analizador en Java y del simulador en Python— dice qué veredicto
   corresponde, y de ahí sale la expectativa `[Error:lexema|linea]` del caso.

Un caso entra al corpus SÓLO si su veredicto es el mismo en TODAS las
configuraciones de control (`--verificar-en`). La cátedra corre sus testers sobre
la carpeta de entrega con el compilador tal cual, o sea con todos los logros, así
que un caso que dependa de tener un logro apagado haría fallar esa corrida. Los
pares acepta-con / rechaza-sin los cubren los tests con fuente embebido y
herramientas/casos-gramatica.sh, que sí saben apagar logros.

Los casos que sólo valen con todos los logros prendidos declaran ese requisito en
resources/requisitos-de-logros.txt, para que una corrida con
`-Dlogros=ninguno` los saltee en vez de fallar.
"""
import argparse
import hashlib
import shutil
import subprocess
import sys
from pathlib import Path

RAIZ = Path(__file__).resolve().parent.parent
GRAMATICA = RAIZ / "gramatica" / "minijava-ll1-logros.g"
HERRAMIENTAS = RAIZ / "herramientas"
TRABAJO = HERRAMIENTAS / "fuzz" / "trabajo"
JARS = HERRAMIENTAS / "fuzz" / "jars"
VENV = RAIZ / ".venv" / "bin"

SIN_ERRORES = RAIZ / "resources" / "sinErrores"
CON_ERRORES = RAIZ / "resources" / "conErrores"
MANIFIESTO = RAIZ / "resources" / "requisitos-de-logros.txt"

TODOS_LOS_LOGROS = "todos"
LOGROS_SINTACTICOS = ("visibilidad", "atrinit", "varclasica", "for", "ternario",
                      "posfijo", "lambda", "arrinit", "generico", "float")


def correr(comando, **kwargs):
    return subprocess.run([str(c) for c in comando], check=True, **kwargs)


def herramienta(nombre):
    ruta = VENV / nombre
    if not ruta.exists():
        sys.exit(f"ERROR: falta {ruta}. Corré `make venv` primero.")
    return ruta


def emitir_gramaticas(logros, destino):
    destino.mkdir(parents=True, exist_ok=True)
    g4 = destino / "MiniJava.g4"
    jj = destino / "MiniJavaParser.jj"
    for script, salida in ((HERRAMIENTAS / "gramatica_a_antlr.py", g4),
                           (HERRAMIENTAS / "gramatica_a_javacc.py", jj)):
        with open(salida, "w", encoding="utf-8") as archivo:
            correr([sys.executable, script, GRAMATICA, "--logros", logros], stdout=archivo)
    return g4, jj


def construir_oraculo(jj, destino):
    """Compila el parser JavaCC. Devuelve el classpath desde donde invocarlo."""
    gen, cp = destino / "gen", destino / "cp"
    for carpeta in (gen, cp):
        shutil.rmtree(carpeta, ignore_errors=True)
        carpeta.mkdir(parents=True)
    correr(["java", "-cp", JARS / "javacc.jar", "javacc",
            f"-OUTPUT_DIRECTORY={gen}", jj], stdout=subprocess.DEVNULL)
    correr(["javac", "-nowarn", "-encoding", "UTF-8", "-d", cp]
           + sorted(str(p) for p in gen.glob("*.java")), stdout=subprocess.DEVNULL)
    return cp


def consultar_oraculo(cp, archivos, por_tanda=400):
    """Un veredicto por archivo. Se invoca de a muchos por JVM: una por archivo
    tardaría horas sobre un corpus de miles."""
    veredictos = {}
    for i in range(0, len(archivos), por_tanda):
        tanda = archivos[i:i + por_tanda]
        salida = subprocess.run(
            ["java", "-Dfile.encoding=UTF-8", "-cp", str(cp), "MiniJavaParser"]
            + [str(a) for a in tanda],
            capture_output=True, text=True, encoding="utf-8", check=True).stdout
        for linea in salida.splitlines():
            ruta, _, veredicto = linea.rpartition("\t")
            veredictos[Path(ruta)] = veredicto
    return veredictos


def generar_arboles(generador, cantidad, profundidad, semilla, patron, poblacion=None):
    comando = [herramienta("grammarinator-generate"),
               "MiniJavaGenerator.MiniJavaGenerator",
               "--sys-path", generador,
               "--sys-path", HERRAMIENTAS / "fuzz",
               "-r", "inicial",
               "-t", "evitar_palabras_reservadas.evitar",
               "-s", "grammarinator.runtime.simple_space_serializer",
               "-o", patron, "-n", cantidad, "-d", profundidad,
               "--random-seed", semilla]
    if poblacion is not None:
        comando += ["--population", poblacion, "--no-generate", "--no-recombine",
                    "--keep-trees", "--unique-attempts", "60"]
    correr(comando, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)


def sin_repetidos(archivos, vistos):
    unicos = []
    for archivo in sorted(archivos):
        digesto = hashlib.sha256(archivo.read_bytes()).hexdigest()
        if digesto in vistos:
            archivo.unlink()
            continue
        vistos.add(digesto)
        unicos.append(archivo)
    return unicos


CABECERA_POSITIVA = "// Generado por herramientas/generar_corpus_fuzz.py (semilla {semilla}).\n"
CABECERA_NEGATIVA = "///{codigo}\n"


def publicar_positivos(candidatos, veredictos, semilla, prefijo):
    """Un positivo entra si TODOS los oráculos de control lo aceptan. La cabecera
    es un comentario: no corre ninguna línea que importe, porque los casos
    correctos no declaran expectativas por línea."""
    publicados = []
    for archivo in candidatos:
        if any(tabla.get(archivo) != "[SinErrores]" for tabla in veredictos):
            continue
        destino = SIN_ERRORES / f"{prefijo}{len(publicados):04d}.java"
        destino.write_bytes(CABECERA_POSITIVA.format(semilla=semilla).encode("utf-8")
                            + archivo.read_bytes().rstrip() + b"\n")
        publicados.append(destino)
    return publicados


def publicar_negativos(candidatos, classpaths, prefijo):
    """Un negativo entra si TODOS los oráculos de control lo rechazan EN EL MISMO
    TOKEN.

    La expectativa se calcula sobre el archivo que ya lleva su cabecera, no sobre
    el contenido pelado: la cabecera es una línea más y correría el número de
    línea del error. Por eso se escribe primero con una cabecera de relleno del
    mismo alto, se consulta, y recién ahí se escribe la definitiva.
    """
    provisorios = []
    for i, archivo in enumerate(candidatos):
        provisorio = CON_ERRORES / f".provisorio{i:05d}.java"
        provisorio.write_bytes(b"//\n" + archivo.read_bytes().rstrip() + b"\n")
        provisorios.append(provisorio)

    veredictos = [consultar_oraculo(cp, provisorios) for cp in classpaths]

    publicados = []
    for provisorio in provisorios:
        codigo = veredictos[0].get(provisorio, "")
        if not codigo.startswith("[Error:") or any(t.get(provisorio) != codigo
                                                   for t in veredictos[1:]):
            provisorio.unlink()
            continue
        cuerpo = provisorio.read_bytes().split(b"\n", 1)[1]
        destino = CON_ERRORES / f"{prefijo}{len(publicados):04d}.java"
        destino.write_bytes(CABECERA_NEGATIVA.format(codigo=codigo).encode("utf-8") + cuerpo)
        provisorio.unlink()
        publicados.append(destino)
    return publicados


def oraculos_de_control(configuraciones):
    """Compila un oráculo JavaCC por configuración de control. Se reusan: cada uno
    tarda unos segundos en generarse y compilarse."""
    classpaths = []
    for i, configuracion in enumerate(configuraciones):
        _, jj = emitir_gramaticas(configuracion, TRABAJO / f"gramaticas-{i}")
        classpaths.append(construir_oraculo(jj, TRABAJO / f"oraculo-{i}"))
        print(f"  oraculo listo para --logros {configuracion}")
    return classpaths


def escribir_manifiesto(entradas):
    """Qué logros necesita cada caso. Un caso que no figura acá aplica a cualquier
    configuración, que es lo que se busca para el lote base."""
    lineas = [
        "# A qué configuración de logros aplica cada caso del corpus sintáctico.",
        "#",
        "# Lo genera herramientas/generar_corpus_fuzz.py junto con el corpus. Un caso",
        "# que no figura acá vale en CUALQUIER configuración: se verificó contra un",
        "# oráculo por cada logro por separado, además de con todos y con ninguno.",
        "#",
        "# Formato, un caso por línea:",
        "#",
        "#     <directorio>/<archivo> | <logros que deben estar PRENDIDOS> | <APAGADOS>",
        "",
    ]
    lineas += entradas
    MANIFIESTO.write_text("\n".join(lineas) + "\n", encoding="utf-8")
    print(f"{MANIFIESTO.relative_to(RAIZ)}: {len(entradas)} casos con requisitos")


def generar(args, classpaths, prefijo, logros):
    generador = TRABAJO / "generador"
    shutil.rmtree(generador, ignore_errors=True)
    generador.mkdir(parents=True)

    g4, _ = emitir_gramaticas(logros, TRABAJO / "gramaticas-generacion")
    correr([herramienta("grammarinator-process"), g4, "-o", generador],
           stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)

    etapa = TRABAJO / "etapa"
    shutil.rmtree(etapa, ignore_errors=True)
    etapa.mkdir()

    print(f"[{prefijo}] generando {args.positivos} positivos...")
    generar_arboles(generador, args.positivos, args.profundidad, args.semilla,
                    str(etapa / "pos_%d.java"))
    vistos = set()
    candidatos = sin_repetidos(list(etapa.glob("pos_*.java")), vistos)
    positivos = publicar_positivos(
        candidatos, [consultar_oraculo(cp, candidatos) for cp in classpaths],
        args.semilla, prefijo)
    print(f"[{prefijo}]   {len(positivos)} positivos publicados")

    print(f"[{prefijo}] armando la poblacion de arboles...")
    arboles = TRABAJO / "arboles"
    shutil.rmtree(arboles, ignore_errors=True)
    arboles.mkdir()
    fuentes = [p for p in positivos if p.read_bytes().split(b"\n", 1)[1].strip()]
    if fuentes:
        correr([herramienta("grammarinator-parse"), *[str(f) for f in fuentes],
                "-g", g4, "-r", "inicial", "-o", arboles, "--antlr", JARS / "antlr.jar"],
               stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)

    print(f"[{prefijo}] mutando para {args.negativos} negativos...")
    shutil.rmtree(etapa, ignore_errors=True)
    etapa.mkdir()
    generar_arboles(generador, args.negativos * 3, args.profundidad, args.semilla + 1,
                    str(etapa / "neg_%d.java"), poblacion=arboles)
    candidatos = sin_repetidos(list(etapa.glob("neg_*.java")), vistos)
    negativos = publicar_negativos(candidatos, classpaths, prefijo)
    print(f"[{prefijo}]   {len(negativos)} negativos publicados")
    return positivos, negativos


def main():
    ap = argparse.ArgumentParser(description=__doc__,
                                 formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--positivos", type=int, default=600)
    ap.add_argument("--negativos", type=int, default=600)
    ap.add_argument("--profundidad", type=int, default=22)
    ap.add_argument("--semilla", type=int, default=20260923)
    args = ap.parse_args()

    SIN_ERRORES.mkdir(parents=True, exist_ok=True)
    CON_ERRORES.mkdir(parents=True, exist_ok=True)
    TRABAJO.mkdir(parents=True, exist_ok=True)
    for carpeta in (SIN_ERRORES, CON_ERRORES):
        for viejo in list(carpeta.glob("fuzz*.java")) + list(carpeta.glob("base*.java")):
            viejo.unlink()

    # El lote `base` se deriva del núcleo sin logros y se verifica contra TODAS las
    # configuraciones de control, así que vale en cualquier corrida. El lote `fuzz`
    # se deriva con todos los logros y sólo tiene que valer ahí, que es como la
    # cátedra corre sus testers sobre la carpeta de entrega.
    print("compilando los oraculos de control...")
    control_completo = oraculos_de_control(
        ["ninguno", TODOS_LOS_LOGROS] + list(LOGROS_SINTACTICOS))
    control_de_entrega = [control_completo[1]]

    base = generar(args, control_completo, "base", "ninguno")
    fuzz = generar(args, control_de_entrega, "fuzz", TODOS_LOS_LOGROS)

    requisitos = ",".join(LOGROS_SINTACTICOS)
    entradas = [f"{caso.parent.name}/{caso.name:<20} | {requisitos} |"
                for caso in sorted(fuzz[0] + fuzz[1])]
    escribir_manifiesto(entradas)

    print(f"\nlote base: {len(base[0])} positivos y {len(base[1])} negativos, "
          f"validos en cualquier configuracion.")
    print(f"lote fuzz: {len(fuzz[0])} positivos y {len(fuzz[1])} negativos, "
          f"con todos los logros.")
    print(f"semilla {args.semilla}.")


if __name__ == "__main__":
    main()
