package minijava.comun.config;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("comun")
class ParserDeConfiguracionTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    void sinEspecificacionSePrendenTodosLosLogros(String especificacion) {
        var resultado = ParserDeConfiguracion.parsear(especificacion);

        assertEquals(ConfiguracionDelCompilador.todos(), resultado.configuracion());
        assertTrue(resultado.avisos().isEmpty());
    }

    @Test
    void ningunoApagaTodo() {
        var resultado = ParserDeConfiguracion.parsear("ninguno");

        assertEquals(ConfiguracionDelCompilador.ninguno(), resultado.configuracion());
        assertTrue(resultado.avisos().isEmpty());
    }

    @Test
    void todosPrendeTodo() {
        assertEquals(ConfiguracionDelCompilador.todos(),
                ParserDeConfiguracion.parsear("todos").configuracion());
    }

    @Test
    void unaListaPrendeExactamenteEsosLogros() {
        var configuracion = ParserDeConfiguracion.parsear("ternario,lambda").configuracion();

        assertEquals(ConfiguracionDelCompilador.ninguno().con(Logro.TERNARIO, Logro.LAMBDA),
                configuracion);
    }

    @Test
    void unSoloLogroPrendeSoloEseLogro() {
        var configuracion = ParserDeConfiguracion.parsear("for").configuracion();

        assertEquals(ConfiguracionDelCompilador.ninguno().con(Logro.FOR), configuracion);
    }

    @ParameterizedTest
    @ValueSource(strings = {"  TERNARIO , Lambda  ", "ternario,,lambda", ",ternario,lambda,"})
    void seToleranEspaciosMayusculasYSeparadoresDeMas(String especificacion) {
        assertEquals(ConfiguracionDelCompilador.ninguno().con(Logro.TERNARIO, Logro.LAMBDA),
                ParserDeConfiguracion.parsear(especificacion).configuracion());
    }

    @Test
    void etapaNPrendeLosLogrosDeEsaEtapa() {
        assertEquals(ConfiguracionDelCompilador.soloEtapa(1),
                ParserDeConfiguracion.parsear("etapa1").configuracion());
        assertEquals(ConfiguracionDelCompilador.soloEtapa(2),
                ParserDeConfiguracion.parsear("etapa2").configuracion());
    }

    @Test
    void lasEtapasSeAcumulanConLosLogrosSueltos() {
        var configuracion = ParserDeConfiguracion.parsear("etapa1,ternario").configuracion();

        assertTrue(configuracion.tiene(Logro.FLOAT));
        assertTrue(configuracion.tiene(Logro.TERNARIO));
        assertFalse(configuracion.tiene(Logro.LAMBDA));
    }

    // El orden importa: `ninguno` limpia lo acumulado hasta ahí, no lo que venga
    // después. Es lo que permite escribir --logros=todos,... y --logros=ninguno,for.
    @Test
    void ningunoLimpiaLoAcumuladoHastaEseMomento() {
        var configuracion = ParserDeConfiguracion.parsear("todos,ninguno,for").configuracion();

        assertEquals(ConfiguracionDelCompilador.ninguno().con(Logro.FOR), configuracion);
    }

    @Test
    void todosDespuesDeNingunoVuelveAPrenderTodo() {
        assertEquals(ConfiguracionDelCompilador.todos(),
                ParserDeConfiguracion.parsear("ninguno,todos").configuracion());
    }

    @Test
    void unLogroRepetidoSeCuentaUnaSolaVez() {
        assertEquals(ConfiguracionDelCompilador.ninguno().con(Logro.FOR),
                ParserDeConfiguracion.parsear("for,for,for").configuracion());
    }

    @Test
    void unNombreDesconocidoAvisaYNoAborta() {
        var resultado = ParserDeConfiguracion.parsear("for,inventado,lambda");

        assertEquals(ConfiguracionDelCompilador.ninguno().con(Logro.FOR, Logro.LAMBDA),
                resultado.configuracion());
        assertEquals(1, resultado.avisos().size());
        assertTrue(resultado.avisos().getFirst().contains("inventado"), resultado.avisos().toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"etapa", "etapax", "etapa1.5"})
    void unaEtapaMalEscritaAvisaYNoAborta(String especificacion) {
        var resultado = ParserDeConfiguracion.parsear(especificacion);

        assertEquals(ConfiguracionDelCompilador.ninguno(), resultado.configuracion());
        assertEquals(1, resultado.avisos().size());
        assertTrue(resultado.avisos().getFirst().contains("no reconocida"),
                resultado.avisos().toString());
    }

    @Test
    void unaEtapaSinLogrosAvisaYNoAborta() {
        var resultado = ParserDeConfiguracion.parsear("etapa9");

        assertEquals(ConfiguracionDelCompilador.ninguno(), resultado.configuracion());
        assertEquals(1, resultado.avisos().size());
        assertTrue(resultado.avisos().getFirst().contains("etapa 9"), resultado.avisos().toString());
    }

    @Test
    void seAcumulaUnAvisoPorCadaNombreInvalido() {
        var resultado = ParserDeConfiguracion.parsear("uno,dos,tres");

        assertEquals(3, resultado.avisos().size());
    }

    @Test
    void losAvisosNoSePuedenModificarDesdeAfuera() {
        var avisos = ParserDeConfiguracion.parsear("inventado").avisos();

        org.junit.jupiter.api.Assertions.assertThrows(
                UnsupportedOperationException.class, () -> avisos.add("otro"));
    }

    @Test
    void laPropiedadDeSistemaAlimentaLaConfiguracion() {
        String anterior = System.getProperty(ParserDeConfiguracion.PROPIEDAD_DE_SISTEMA);
        try {
            System.setProperty(ParserDeConfiguracion.PROPIEDAD_DE_SISTEMA, "ninguno");

            assertEquals(ConfiguracionDelCompilador.ninguno(),
                    ParserDeConfiguracion.desdePropiedadDeSistema().configuracion());
        } finally {
            if (anterior == null) {
                System.clearProperty(ParserDeConfiguracion.PROPIEDAD_DE_SISTEMA);
            } else {
                System.setProperty(ParserDeConfiguracion.PROPIEDAD_DE_SISTEMA, anterior);
            }
        }
    }
}
