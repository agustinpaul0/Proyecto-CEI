package minijava.aplicacion;

import minijava.salida.SalidaEstandar;

import java.io.IOException;

// Punto de entrada: java -jar Compilador.jar <archivo-fuente>.
public final class Main {

    public static void main(String[] args) {
        // Sin System.exit() ni excepciones que escapen: los testers de la
        // cátedra invocan main() in-process y repetidamente en la misma JVM
        // (agents/convenciones-catedra.md), así que cualquiera de las dos
        // cosas mataría la corrida completa de tests.
        if (args.length == 0) {
            System.out.println("Uso: java -jar Compilador.jar <archivo-fuente>");
            return;
        }
        try {
            new ModuloPrincipal(new SalidaEstandar()).ejecutar(args[0]);
        } catch (IOException excepcion) {
            System.out.println("No se pudo leer el archivo fuente: " + args[0]);
        }
    }
}
