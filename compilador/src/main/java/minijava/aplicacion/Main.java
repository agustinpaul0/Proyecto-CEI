package minijava.aplicacion;

import minijava.salida.SalidaEstandar;

import java.io.IOException;

// Punto de entrada: java -jar Compilador.jar <archivo-fuente>.
public final class Main {

    public static void main(String[] args) {
        try {
            new ModuloPrincipal(new SalidaEstandar()).ejecutar(args[0]);
        } catch (IOException excepcion) {
            System.out.println("No se pudo leer el archivo fuente: " + args[0]);
        }
    }
}
