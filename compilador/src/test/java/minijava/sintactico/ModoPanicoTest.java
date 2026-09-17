package minijava.sintactico;

import minijava.aplicacion.ModuloPrincipalSintactico;
import minijava.comun.salida.SalidaEnMemoria;
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

// Recuperación de Errores en Modo Pánico: al detectar un error el analizador
// descarta entrada hasta un token de sincronización y sigue, en vez de terminar
// en el primero.
@Tag("sintactico")
class ModoPanicoTest extends BaseDeAnalisis {

    private List<String> lexemasDeLosErrores(Path dir, String fuente) throws Exception {
        return erroresDe(dir, fuente).stream()
                .map(ExcepcionSintactica::getLexemaOfensivo).toList();
    }

    private List<String> salidaDe(Path dir, String fuente) throws Exception {
        Path archivo = dir.resolve("fuente.mjava");
        Files.writeString(archivo, fuente);
        SalidaEnMemoria salida = new SalidaEnMemoria();
        new ModuloPrincipalSintactico(salida).ejecutar(archivo.toString());
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
    void seReportanTodosLosErroresYNoSoloElPrimero(@TempDir Path dir) throws Exception {
        assertEquals(List.of("void", ";", ";", ","), lexemasDeLosErrores(dir, CUATRO_ERRORES));
    }

    // La recuperación agrega errores DESPUÉS del primero, nunca cambia cuál es el
    // primero: es el mismo que reportaría un analizador que cortara ahí, que es
    // la convención base de Pautas slide 25.
    @Test
    void elPrimerErrorEsElDelPrimerTokenQueNoEncaja(@TempDir Path dir) throws Exception {
        assertEquals("void", lexemasDeLosErrores(dir, CUATRO_ERRORES).getFirst());
        assertEquals("{", lexemasDeLosErrores(dir, "class C1 { }\nclass { }\n").getFirst());
        assertEquals("else",
                lexemasDeLosErrores(dir, envolverEnMetodo("if (a) ; else ; else ;")).getFirst());
        assertEquals(";", lexemasDeLosErrores(dir, envolverEnMetodo("x = ;")).getFirst());
        assertEquals("{", lexemasDeLosErrores(dir, "interface I1 { void m(){} }\n").getFirst());
    }

    @Test
    void unaLineaDeErrorNuncaRetrocede(@TempDir Path dir) throws Exception {
        int anterior = 0;
        for (ExcepcionSintactica error : erroresDe(dir, CUATRO_ERRORES)) {
            assertTrue(error.getLinea() >= anterior, "las lineas retroceden");
            anterior = error.getLinea();
        }
    }

    // El cuerpo de un método cuya firma está rota se saltea entero: si no, su
    // contenido se leería como si fuera otro miembro y cada sentencia daría un
    // error inventado.
    @Test
    void elCuerpoDeUnMetodoConLaFirmaRotaNoInventaErrores(@TempDir Path dir) throws Exception {
        String fuente = "class C1 { void m(int){ x = 1; y = 2; z = 3; } int b; }\n";

        assertEquals(List.of(")"), lexemasDeLosErrores(dir, fuente));
    }

    // Un miembro correcto que viene después de uno roto se sigue analizando: el
    // punto de sincronización incluye los Primeros del miembro siguiente.
    @Test
    void unMiembroCorrectoDespuesDeUnoRotoNoSePierde(@TempDir Path dir) throws Exception {
        String fuente = "class C1 { int x  void m(){ y = ; } }\n";

        assertEquals(List.of("void", ";"), lexemasDeLosErrores(dir, fuente));
    }

    // El error cae justo en un token que también arranca un miembro válido, y el
    // miembro vuelve a fallar ahí mismo. Sin la garantía de progreso el
    // analizador daría vueltas para siempre sobre el mismo token.
    @Test
    void unErrorQueCaeEnElComienzoDeOtroElementoNoCiclaParaSiempre(@TempDir Path dir) {
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            assertFalse(lexemasDeLosErrores(dir, "class C1 { int int; }\n").isEmpty());
            assertFalse(lexemasDeLosErrores(dir, "class C1 { void m(){ if if ; } }\n").isEmpty());
            assertFalse(lexemasDeLosErrores(dir, "class class class\n").isEmpty());
        });
    }

    @Test
    void unFuenteValidoNoDisparaLaRecuperacion(@TempDir Path dir) throws Exception {
        String fuente = "class C1 { int x; void m(){ var a = 1; } }\nclass C2 { }\n";

        assertTrue(erroresDe(dir, fuente).isEmpty());
        assertEquals(List.of("Compilacion Exitosa", "", "[SinErrores]"),
                salidaDe(dir, fuente));
    }

    // Con errores nunca se imprime el código de éxito, aunque el análisis haya
    // seguido después del primero.
    @Test
    void conErroresNuncaSeImprimeSinErrores(@TempDir Path dir) throws Exception {
        List<String> salida = salidaDe(dir, CUATRO_ERRORES);

        assertFalse(salida.contains("[SinErrores]"));
        assertEquals(4, salida.stream().filter(l -> l.startsWith("[Error:")).count());
    }

    // Un error LÉXICO corta el análisis, pero no puede llevarse puestos los
    // errores sintácticos que la recuperación ya había encontrado: son anteriores
    // en el fuente y hay que reportarlos igual.
    @Test
    void unErrorLexicoNoSeLlevaPuestosLosSintacticosYaEncontrados(@TempDir Path dir)
            throws Exception {
        // `#` no es un carácter de MiniJava: error LÉXICO, y llega después del
        // error sintáctico de la primera línea.
        List<String> salida = salidaDe(dir, "class C1 { int x  void m(){ } }\nclass #\n");

        assertTrue(salida.contains("[Error:void|1]"), salida.toString());
        assertTrue(salida.stream().anyMatch(l -> l.startsWith("[Error:#")), salida.toString());
    }

    // El formato de cada reporte es el mismo de Pautas slide 25, repetido: el
    // mensaje, un renglón en blanco y el código.
    @Test
    void cadaErrorSeReportaConElFormatoCompletoDeLasPautas(@TempDir Path dir) throws Exception {
        List<String> salida = salidaDe(dir, "class C1 { int x  int y  }\n");

        assertEquals(6, salida.size());
        assertTrue(salida.get(0).startsWith("Error Sintactico en linea 1: "));
        assertEquals("", salida.get(1));
        assertEquals("[Error:int|1]", salida.get(2));
        assertTrue(salida.get(3).startsWith("Error Sintactico en linea 1: "));
        assertEquals("", salida.get(4));
        assertEquals("[Error:}|1]", salida.get(5));
    }
}
