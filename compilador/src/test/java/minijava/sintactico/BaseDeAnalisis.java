package minijava.sintactico;

import minijava.comun.config.ConfiguracionDelCompilador;
import minijava.comun.io.ManejadorDeArchivo;
import minijava.comun.io.ManejadorDeArchivoEficiente;
import minijava.lexico.AnalizadorLexico;
import minijava.logros.ConfiguracionDeLaCorrida;
import minijava.sintactico.errores.ExcepcionSintactica;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Lo común a los tests que ejercitan el analizador sobre un fuente: correrlo con
// los logros de la corrida y afirmar que acepta o que rechaza.
abstract class BaseDeAnalisis {

    protected void analizar(Path dir, String fuente, ConfiguracionDelCompilador configuracion)
            throws Exception {
        Path archivo = dir.resolve("fuente.mjava");
        Files.writeString(archivo, fuente);
        ManejadorDeArchivo manejador = new ManejadorDeArchivoEficiente();
        manejador.open(archivo.toString());
        try {
            new AnalizadorSintactico(new AnalizadorLexico(manejador, configuracion), configuracion)
                    .analizar();
        } finally {
            manejador.close();
        }
    }

    protected void aceptar(Path dir, String fuente) {
        assertDoesNotThrow(() -> analizar(dir, fuente, ConfiguracionDeLaCorrida.actual()), fuente);
    }

    protected ExcepcionSintactica rechazar(Path dir, String fuente) {
        return assertThrows(ExcepcionSintactica.class,
                () -> analizar(dir, fuente, ConfiguracionDeLaCorrida.actual()), fuente);
    }

    protected String envolverEnMetodo(String sentencias) {
        return "class C1 {\n  void m(){\n    " + sentencias + "\n  }\n}\n";
    }
}
