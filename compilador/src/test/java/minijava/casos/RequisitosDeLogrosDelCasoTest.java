package minijava.casos;

import minijava.casos.RequisitosDeLogrosDelCaso.Fase;
import minijava.comun.config.ConfiguracionDelCompilador;
import minijava.comun.config.Logro;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("comun")
class RequisitosDeLogrosDelCasoTest {

    private static final Path RAIZ = Path.of("resources", "lexico");
    private static final Path MANIFIESTO = RAIZ.resolve("requisitos-de-logros.txt");

    @Test
    void unCasoSinRequisitosAplicaACualquierConfiguracion() {
        var requisitos = RequisitosDeLogrosDelCaso.de(Fase.LEXICO, RAIZ.resolve("sinErrores/lexSinErrores01.java"));

        assertTrue(requisitos.seCumplenEn(ConfiguracionDelCompilador.ninguno()));
        assertTrue(requisitos.seCumplenEn(ConfiguracionDelCompilador.todos()));
        assertEquals("", requisitos.describirIncumplidos(ConfiguracionDelCompilador.ninguno()));
    }

    @Test
    void unCasoQueExigeUnLogroPrendidoNoAplicaSinEl() {
        var requisitos = RequisitosDeLogrosDelCaso.de(Fase.LEXICO, RAIZ.resolve("sinErrores/floats.java"));

        assertTrue(requisitos.seCumplenEn(ConfiguracionDelCompilador.todos()));
        assertFalse(requisitos.seCumplenEn(ConfiguracionDelCompilador.todos().sin(Logro.FLOAT)));
        assertTrue(requisitos.describirIncumplidos(ConfiguracionDelCompilador.ninguno())
                .contains("float tiene que estar prendido"));
    }

    @Test
    void unCasoQueExigeUnLogroApagadoNoAplicaConEl() {
        var requisitos = RequisitosDeLogrosDelCaso.de(Fase.LEXICO, RAIZ.resolve("conErrores/preguntaSuelta.java"));

        assertTrue(requisitos.seCumplenEn(ConfiguracionDelCompilador.soloEtapa(1)));
        assertFalse(requisitos.seCumplenEn(ConfiguracionDelCompilador.todos()));
        assertTrue(requisitos.describirIncumplidos(ConfiguracionDelCompilador.todos())
                .contains("ternario tiene que estar apagado"));
    }

    @Test
    void elCasoSeIdentificaIgualPorRutaCompletaQuePorDirectorioYNombre() {
        assertEquals(RequisitosDeLogrosDelCaso.de(Fase.LEXICO, RAIZ.resolve("sinErrores/floats.java")),
                RequisitosDeLogrosDelCaso.de(Fase.LEXICO, Path.of("sinErrores", "floats.java")));
    }

    // Un caso declarado en el manifiesto que después se renombra o se borra
    // dejaría una línea muerta que nunca se aplica y nadie notaría.
    @Test
    void todoCasoDelManifiestoExisteEnElCorpus() throws IOException {
        List<String> inexistentes = new ArrayList<>();
        for (String caso : casosDeclarados()) {
            if (!Files.isRegularFile(RAIZ.resolve(caso))) {
                inexistentes.add(caso);
            }
        }

        assertEquals(List.of(), inexistentes, MANIFIESTO + ": declaran casos que no existen");
    }

    // El manifiesto describe una dependencia real del compilador: si un caso
    // declara que necesita un logro y con ese logro apagado la salida es la
    // misma, la línea sobra y confunde.
    @Test
    void todoRequisitoDeclaradoCorrespondeAUnaDependenciaReal() throws IOException {
        List<String> falsos = new ArrayList<>();
        for (String caso : casosDeclarados()) {
            var requisitos = RequisitosDeLogrosDelCaso.de(Fase.LEXICO, RAIZ.resolve(caso));
            for (Logro logro : requisitos.prendidos()) {
                if (!laSalidaCambiaAlPrender(caso, logro)) {
                    falsos.add(caso + " declara requerir " + logro.getNombre() + " prendido");
                }
            }
            for (Logro logro : requisitos.apagados()) {
                if (!laSalidaCambiaAlPrender(caso, logro)) {
                    falsos.add(caso + " declara requerir " + logro.getNombre() + " apagado");
                }
            }
        }

        assertEquals(List.of(), falsos, MANIFIESTO + ": requisitos que no cambian nada");
    }

    private boolean laSalidaCambiaAlPrender(String caso, Logro logro) throws IOException {
        var base = ConfiguracionDelCompilador.todos();
        return !salidaDe(caso, base.con(logro)).equals(salidaDe(caso, base.sin(logro)));
    }

    private List<String> salidaDe(String caso, ConfiguracionDelCompilador configuracion)
            throws IOException {
        var salida = new minijava.comun.salida.SalidaEnMemoria();
        new minijava.aplicacion.ModuloPrincipalLexico(salida, configuracion)
                .ejecutar(RAIZ.resolve(caso).toString());
        return salida.getLineas();
    }

    private List<String> casosDeclarados() throws IOException {
        List<String> casos = new ArrayList<>();
        for (String linea : Files.readAllLines(MANIFIESTO, StandardCharsets.UTF_8)) {
            String limpia = linea.strip();
            if (!limpia.isEmpty() && !limpia.startsWith("#")) {
                casos.add(limpia.split("\\|")[0].strip());
            }
        }
        return casos;
    }
}
