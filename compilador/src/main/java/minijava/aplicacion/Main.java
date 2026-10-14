package minijava.aplicacion;

import minijava.comun.salida.Salida;
import minijava.comun.salida.SalidaEstandar;

import java.io.IOException;
import java.util.Map;
import java.util.function.Function;

public final class Main {

    private static final String USO =
            "Uso: java -jar Compilador.jar [--etapa=1|2|3|4] <archivo-fuente>";

    static final Map<Integer, Function<Salida, ModuloPrincipal>> MODULO_POR_ETAPA = Map.of(
            1, ModuloPrincipalLexico::new,
            2, ModuloPrincipalSintactico::new,
            3, ModuloPrincipalSemantico::new,
            4, ModuloPrincipalSemantico::conChequeoDeSentencias);

    public static void main(String[] args) {
        // Sin System.exit() ni excepciones que escapen: los testers de la cátedra
        // invocan main() repetidamente en la misma JVM y morirían con la corrida.
        Salida salida = new SalidaEstandar();
        OpcionesDeInvocacion opciones = OpcionesDeInvocacion.parsear(args);
        opciones.avisos().forEach(salida::imprimir);

        String ruta = opciones.rutaFuente();
        if (ruta == null) {
            salida.imprimir(USO);
            return;
        }
        try {
            MODULO_POR_ETAPA.get(opciones.etapa()).apply(salida).ejecutar(ruta);
        } catch (IOException excepcion) {
            salida.imprimir("No se pudo leer el archivo fuente: " + ruta);
        }
    }
}
