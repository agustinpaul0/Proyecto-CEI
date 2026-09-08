package minijava.sintactico;

import minijava.comun.config.ConfiguracionDelCompilador;
import minijava.comun.config.Logro;
import minijava.comun.io.ManejadorDeArchivo;
import minijava.comun.io.ManejadorDeArchivoEficiente;
import minijava.lexico.AnalizadorLexico;
import minijava.logros.ConfiguracionDeLaCorrida;
import minijava.logros.RequiereLogro;
import minijava.logros.RequiereLogroApagado;
import minijava.sintactico.errores.ExcepcionSintactica;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("sintactico")
class AnalizadorSintacticoTest extends BaseDeAnalisis {

    // --------------------------------------------------------- lo que acepta ---

    // Un programa vacío es <ListaClases> -> ε seguido de eof: el caso degenerado
    // que exige la gramática y que suele quedar sin probar.
    @ParameterizedTest
    @ValueSource(strings = {"", "\n", "   \n\t\n", "// solo un comentario\n", "/* nada */"})
    void unFuenteSinNingunaClaseEsValido(String fuente, @TempDir Path dir) {
        aceptar(dir, fuente);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "class C1 {}",
            "class C1 extends Base {}",
            "class C1 implements Contrato {}",
            "class C1<T> {}",
            "class C1<T> extends Base {}",
            "class C1<T> implements Contrato<T> {}",
            "class C1 extends Base<Otra> {}",
    })
    void seAceptaUnaClaseSinMiembros(String fuente, @TempDir Path dir) {
        aceptar(dir, fuente);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "interface I1 {}",
            "interface I1<T> {}",
            "interface I1 extends Otra {}",
            "interface I1<T> extends Otra<T> {}",
    })
    void seAceptaUnaInterfazSinMetodos(String fuente, @TempDir Path dir) {
        aceptar(dir, fuente);
    }

    @Test
    void seAceptanVariasClasesEInterfacesMezcladas(@TempDir Path dir) {
        aceptar(dir, "class C1 {} interface I1 {} class C2 {} interface I2 {} class C3 {}");
    }

    // La recursión de <ListaClases> es a derecha y consume la pila de llamadas.
    @Test
    void seAceptaUnaListaLarguisimaDeClases(@TempDir Path dir) {
        aceptar(dir, "class C1 {}\n".repeat(500));
    }

    // --------------------------------------------------------- lo que rechaza ---

    // El ejemplo textual de Pautas Etapa 2, slide 25.
    @Test
    void elEjemploDeLasPautasFallaEnElTokenQueLasPautasDeclaran(@TempDir Path dir) {
        ExcepcionSintactica excepcion = rechazar(dir, "class {\n\n}\n");

        assertEquals("{", excepcion.getLexemaOfensivo());
        assertEquals(1, excepcion.getLinea());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "class C1 {                 | $  | 1",
            "class C1 }                 | }  | 1",
            "class                      | $  | 1",
            "C1 {}                      | C1 | 1",
            "class C1 extends {}        | {  | 1",
            "class C1 extends Base      | $  | 1",
            "class C1 implements {}     | {  | 1",
            "interface I1 implements Otra {} | implements | 1",
            "class C1<> {}              | >  | 1",
            "class C1<T {}              | {  | 1",
            "class C1<Clase> {}         | Clase | 1",
            "class c1 {}                | c1 | 1",
            "class C1 {} }              | }  | 1",
    })
    void unFuenteInvalidoFallaEnElTokenExacto(String fuente, String lexema, int linea,
                                              @TempDir Path dir) {
        ExcepcionSintactica excepcion = rechazar(dir, fuente);

        assertEquals(lexema, excepcion.getLexemaOfensivo());
        assertEquals(linea, excepcion.getLinea());
    }

    // Pautas Etapa 2, slide 26: el lexema del token eof es '$'.
    @Test
    void elErrorEnElFinDeArchivoReportaElLexemaDolar(@TempDir Path dir) {
        ExcepcionSintactica excepcion = rechazar(dir, "class C1 {\n");

        assertEquals("$", excepcion.getLexemaOfensivo());
        assertEquals(2, excepcion.getLinea());
    }

    @Test
    void elErrorReportaLaLineaEnLaQueEstaElTokenYNoLaPrimera(@TempDir Path dir) {
        ExcepcionSintactica excepcion = rechazar(dir, "class C1 {}\nclass C2 {}\nclass 9 {}\n");

        assertEquals("9", excepcion.getLexemaOfensivo());
        assertEquals(3, excepcion.getLinea());
    }

    @Test
    void elMensajeSigueElFormatoDeLasPautas(@TempDir Path dir) {
        ExcepcionSintactica excepcion = rechazar(dir, "class {}");

        assertEquals("Error Sintactico en linea 1: se esperaba idClase se encontro \"{\"",
                excepcion.getMessage());
    }

    // ------------------------------------------------- el logro de genericidad ---

    @Test
    @RequiereLogro(Logro.GENERICO)
    void conGenericidadAvanzadaSeAceptaUnGenericoAnidado(@TempDir Path dir) {
        aceptar(dir, "class C1 extends Base<Lista<Otra>> {}");
    }

    @Test
    @RequiereLogro(Logro.GENERICO)
    void conGenericidadAvanzadaElAnidamientoNoTieneLimite(@TempDir Path dir) {
        aceptar(dir, "class C1 extends " + "Lista<".repeat(50) + "Hoja" + ">".repeat(50) + " {}");
    }

    @Test
    @RequiereLogroApagado(Logro.GENERICO)
    void sinGenericidadAvanzadaUnGenericoAnidadoSeRechaza(@TempDir Path dir) {
        ExcepcionSintactica excepcion = rechazar(dir, "class C1 extends Base<Lista<Otra>> {}");

        assertEquals("<", excepcion.getLexemaOfensivo());
    }

    // Un solo nivel de genérico existe con el logro y sin él: el logro agrega el
    // anidamiento, no la genericidad.
    @Test
    void unGenericoDeUnSoloNivelSeAceptaConElLogroYSinEl(@TempDir Path dir) {
        aceptar(dir, "class C1 extends Base<Otra> {}");
    }

    // La clase sigue limitada a UN parámetro de tipo, también con el logro.
    @Test
    void laClaseSigueLimitadaAUnUnicoParametroDeTipo(@TempDir Path dir) {
        ExcepcionSintactica excepcion = rechazar(dir, "class C1<T,U> {}");

        assertEquals(",", excepcion.getLexemaOfensivo());
    }

    // ------------------------------------------------------ error dentro de <> ---

    @Test
    void unArgumentoDeTipoInvalidoNombraLasAlternativasPosibles(@TempDir Path dir) {
        ExcepcionSintactica excepcion = rechazar(dir, "class C1 extends Base<9> {}");

        assertEquals("9", excepcion.getLexemaOfensivo());
        assertTrue(excepcion.getEsperado().contains("idGen"), excepcion.getEsperado());
        assertTrue(excepcion.getEsperado().contains("idClase"), excepcion.getEsperado());
    }

    // ------------------------------------------------------------- el léxico ---

    @Test
    void unErrorLexicoLlegaComoTalYNoComoErrorSintactico(@TempDir Path dir) {
        Path archivo = dir.resolve("fuente.mjava");

        assertThrows(minijava.lexico.errores.ExcepcionLexica.class, () -> {
            Files.writeString(archivo, "class C1 # {}");
            ManejadorDeArchivo manejador = new ManejadorDeArchivoEficiente();
            manejador.open(archivo.toString());
            var configuracion = ConfiguracionDeLaCorrida.actual();
            new AnalizadorSintactico(new AnalizadorLexico(manejador, configuracion), configuracion)
                    .analizar();
        });
    }

    @Test
    void unFuenteInexistenteFallaAlAbrirloYNoAlAnalizarlo(@TempDir Path dir) {
        ManejadorDeArchivo manejador = new ManejadorDeArchivoEficiente();

        assertThrows(IOException.class, () -> manejador.open(dir.resolve("no-existe").toString()));
    }
}
