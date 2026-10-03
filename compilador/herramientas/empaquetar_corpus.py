#!/usr/bin/env python3
"""Empaqueta un corpus generado en un único .tar.gz reproducible.

    python3 herramientas/empaquetar_corpus.py <carpeta> <archivo.tar.gz>

Los corpus generados tienen decenas de miles de fuentes chicos. Sueltos, las
cabeceras que el zip de entrega lleva por archivo pesan tanto como los fuentes;
empaquetados ocupan la cuarta parte. Los tests los descomprimen en target/corpus/
(CorpusEmpaquetado).

El archivo sale idéntico byte a byte si el corpus no cambió: entradas ordenadas,
sin fechas ni dueños, y gzip sin fecha. Así un corpus regenerado igual no
aparece como cambio en git.
"""
import gzip
import sys
import tarfile
from pathlib import Path


def empaquetar(origen, destino):
    origen, destino = Path(origen), Path(destino)
    fuentes = sorted(p for p in origen.rglob("*") if p.is_file())
    with open(destino, "wb") as crudo, \
            gzip.GzipFile(filename="", mode="wb", fileobj=crudo, mtime=0) as comprimido, \
            tarfile.open(fileobj=comprimido, mode="w", format=tarfile.PAX_FORMAT) as tar:
        for fuente in fuentes:
            info = tar.gettarinfo(fuente, arcname=fuente.relative_to(origen).as_posix())
            info.mtime = 0
            info.mode = 0o644
            info.uid = info.gid = 0
            info.uname = info.gname = ""
            with open(fuente, "rb") as contenido:
                tar.addfile(info, contenido)
    return len(fuentes)


def main():
    if len(sys.argv) != 3:
        sys.exit(__doc__)
    cantidad = empaquetar(sys.argv[1], sys.argv[2])
    print(f"{cantidad} fuentes en {sys.argv[2]}")


if __name__ == "__main__":
    main()
