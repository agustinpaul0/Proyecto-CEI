package minijava.generacion;

import ar.edu.uns.cs.cei.ceivmapi.CeIVMAPI;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

// Corre un programa CeIASM en la CeIVM de la cátedra, en el mismo proceso, por su
// API: es lo mismo que hace su front-end (CeIVM.main) sin el banner y sin lanzar
// una JVM por caso. Lo que el programa imprime queda separado del error de
// ejecución, que la CeIVM informa con una excepción.
public final class EjecutorCeIVM {

    private static final int MEMORIA = 8192;

    private EjecutorCeIVM() {
    }

    public record Ejecucion(String salida, String error) {

        public boolean terminoBien() {
            return error == null;
        }
    }

    public static Ejecucion correr(String codigo) {
        return correr(codigo, "");
    }

    public static Ejecucion correr(String codigo, String entrada) {
        try {
            Path archivo = Files.createTempFile("ceivm", ".out");
            try {
                Files.writeString(archivo, codigo, StandardCharsets.UTF_8);
                return correr(archivo, entrada);
            } finally {
                Files.deleteIfExists(archivo);
            }
        } catch (IOException excepcion) {
            throw new IllegalStateException(excepcion);
        }
    }

    public static Ejecucion correr(Path archivo, String entrada) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        CeIVMAPI vm = new CeIVMAPI(MEMORIA,
                new ByteArrayInputStream(entrada.getBytes(StandardCharsets.UTF_8)),
                salida, salida, StandardCharsets.UTF_8.name());
        String error = null;
        try {
            vm.parseAndAssemble(archivo.toString());
            vm.loadProgram();
            vm.initializeVM();
            vm.executeToCompletion();
        } catch (Exception excepcion) {
            error = String.valueOf(excepcion.getMessage());
        }
        return new Ejecucion(salida.toString(StandardCharsets.UTF_8), error);
    }

    // Sólo ensambla: para los fuentes de la robustez, que no se ejecutan.
    public static String errorDeEnsamblado(String codigo) {
        try {
            Path archivo = Files.createTempFile("ceivm", ".out");
            try {
                Files.writeString(archivo, codigo, StandardCharsets.UTF_8);
                CeIVMAPI vm = new CeIVMAPI(MEMORIA, new ByteArrayInputStream(new byte[0]),
                        new ByteArrayOutputStream(), new ByteArrayOutputStream(), StandardCharsets.UTF_8.name());
                vm.parseAndAssemble(archivo.toString());
                vm.loadProgram();
                return null;
            } catch (Exception excepcion) {
                return String.valueOf(excepcion.getMessage());
            } finally {
                Files.deleteIfExists(archivo);
            }
        } catch (IOException excepcion) {
            throw new IllegalStateException(excepcion);
        }
    }
}
