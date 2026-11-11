package minijava.generacion;

import minijava.aplicacion.ModuloPrincipalSemantico;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

// Compila un fuente con el módulo principal de la Etapa 5, el mismo que usa Main,
// y devuelve lo que imprimió y el código generado, o null si no lo escribió.
public record Compilacion(List<String> reporte, String codigo) {

    public static Compilacion de(Path fuente) {
        try {
            Path salida = Files.createTempFile("minijava-", ".out");
            Files.delete(salida);
            try {
                List<String> reporte = new ArrayList<>();
                ModuloPrincipalSemantico.conGeneracionDeCodigo(reporte::add, salida.toString())
                        .ejecutar(fuente.toString());
                String codigo = Files.exists(salida) ? Files.readString(salida, StandardCharsets.UTF_8) : null;
                return new Compilacion(reporte, codigo);
            } finally {
                Files.deleteIfExists(salida);
            }
        } catch (IOException excepcion) {
            throw new UncheckedIOException(excepcion);
        }
    }

    public static Compilacion deTexto(String fuente) {
        try {
            Path archivo = Files.createTempFile("minijava-", ".java");
            try {
                Files.writeString(archivo, fuente, StandardCharsets.UTF_8);
                return de(archivo);
            } finally {
                Files.deleteIfExists(archivo);
            }
        } catch (IOException excepcion) {
            throw new UncheckedIOException(excepcion);
        }
    }

    public boolean exitosa() {
        return reporte.equals(List.of("Compilacion Exitosa", "", "[SinErrores]")) && codigo != null;
    }

    // Compila y corre en la CeIVM; falla si la compilación no fue exitosa.
    public static EjecutorCeIVM.Ejecucion ejecutar(String fuente) {
        Compilacion compilacion = deTexto(fuente);
        if (!compilacion.exitosa()) {
            throw new AssertionError("No compila:\n" + String.join("\n", compilacion.reporte()) + "\n" + fuente);
        }
        return EjecutorCeIVM.correr(compilacion.codigo());
    }
}
