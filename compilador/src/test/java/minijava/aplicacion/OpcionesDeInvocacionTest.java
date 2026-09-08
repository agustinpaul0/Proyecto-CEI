package minijava.aplicacion;

import minijava.comun.config.ConfiguracionDelCompilador;
import minijava.comun.config.Logro;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("comun")
class OpcionesDeInvocacionTest {

    private OpcionesDeInvocacion parsear(String... argumentos) {
        return OpcionesDeInvocacion.parsear(argumentos);
    }

    // El contrato de las Pautas: un único argumento, la ruta, sin ninguna opción.
    @Test
    void conSoloLaRutaCorreLaEtapaDosConTodosLosLogros() {
        var opciones = parsear("programa1.java");

        assertEquals(2, opciones.etapa());
        assertEquals("programa1.java", opciones.rutaFuente().orElseThrow());
        assertEquals(ConfiguracionDelCompilador.todos(), opciones.configuracion());
        assertTrue(opciones.avisos().isEmpty());
    }

    @Test
    void sinArgumentosNoHayRutaYLaEtapaEsLaPorDefecto() {
        var opciones = parsear();

        assertTrue(opciones.rutaFuente().isEmpty());
        assertEquals(OpcionesDeInvocacion.ETAPA_POR_DEFECTO, opciones.etapa());
    }

    // Correr la Etapa 1 reproduce la entrega de la Etapa 1: sus logros, y ninguno
    // de los cuatro tokens que la Etapa 2 agrega.
    @Test
    void laEtapaUnoTraeSoloLosLogrosDeLaEtapaUno() {
        var opciones = parsear("--etapa=1", "programa1.java");

        assertEquals(1, opciones.etapa());
        assertEquals(ConfiguracionDelCompilador.soloEtapa(1), opciones.configuracion());
        assertFalse(opciones.configuracion().tiene(Logro.FOR));
    }

    @Test
    void laEtapaDosExplicitaEsLoMismoQueNoIndicarNinguna() {
        assertEquals(parsear("fuente.java").configuracion(),
                parsear("--etapa=2", "fuente.java").configuracion());
    }

    @Test
    void elOrdenDeLasOpcionesYLaRutaEsIndistinto() {
        var antes = parsear("--etapa=1", "--logros=ternario", "fuente.java");
        var despues = parsear("fuente.java", "--logros=ternario", "--etapa=1");

        assertEquals(antes, despues);
    }

    // --logros explícito manda por encima del default que fija la etapa.
    @Test
    void logrosExplicitoPisaAlDefaultDeLaEtapa() {
        var opciones = parsear("--etapa=1", "--logros=ternario", "fuente.java");

        assertEquals(ConfiguracionDelCompilador.ninguno().con(Logro.TERNARIO),
                opciones.configuracion());
    }

    @Test
    void logrosNingunoApagaTodoInclusoEnLaEtapaDos() {
        assertEquals(ConfiguracionDelCompilador.ninguno(),
                parsear("--logros=ninguno", "fuente.java").configuracion());
    }

    @ParameterizedTest
    @ValueSource(strings = {"--etapa=0", "--etapa=3", "--etapa=x", "--etapa="})
    void unaEtapaNoSoportadaAvisaYCaeEnLaPorDefecto(String argumento) {
        var opciones = parsear(argumento, "fuente.java");

        assertEquals(OpcionesDeInvocacion.ETAPA_POR_DEFECTO, opciones.etapa());
        assertEquals(1, opciones.avisos().size());
        assertTrue(opciones.avisos().getFirst().contains("Etapa no soportada"),
                opciones.avisos().toString());
        assertEquals("fuente.java", opciones.rutaFuente().orElseThrow());
    }

    @Test
    void unaOpcionDesconocidaAvisaYNoSeConfundeConLaRuta() {
        var opciones = parsear("--inventada", "fuente.java");

        assertEquals("fuente.java", opciones.rutaFuente().orElseThrow());
        assertEquals(1, opciones.avisos().size());
        assertTrue(opciones.avisos().getFirst().contains("--inventada"), opciones.avisos().toString());
    }

    @Test
    void unLogroDesconocidoAvisaYNoAborta() {
        var opciones = parsear("--logros=inventado", "fuente.java");

        assertEquals(ConfiguracionDelCompilador.ninguno(), opciones.configuracion());
        assertEquals(1, opciones.avisos().size());
        assertTrue(opciones.avisos().getFirst().contains("inventado"), opciones.avisos().toString());
    }

    @Test
    void unSegundoArchivoAvisaYSeQuedaConElPrimero() {
        var opciones = parsear("uno.java", "dos.java");

        assertEquals("uno.java", opciones.rutaFuente().orElseThrow());
        assertEquals(1, opciones.avisos().size());
        assertTrue(opciones.avisos().getFirst().contains("dos.java"), opciones.avisos().toString());
    }

    // Cualquier extensión, incluida ninguna: lo exigen las Pautas explícitamente.
    @ParameterizedTest
    @ValueSource(strings = {"programa1.java", "fuente.mjava", "sinExtension", "a.b.c", "-guion.java"})
    void laRutaSeAceptaConCualquierExtension(String ruta) {
        assertEquals(ruta, parsear(ruta).rutaFuente().orElseThrow());
    }

    @Test
    void seAcumulaUnAvisoPorCadaProblemaDistinto() {
        var opciones = parsear("--etapa=9", "--inventada", "--logros=nada", "uno.java", "dos.java");

        assertEquals(4, opciones.avisos().size(), opciones.avisos().toString());
    }

    @Test
    void losAvisosNoSePuedenModificarDesdeAfuera() {
        var avisos = parsear("--inventada", "fuente.java").avisos();

        org.junit.jupiter.api.Assertions.assertThrows(
                UnsupportedOperationException.class, () -> avisos.add("otro"));
    }
}
