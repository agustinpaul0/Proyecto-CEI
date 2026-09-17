package minijava.lexico;

import minijava.comun.io.ManejadorDeArchivo;
import minijava.comun.io.ManejadorDeArchivoEficiente;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

// Los dos ayudantes que comparten todas las suites del analizador léxico:
// escribir el fuente en un archivo temporal y abrir el analizador sobre él.
abstract class BaseDeAnalisisLexico {

    protected AnalizadorLexico crearAnalizador(Path dir, String fuente) throws IOException {
        Path archivo = dir.resolve("fuente.mjava");
        Files.writeString(archivo, fuente);
        ManejadorDeArchivo manejador = new ManejadorDeArchivoEficiente();
        manejador.open(archivo.toString());
        return new AnalizadorLexico(manejador);
    }

}
