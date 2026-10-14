"""Registro de los casos escritos a mano del corpus de la Etapa 4.

Cada caso declara su fuente y, si tiene error, el lexema que se espera. La línea
del error se marca en el fuente con `// <-` al final de la línea: el lote la
convierte en el número de línea del código `///[Error:lexema|N]`, contando la
cabecera. El lexema lo predice quien escribe el caso, antes de correr nada.
"""
from dataclasses import dataclass
from textwrap import dedent

MARCA = "// <-"


@dataclass(frozen=True)
class Caso:
    nombre: str
    descripcion: str
    fuente: str
    lexema: str | None

    def con_errores(self):
        return self.lexema is not None

    def contenido(self):
        cuerpo = dedent(self.fuente).strip("\n") + "\n"
        if not self.con_errores():
            if MARCA in cuerpo:
                raise ValueError(f"{self.nombre}: un caso correcto no lleva marca")
            return f"// {self.descripcion}\n{cuerpo}"
        lineas = cuerpo.splitlines()
        marcadas = [i for i, linea in enumerate(lineas) if linea.rstrip().endswith(MARCA)]
        if len(marcadas) != 1:
            raise ValueError(f"{self.nombre}: hace falta exactamente una linea marcada")
        linea = marcadas[0] + 3  # la cabecera ocupa las líneas 1 y 2
        return f"///[Error:{self.lexema}|{linea}]\n// {self.descripcion}\n{cuerpo}"


CASOS: list[Caso] = []


def bien(nombre, descripcion, fuente):
    CASOS.append(Caso(nombre, descripcion, fuente, None))


def mal(nombre, lexema, descripcion, fuente):
    CASOS.append(Caso(nombre, descripcion, fuente, lexema))
