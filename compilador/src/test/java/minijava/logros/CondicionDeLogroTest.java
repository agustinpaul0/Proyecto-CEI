package minijava.logros;

import minijava.comun.config.Logro;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// La condición no se puede testear "en frío" —depende de -Dlogros, que es de la
// corrida entera—, así que se verifica por su efecto observable: un test anotado
// o corre y ve su logro en el estado que pidió, o no corre. Nunca corre con el
// logro al revés.
@Tag("comun")
class CondicionDeLogroTest {

    @Test
    void sinAnotacionElTestCorreSiempre() {
        assertTrue(true);
    }

    @Test
    @RequiereLogro(Logro.TERNARIO)
    void unTestQueExigeUnLogroSoloCorreConEsePrendido() {
        assertTrue(ConfiguracionDeLaCorrida.actual().tiene(Logro.TERNARIO));
    }

    @Test
    @RequiereLogroApagado(Logro.TERNARIO)
    void unTestQueExigeUnLogroApagadoSoloCorreConEseApagado() {
        assertFalse(ConfiguracionDeLaCorrida.actual().tiene(Logro.TERNARIO));
    }

    @Test
    @RequiereLogro({Logro.TERNARIO, Logro.LAMBDA})
    void seExigenTodosLosLogrosDeLaLista() {
        assertTrue(ConfiguracionDeLaCorrida.actual().tiene(Logro.TERNARIO));
        assertTrue(ConfiguracionDeLaCorrida.actual().tiene(Logro.LAMBDA));
    }

    // Las dos anotaciones a la vez: es el caso de un logro que cambia de
    // comportamiento sólo cuando otro está apagado.
    @Test
    @RequiereLogro(Logro.TERNARIO)
    @RequiereLogroApagado(Logro.MODO_PANICO)
    void lasDosAnotacionesJuntasSeCumplenAlMismoTiempo() {
        assertTrue(ConfiguracionDeLaCorrida.actual().tiene(Logro.TERNARIO));
        assertFalse(ConfiguracionDeLaCorrida.actual().tiene(Logro.MODO_PANICO));
    }

    @Nested
    @RequiereLogro(Logro.FOR)
    class AnotadaEnLaClase {

        @Test
        void laAnotacionDeLaClaseAlcanzaASusMetodos() {
            assertTrue(ConfiguracionDeLaCorrida.actual().tiene(Logro.FOR));
        }

        @Test
        @RequiereLogro(Logro.LAMBDA)
        void laAnotacionDelMetodoSeSumaALaDeLaClase() {
            assertTrue(ConfiguracionDeLaCorrida.actual().tiene(Logro.FOR));
            assertTrue(ConfiguracionDeLaCorrida.actual().tiene(Logro.LAMBDA));
        }
    }
}
