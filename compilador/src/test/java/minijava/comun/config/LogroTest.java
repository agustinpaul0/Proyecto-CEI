package minijava.comun.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogroTest {

    @ParameterizedTest
    @EnumSource(Logro.class)
    void todoLogroSeRecuperaPorSuPropioNombre(Logro logro) {
        assertEquals(logro, Logro.porNombre(logro.getNombre()).orElseThrow());
    }

    @Test
    void losNombresNoSeRepiten() {
        Set<String> nombres = new HashSet<>();
        for (Logro logro : Logro.values()) {
            assertTrue(nombres.add(logro.getNombre()), "nombre repetido: " + logro.getNombre());
        }
    }

    @ParameterizedTest
    @EnumSource(Logro.class)
    void todoLogroPerteneceAUnaEtapaConocida(Logro logro) {
        assertTrue(logro.getEtapa() == 1 || logro.getEtapa() == 2, logro + " -> " + logro.getEtapa());
    }

    @ParameterizedTest
    @ValueSource(strings = {"FLOATS", "Floats", "  floats  ", "FLOAT", "float"})
    void elNombreSeReconoceSinImportarMayusculasNiEspacios(String escrito) {
        assertSame(Logro.FLOATS, Logro.porNombre(escrito).orElseThrow());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "inexistente", "floats2", "ternarios"})
    void unNombreQueNoEsDeNingunLogroNoDevuelveNada(String escrito) {
        assertTrue(Logro.porNombre(escrito).isEmpty(), escrito);
    }

    // Los nombres son los flags con los que gramatica/minijava-ll1-logros.g marca
    // sus producciones: si divergen, el enum y la gramática dejan de leerse uno
    // contra el otro. Los cuatro de Etapa 1 que no aparecen en la gramática y el
    // modo pánico —que no es una producción— son la excepción esperada.
    @Test
    void losLogrosDeGramaticaUsanElMismoNombreQueLosFlagsDelArchivoDeGramatica() {
        Set<String> flagsDeLaGramatica = Set.of(
                "visibilidad", "atrinit", "varclasica", "for", "ternario",
                "posfijo", "lambda", "arrinit", "generico");

        Set<String> deEtapaDos = new HashSet<>();
        for (Logro logro : Logro.values()) {
            if (logro.getEtapa() == 2 && logro != Logro.MODO_PANICO) {
                deEtapaDos.add(logro.getNombre());
            }
        }

        assertEquals(flagsDeLaGramatica, deEtapaDos);
    }

    @Test
    void elFlagFloatDeLaGramaticaEsUnAliasYNoUnLogroPropio() {
        assertFalse(Logro.porNombre("float").isEmpty());
        assertEquals("floats", Logro.FLOATS.getNombre());
    }
}
