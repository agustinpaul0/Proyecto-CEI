package minijava.aplicacion;

import minijava.comun.salida.SalidaEstandar;

import java.io.IOException;

public final class Main {

    public static void main(String[] args) {
        // Sin System.exit() ni excepciones que escapen: los testers de la cátedra
        // invocan main() repetidamente en la misma JVM y morirían con la corrida.
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
