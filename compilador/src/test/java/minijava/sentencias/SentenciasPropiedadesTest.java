package minijava.sentencias;

import minijava.aplicacion.ModuloPrincipalSemantico;
import minijava.comun.salida.SalidaEnMemoria;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Tag;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Propiedades del chequeo de expresiones sobre expresiones armadas al azar
// (ExpresionAleatoria). La semilla es fija: una corrida es reproducible.
@Tag("sentencias")
class SentenciasPropiedadesTest {

    private static final Path ARCHIVO = crearArchivo();
    private static final int LINEA_DE_LA_EXPRESION = 6;

    private static Path crearArchivo() {
        try {
            Path archivo = Files.createTempFile("minijava-sentencias-", ".java");
            archivo.toFile().deleteOnExit();
            return archivo;
        } catch (IOException excepcion) {
            throw new UncheckedIOException(excepcion);
        }
    }

    private static String programa(String destino, ExpresionAleatoria expresion) {
        return """
                class Aa {
                    int[] v;
                    int f(int x) { return x; }
                    boolean g(boolean x) { return x; }
                    void m(int a, boolean b, int ri, boolean rb) {
                        %s = %s;
                    }
                }
                """.formatted(destino, expresion.texto());
    }

    @Property(tries = 1500, seed = "20261014")
    void unaExpresionEnteraBienTipadaCompila(@ForAll long semilla) {
        ExpresionAleatoria expresion = ExpresionAleatoria.entera(new Random(semilla), 5);
        assertEquals(List.of("Compilacion Exitosa", "", "[SinErrores]"), compilar(programa("ri", expresion)),
                expresion.texto());
    }

    @Property(tries = 1500, seed = "20261015")
    void unaExpresionBooleanaBienTipadaCompila(@ForAll long semilla) {
        ExpresionAleatoria expresion = ExpresionAleatoria.booleana(new Random(semilla), 5);
        assertEquals(List.of("Compilacion Exitosa", "", "[SinErrores]"), compilar(programa("rb", expresion)),
                expresion.texto());
    }

    // Con una sola hoja del tipo equivocado, el error es el del nodo que la
    // contiene: cada nodo chequea a sus hijos antes que a sí mismo.
    @Property(tries = 1500, seed = "20261016")
    void unaHojaMalTipadaFallaEnElNodoQueLaContiene(@ForAll long semilla) {
        Random azar = new Random(semilla);
        boolean entera = azar.nextBoolean();
        ExpresionAleatoria expresion = entera ? ExpresionAleatoria.entera(azar, 5) : ExpresionAleatoria.booleana(azar, 5);
        ExpresionAleatoria.Mutacion mutacion = ExpresionAleatoria.conUnaHojaMalTipada(expresion, azar);
        List<String> salida = compilar(programa(entera ? "ri" : "rb", mutacion.expresion()));

        assertEquals(3, salida.size(), salida.toString());
        assertEquals("[Error:" + mutacion.lexema() + "|" + LINEA_DE_LA_EXPRESION + "]", salida.getLast(),
                mutacion.expresion().texto());
    }

    private List<String> compilar(String fuente) {
        try {
            Files.writeString(ARCHIVO, fuente);
            SalidaEnMemoria salida = new SalidaEnMemoria();
            ModuloPrincipalSemantico.conChequeoDeSentencias(salida).ejecutar(ARCHIVO.toString());
            return salida.getLineas();
        } catch (IOException excepcion) {
            throw new UncheckedIOException(excepcion);
        }
    }
}
