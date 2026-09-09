package minijava.sintactico;

import minijava.aplicacion.ModuloPrincipalSintactico;
import minijava.comun.config.ConfiguracionDelCompilador;
import minijava.comun.config.Logro;
import minijava.comun.salida.SalidaEnMemoria;
import minijava.logros.ConfiguracionDeLaCorrida;
import minijava.logros.RequiereLogro;
import minijava.logros.RequiereLogroApagado;
import minijava.sintactico.errores.ExcepcionSintactica;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

// El logro Recuperación de Errores Modo Pánico: al detectar un error el
// analizador descarta entrada hasta un token de sincronización y sigue, en vez
// de terminar en el primero.
@Tag("sintactico")
class ModoPanicoTest extends BaseDeAnalisis {

    private ConfiguracionDelCompilador conPanico() {
        return ConfiguracionDeLaCorrida.actual().con(Logro.MODO_PANICO);
    }

    private ConfiguracionDelCompilador sinPanico() {
        return ConfiguracionDeLaCorrida.actual().sin(Logro.MODO_PANICO);
    }

    private List<String> lexemasDeLosErrores(Path dir, String fuente,
                                             ConfiguracionDelCompilador configuracion)
            throws Exception {
        return erroresDe(dir, fuente, configuracion).stream()
                .map(ExcepcionSintactica::getLexemaOfensivo).toList();
    }

    private List<String> salidaDe(Path dir, String fuente, ConfiguracionDelCompilador configuracion)
            throws Exception {
        Path archivo = dir.resolve("fuente.mjava");
        Files.writeString(archivo, fuente);
        SalidaEnMemoria salida = new SalidaEnMemoria();
        new ModuloPrincipalSintactico(salida, configuracion).ejecutar(archivo.toString());
        return salida.getLineas();
    }

    // Cuatro errores independientes: uno por miembro y dos dentro de un cuerpo.
    private static final String CUATRO_ERRORES = """
            class C1 {
              int x
              void m(){
                var a = ;
                b = 1;
                c = ;
              }
              void n(,){ }
              int y;
            }
            class C2 {
              void p(){ }
            }
            """;

    @Test
    @RequiereLogro(Logro.MODO_PANICO)
    void conModoPanicoSeReportanTodosLosErroresYNoSoloElPrimero(@TempDir Path dir) throws Exception {
        assertEquals(List.of("void", ";", ";", ","), lexemasDeLosErrores(dir, CUATRO_ERRORES, conPanico()));
    }

    @Test
    void sinModoPanicoElAnalisisCortaEnElPrimerError(@TempDir Path dir) throws Exception {
        assertEquals(List.of("void"), lexemasDeLosErrores(dir, CUATRO_ERRORES, sinPanico()));
    }

    // La invariante que hace que un caso del corpus valga para las dos
    // configuraciones: el logro agrega errores al final, nunca cambia el primero.
    @Test
    void elPrimerErrorEsElMismoConElLogroYSinEl(@TempDir Path dir) throws Exception {
        for (String fuente : List.of(
                CUATRO_ERRORES,
                "class C1 { }\nclass { }\n",
                envolverEnMetodo("if (a) ; else ; else ;"),
                envolverEnMetodo("x = ;"),
                "interface I1 { void m(){} }\n",
                "class C1 { void m(){ } ")) {
            assertEquals(lexemasDeLosErrores(dir, fuente, sinPanico()).getFirst(),
                    lexemasDeLosErrores(dir, fuente, conPanico()).getFirst(), fuente);
        }
    }

    @Test
    @RequiereLogro(Logro.MODO_PANICO)
    void unaLineaDeErrorNuncaRetrocede(@TempDir Path dir) throws Exception {
        int anterior = 0;
        for (ExcepcionSintactica error : erroresDe(dir, CUATRO_ERRORES, conPanico())) {
            assertTrue(error.getLinea() >= anterior, "las lineas retroceden");
            anterior = error.getLinea();
        }
    }

    // El cuerpo de un método cuya firma está rota se saltea entero: si no, su
    // contenido se leería como si fuera otro miembro y cada sentencia daría un
    // error inventado.
    @Test
    @RequiereLogro(Logro.MODO_PANICO)
    void elCuerpoDeUnMetodoConLaFirmaRotaNoInventaErrores(@TempDir Path dir) throws Exception {
        String fuente = "class C1 { void m(int){ x = 1; y = 2; z = 3; } int b; }\n";

        assertEquals(List.of(")"), lexemasDeLosErrores(dir, fuente, conPanico()));
    }

    // Un miembro correcto que viene después de uno roto se sigue analizando: el
    // punto de sincronización incluye los Primeros del miembro siguiente.
    @Test
    @RequiereLogro(Logro.MODO_PANICO)
    void unMiembroCorrectoDespuesDeUnoRotoNoSePierde(@TempDir Path dir) throws Exception {
        String fuente = "class C1 { int x  void m(){ y = ; } }\n";

        assertEquals(List.of("void", ";"), lexemasDeLosErrores(dir, fuente, conPanico()));
    }

    // El error cae justo en un token que también arranca un miembro válido, y el
    // miembro vuelve a fallar ahí mismo. Sin la garantía de progreso el
    // analizador daría vueltas para siempre sobre el mismo token.
    @Test
    @RequiereLogro(Logro.MODO_PANICO)
    void unErrorQueCaeEnElComienzoDeOtroElementoNoCiclaParaSiempre(@TempDir Path dir) {
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            assertFalse(lexemasDeLosErrores(dir, "class C1 { int int; }\n", conPanico()).isEmpty());
            assertFalse(lexemasDeLosErrores(dir, "class C1 { void m(){ if if ; } }\n",
                    conPanico()).isEmpty());
            assertFalse(lexemasDeLosErrores(dir, "class class class\n", conPanico()).isEmpty());
        });
    }

    @Test
    @RequiereLogro(Logro.MODO_PANICO)
    void unFuenteValidoNoCambiaConElLogroPrendido(@TempDir Path dir) throws Exception {
        String fuente = "class C1 { int x; void m(){ var a = 1; } }\nclass C2 { }\n";

        assertTrue(erroresDe(dir, fuente, conPanico()).isEmpty());
        assertEquals(List.of("Compilacion Exitosa", "", "[SinErrores]"),
                salidaDe(dir, fuente, conPanico()));
    }

    // Con errores nunca se imprime el código de éxito, aunque el análisis haya
    // seguido después del primero.
    @Test
    @RequiereLogro(Logro.MODO_PANICO)
    void conErroresNuncaSeImprimeSinErrores(@TempDir Path dir) throws Exception {
        List<String> salida = salidaDe(dir, CUATRO_ERRORES, conPanico());

        assertFalse(salida.contains("[SinErrores]"));
        assertEquals(4, salida.stream().filter(l -> l.startsWith("[Error:")).count());
    }

    // Un error LÉXICO corta el análisis, pero no puede llevarse puestos los
    // errores sintácticos que la recuperación ya había encontrado: son anteriores
    // en el fuente y hay que reportarlos igual.
    @Test
    @RequiereLogro(Logro.MODO_PANICO)
    @RequiereLogroApagado(Logro.TERNARIO)
    void unErrorLexicoNoSeLlevaPuestosLosSintacticosYaEncontrados(@TempDir Path dir)
            throws Exception {
        // Sin el logro de ternario, el `?` no es un token de MiniJava.
        List<String> salida = salidaDe(dir, "class C1 { int x  void m(){ } }\nclass ?\n",
                conPanico());

        assertTrue(salida.contains("[Error:void|1]"), salida.toString());
        assertTrue(salida.stream().anyMatch(l -> l.startsWith("[Error:?")), salida.toString());
    }

    // El formato de cada reporte es el mismo de Pautas slide 25, repetido: el
    // mensaje, un renglón en blanco y el código.
    @Test
    @RequiereLogro(Logro.MODO_PANICO)
    void cadaErrorSeReportaConElFormatoCompletoDeLasPautas(@TempDir Path dir) throws Exception {
        List<String> salida = salidaDe(dir, "class C1 { int x  int y  }\n", conPanico());

        assertEquals(6, salida.size());
        assertTrue(salida.get(0).startsWith("Error Sintactico en linea 1: "));
        assertEquals("", salida.get(1));
        assertEquals("[Error:int|1]", salida.get(2));
        assertTrue(salida.get(3).startsWith("Error Sintactico en linea 1: "));
        assertEquals("", salida.get(4));
        assertEquals("[Error:}|1]", salida.get(5));
    }
}
