package minijava.sintactico;

import minijava.aplicacion.ModuloPrincipalSintactico;
import minijava.comun.io.ManejadorDeArchivo;
import minijava.comun.io.ManejadorDeArchivoEficiente;
import minijava.comun.salida.SalidaEnMemoria;
import minijava.casos.VolcadoDeCasos;
import minijava.lexico.AnalizadorLexico;
import minijava.sintactico.errores.ExcepcionSintactica;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Lo común a los tests que ejercitan el analizador sobre un fuente: correrlo y
// afirmar que acepta o que rechaza.
abstract class BaseDeAnalisis {

    protected List<ExcepcionSintactica> erroresDe(Path dir, String fuente) throws Exception {
        Path archivo = dir.resolve("fuente.mjava");
        Files.writeString(archivo, fuente);
        volcar(archivo, fuente);
        ManejadorDeArchivo manejador = new ManejadorDeArchivoEficiente();
        manejador.open(archivo.toString());
        try {
            return new AnalizadorSintactico(new AnalizadorLexico(manejador)).analizar();
        } finally {
            manejador.close();
        }
    }

    // Estos tests trabajan con la excepción y no con la consola, así que sin esto
    // correrlos desde el IDE no muestra nada de lo que el compilador diría. Se
    // vuelca la salida REAL —se corre el módulo principal sobre el mismo fuente—
    // y no una reconstrucción, para que lo que se lee en la consola sea
    // exactamente lo que imprimiría `java -jar Compilador.jar`.
    //
    // Lo gobierna el mismo -Dvolcado que el corpus: completo | resumen | off.
    private void volcar(Path archivo, String fuente) {
        if (!VolcadoDeCasos.habilitado()) {
            return;
        }
        SalidaEnMemoria salida = new SalidaEnMemoria();
        try {
            new ModuloPrincipalSintactico(salida).ejecutar(archivo.toString());
        } catch (Exception excepcion) {
            salida.imprimir(excepcion.toString());
        }
        VolcadoDeCasos.volcar(enUnaLinea(fuente), salida.getLineas());
    }

    private String enUnaLinea(String fuente) {
        return fuente.strip().replaceAll("\\s+", " ");
    }

    // Analizar y dejar que el primer error suba como excepción: es lo que espera
    // la mayoría de los tests, que ejercitan una construcción por vez.
    protected void analizar(Path dir, String fuente) throws Exception {
        List<ExcepcionSintactica> errores = erroresDe(dir, fuente);
        if (!errores.isEmpty()) {
            throw errores.getFirst();
        }
    }

    protected void aceptar(Path dir, String fuente) {
        assertDoesNotThrow(() -> analizar(dir, fuente), fuente);
    }

    protected ExcepcionSintactica rechazar(Path dir, String fuente) {
        return assertThrows(ExcepcionSintactica.class, () -> analizar(dir, fuente), fuente);
    }

    protected String envolverEnMetodo(String sentencias) {
        return "class C1 {\n  void m(){\n    " + sentencias + "\n  }\n}\n";
    }
}
