package minijava.comun.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfiguracionDelCompiladorTest {

    @ParameterizedTest
    @EnumSource(Logro.class)
    void conNingunLogroNingunoEstaPrendido(Logro logro) {
        assertFalse(ConfiguracionDelCompilador.ninguno().tiene(logro));
    }

    @ParameterizedTest
    @EnumSource(Logro.class)
    void conTodosLosLogrosTodosEstanPrendidos(Logro logro) {
        assertTrue(ConfiguracionDelCompilador.todos().tiene(logro));
    }

    @Test
    void laConfiguracionPorDefectoEsLaDeTodosLosLogros() {
        assertEquals(ConfiguracionDelCompilador.todos(), ConfiguracionDelCompilador.DEFAULT);
    }

    @Test
    void soloEtapaDejaPrendidosExactamenteLosDeEsaEtapa() {
        var etapaUno = ConfiguracionDelCompilador.soloEtapa(1);

        for (Logro logro : Logro.values()) {
            assertEquals(logro.getEtapa() == 1, etapaUno.tiene(logro), logro.name());
        }
    }

    @Test
    void unaEtapaSinLogrosDaLaConfiguracionVacia() {
        assertEquals(ConfiguracionDelCompilador.ninguno(), ConfiguracionDelCompilador.soloEtapa(9));
    }

    @Test
    void lasDosEtapasJuntasSonTodosLosLogros() {
        var union = ConfiguracionDelCompilador.soloEtapa(1)
                .con(ConfiguracionDelCompilador.soloEtapa(2).getLogros().toArray(new Logro[0]));

        assertEquals(ConfiguracionDelCompilador.todos(), union);
    }

    @Test
    void conAgregaSinTocarLaConfiguracionOriginal() {
        var base = ConfiguracionDelCompilador.ninguno();

        var derivada = base.con(Logro.TERNARIO, Logro.LAMBDA);

        assertTrue(derivada.tiene(Logro.TERNARIO));
        assertTrue(derivada.tiene(Logro.LAMBDA));
        assertFalse(base.tiene(Logro.TERNARIO));
    }

    @Test
    void sinQuitaSinTocarLaConfiguracionOriginal() {
        var base = ConfiguracionDelCompilador.todos();

        var derivada = base.sin(Logro.FLOATS);

        assertFalse(derivada.tiene(Logro.FLOATS));
        assertTrue(base.tiene(Logro.FLOATS));
    }

    @Test
    void agregarUnLogroQueYaEstabaNoCambiaNada() {
        var todos = ConfiguracionDelCompilador.todos();

        assertEquals(todos, todos.con(Logro.FOR));
    }

    @Test
    void quitarUnLogroQueNoEstabaNoCambiaNada() {
        var ninguno = ConfiguracionDelCompilador.ninguno();

        assertEquals(ninguno, ninguno.sin(Logro.FOR));
    }

    @Test
    void conYSinSinArgumentosDevuelvenLaMismaConfiguracion() {
        var base = ConfiguracionDelCompilador.todos().sin(Logro.FLOATS);

        assertEquals(base, base.con());
        assertEquals(base, base.sin());
    }

    @Test
    void deConstruyeDesdeUnConjuntoCualquiera() {
        var configuracion = ConfiguracionDelCompilador.de(Set.of(Logro.FOR, Logro.POSFIJO));

        assertTrue(configuracion.tiene(Logro.FOR));
        assertTrue(configuracion.tiene(Logro.POSFIJO));
        assertFalse(configuracion.tiene(Logro.LAMBDA));
    }

    @Test
    void deConUnConjuntoVacioDaLaConfiguracionSinLogros() {
        assertEquals(ConfiguracionDelCompilador.ninguno(), ConfiguracionDelCompilador.de(Set.of()));
    }

    @Test
    void elConjuntoExpuestoNoSePuedeModificarDesdeAfuera() {
        var logros = ConfiguracionDelCompilador.ninguno().getLogros();

        assertThrows(UnsupportedOperationException.class, () -> logros.add(Logro.FOR));
    }

    @Test
    void modificarElConjuntoOrigenNoAfectaAUnaConfiguracionYaConstruida() {
        EnumSet<Logro> origen = EnumSet.of(Logro.FOR);
        var configuracion = ConfiguracionDelCompilador.de(origen);

        origen.add(Logro.LAMBDA);

        assertFalse(configuracion.tiene(Logro.LAMBDA));
    }

    @Test
    void dosConfiguracionesConLosMismosLogrosSonIguales() {
        var una = ConfiguracionDelCompilador.ninguno().con(Logro.FOR, Logro.LAMBDA);
        var otra = ConfiguracionDelCompilador.ninguno().con(Logro.LAMBDA, Logro.FOR);

        assertEquals(una, otra);
        assertEquals(una.hashCode(), otra.hashCode());
    }

    @Test
    void configuracionesConDistintosLogrosNoSonIguales() {
        assertNotEquals(ConfiguracionDelCompilador.ninguno(), ConfiguracionDelCompilador.todos());
        assertNotEquals(ConfiguracionDelCompilador.ninguno(), "ninguno");
    }

    @Test
    void elToStringNombraLosLogrosPrendidos() {
        var configuracion = ConfiguracionDelCompilador.ninguno().con(Logro.FOR);

        assertTrue(configuracion.toString().contains("FOR"), configuracion.toString());
    }

    // Los cuatro accesores con nombre son los que consultan el léxico y el
    // formateador: tienen que seguir el estado del logro que envuelven.
    @Test
    void losAccesoresDeEtapaUnoSiguenAlLogroQueEnvuelven() {
        var todos = ConfiguracionDelCompilador.todos();
        var ninguno = ConfiguracionDelCompilador.ninguno();

        assertTrue(todos.floatsHabilitados());
        assertTrue(todos.unicodeHabilitado());
        assertTrue(todos.reporteElegante());
        assertTrue(todos.multiDeteccionDeErrores());

        assertFalse(ninguno.floatsHabilitados());
        assertFalse(ninguno.unicodeHabilitado());
        assertFalse(ninguno.reporteElegante());
        assertFalse(ninguno.multiDeteccionDeErrores());
    }
}
