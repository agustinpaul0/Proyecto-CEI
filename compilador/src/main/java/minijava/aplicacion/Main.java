package minijava.aplicacion;

import minijava.comun.salida.Salida;
import minijava.comun.salida.SalidaEstandar;

import java.io.IOException;
import java.util.Map;
import java.util.function.BiFunction;

public final class Main {

    private static final String USO = "Uso: java -jar Compilador.jar [--etapa=1|2|3|4|5] <archivo-fuente> "
            + "[<archivo-salida>]. El archivo de salida es obligatorio en la Etapa 5, la de por defecto.";

    // La ruta de salida sólo la usa la Etapa 5.
    static final Map<Integer, BiFunction<Salida, String, ModuloPrincipal>> MODULO_POR_ETAPA = Map.of(
            1, (salida, rutaSalida) -> new ModuloPrincipalLexico(salida),
            2, (salida, rutaSalida) -> new ModuloPrincipalSintactico(salida),
            3, (salida, rutaSalida) -> new ModuloPrincipalSemantico(salida),
            4, (salida, rutaSalida) -> ModuloPrincipalSemantico.conChequeoDeSentencias(salida),
            5, ModuloPrincipalSemantico::conGeneracionDeCodigo);

    private static final int ETAPA_DE_GENERACION = 5;

    public static void main(String[] args) {
        // Sin System.exit() ni excepciones que escapen: los testers de la cátedra
        // invocan main() repetidamente en la misma JVM y morirían con la corrida.
        Salida salida = new SalidaEstandar();
        OpcionesDeInvocacion opciones = OpcionesDeInvocacion.parsear(args);
        opciones.avisos().forEach(salida::imprimir);

        String ruta = opciones.rutaFuente();
        boolean faltaLaSalida = opciones.etapa() == ETAPA_DE_GENERACION && opciones.rutaSalida() == null;
        if (ruta == null || faltaLaSalida) {
            salida.imprimir(USO);
            return;
        }
        try {
            MODULO_POR_ETAPA.get(opciones.etapa()).apply(salida, opciones.rutaSalida()).ejecutar(ruta);
        } catch (IOException excepcion) {
            salida.imprimir("No se pudo leer el archivo fuente: " + ruta);
        }
    }
}
