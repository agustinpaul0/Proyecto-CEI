"""Transformador de Grammarinator: un idMV no puede ser una palabra reservada.

La regla léxica de idMetVar es `minuscula (letra | digito | _)*`, así que el
generador puede producir `for` o `int`, que el léxico reconocería como palabra
clave y no como identificador. Se le agrega un `_` al final: ninguna palabra
reservada de MiniJava termina en `_`, así que no puede caer en otra.
"""
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))
import gramaticas_externas as externas  # noqa: E402
from grammarinator.runtime import Rule, UnlexerRule  # noqa: E402

RESERVADAS = {lexema for lexema, _, _ in externas.PALABRAS_CLAVE}


def evitar(raiz: Rule) -> Rule:
    _recorrer(raiz)
    return raiz


def _recorrer(nodo: Rule) -> None:
    if isinstance(nodo, UnlexerRule):
        if nodo.name == "ID_MV" and nodo.src in RESERVADAS:
            nodo.src = nodo.src + "_"
        return
    for hijo in getattr(nodo, "children", ()):
        _recorrer(hijo)
