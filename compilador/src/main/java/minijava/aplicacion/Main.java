package minijava.aplicacion;

import minijava.comun.salida.Salida;
import minijava.comun.salida.SalidaEstandar;

import java.io.IOException;

public final class Main {

    private static final String USO =
            "Uso: java -jar Compilador.jar [--etapa=1|2] [--logros=...] <archivo-fuente>";

    public static void main(String[] args) {
        // Sin System.exit() ni excepciones que escapen: los testers de la cátedra
        // invocan main() repetidamente en la misma JVM y morirían con la corrida.
        Salida salida = new SalidaEstandar();
        OpcionesDeInvocacion opciones = OpcionesDeInvocacion.parsear(args);
        opciones.avisos().forEach(salida::imprimir);

        if (opciones.rutaFuente().isEmpty()) {
            salida.imprimir(USO);
            return;
        }
        String ruta = opciones.rutaFuente().get();
        try {
            new ModuloPrincipalLexico(salida, opciones.configuracion()).ejecutar(ruta);
        } catch (IOException excepcion) {
            salida.imprimir("No se pudo leer el archivo fuente: " + ruta);
        }
    }
}
