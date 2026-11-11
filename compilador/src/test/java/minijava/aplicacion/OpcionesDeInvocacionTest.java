package minijava.aplicacion;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("comun")
class OpcionesDeInvocacionTest {

    private OpcionesDeInvocacion parsear(String... argumentos) {
        return OpcionesDeInvocacion.parsear(argumentos);
    }

    // El contrato de las Pautas Etapa 5, slide 30: el fuente y el archivo de
    // salida, sin ninguna opción.
    @Test
    void conLaRutaYLaSalidaCorreLaEtapaCinco() {
        var opciones = parsear("programa1.java", "salida.txt");

        assertEquals(5, opciones.etapa());
        assertEquals("programa1.java", opciones.rutaFuente());
        assertEquals("salida.txt", opciones.rutaSalida());
        assertTrue(opciones.avisos().isEmpty());
    }

    @Test
    void conSoloLaRutaNoHaySalida() {
        var opciones = parsear("programa1.java");

        assertEquals("programa1.java", opciones.rutaFuente());
        assertNull(opciones.rutaSalida());
        assertTrue(opciones.avisos().isEmpty());
    }

    @Test
    void sinArgumentosNoHayRutaYLaEtapaEsLaPorDefecto() {
        var opciones = parsear();

        assertNull(opciones.rutaFuente());
        assertEquals(OpcionesDeInvocacion.ETAPA_POR_DEFECTO, opciones.etapa());
    }

    // Cada etapa anterior se corrige por separado y es ejecutable de forma aislada.
    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4})
    void unaEtapaAnteriorSeSeleccionaConSuOpcion(int etapa) {
        assertEquals(etapa, parsear("--etapa=" + etapa, "programa1.java").etapa());
    }

    @Test
    void laEtapaCincoExplicitaEsLoMismoQueNoIndicarNinguna() {
        assertEquals(parsear("fuente.java").etapa(), parsear("--etapa=5", "fuente.java").etapa());
    }

    @Test
    void elOrdenDeLasOpcionesYLaRutaEsIndistinto() {
        assertEquals(parsear("--etapa=1", "fuente.java"), parsear("fuente.java", "--etapa=1"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"--etapa=0", "--etapa=6", "--etapa=x", "--etapa="})
    void unaEtapaNoSoportadaAvisaYCaeEnLaPorDefecto(String argumento) {
        var opciones = parsear(argumento, "fuente.java");

        assertEquals(OpcionesDeInvocacion.ETAPA_POR_DEFECTO, opciones.etapa());
        assertEquals(1, opciones.avisos().size());
        assertTrue(opciones.avisos().getFirst().contains("Etapa no soportada"),
                opciones.avisos().toString());
        assertEquals("fuente.java", opciones.rutaFuente());
    }

    @Test
    void unaOpcionDesconocidaAvisaYNoSeConfundeConLaRuta() {
        var opciones = parsear("--inventada", "fuente.java");

        assertEquals("fuente.java", opciones.rutaFuente());
        assertEquals(1, opciones.avisos().size());
        assertTrue(opciones.avisos().getFirst().contains("--inventada"), opciones.avisos().toString());
    }

    @Test
    void unTercerArchivoAvisaYSeQuedaConLosDosPrimeros() {
        var opciones = parsear("uno.java", "salida.txt", "dos.java");

        assertEquals("uno.java", opciones.rutaFuente());
        assertEquals("salida.txt", opciones.rutaSalida());
        assertEquals(1, opciones.avisos().size());
        assertTrue(opciones.avisos().getFirst().contains("dos.java"), opciones.avisos().toString());
    }

    @Test
    void laSalidaTambienSeAceptaConCualquierExtensionYAntesDeLasOpciones() {
        var opciones = parsear("fuente.mjava", "salida", "--etapa=5");

        assertEquals("salida", opciones.rutaSalida());
        assertEquals(5, opciones.etapa());
    }

    // Cualquier extensión, incluida ninguna: lo exigen las Pautas explícitamente.
    @ParameterizedTest
    @ValueSource(strings = {"programa1.java", "fuente.mjava", "sinExtension", "a.b.c", "-guion.java"})
    void laRutaSeAceptaConCualquierExtension(String ruta) {
        assertEquals(ruta, parsear(ruta).rutaFuente());
    }

    @Test
    void seAcumulaUnAvisoPorCadaProblemaDistinto() {
        var opciones = parsear("--etapa=9", "--inventada", "uno.java", "salida.txt", "dos.java");

        assertEquals(3, opciones.avisos().size(), opciones.avisos().toString());
    }

    @Test
    void losAvisosNoSePuedenModificarDesdeAfuera() {
        var avisos = parsear("--inventada", "fuente.java").avisos();

        org.junit.jupiter.api.Assertions.assertThrows(
                UnsupportedOperationException.class, () -> avisos.add("otro"));
    }
}
