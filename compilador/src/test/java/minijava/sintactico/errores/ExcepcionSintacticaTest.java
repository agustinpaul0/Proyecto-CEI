package minijava.sintactico.errores;

import minijava.lexico.tokens.TipoDeToken;
import minijava.lexico.tokens.Token;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("sintactico")
class ExcepcionSintacticaTest {

    private Token token(String lexema, int linea) {
        return new Token(TipoDeToken.LLAVE_A, lexema, linea);
    }

    // Pautas Etapa 2, slide 25: el formato del mensaje es fijo.
    @Test
    void elMensajeSigueElFormatoDeLasPautas() {
        var excepcion = new ExcepcionSintactica(token("{", 1), "idClase");

        assertEquals("Error Sintactico en linea 1: se esperaba idClase se encontro \"{\"",
                excepcion.getMessage());
    }

    @Test
    void elLexemaYLaLineaSalenDelTokenEncontrado() {
        var encontrado = token("{", 7);
        var excepcion = new ExcepcionSintactica(encontrado, "idClase");

        assertEquals("{", excepcion.getLexemaOfensivo());
        assertEquals(7, excepcion.getLinea());
        assertEquals(encontrado, excepcion.getEncontrado());
        assertEquals("idClase", excepcion.getEsperado());
    }

    // "a", "a o b", "a, b o c": la prosa del ejemplo de Clase 5, slide 30.
    @Test
    void unaSolaAlternativaSeNombraSola() {
        assertEquals("idClase",
                new ExcepcionSintactica(token("{", 1), List.of("idClase")).getEsperado());
    }

    @Test
    void dosAlternativasSeSeparanConUnO() {
        assertEquals("idGen o idClase",
                new ExcepcionSintactica(token("{", 1), List.of("idGen", "idClase")).getEsperado());
    }

    @Test
    void tresOMasAlternativasVanConComasYUnOAlFinal() {
        assertEquals("(, id o num",
                new ExcepcionSintactica(token("{", 1), List.of("(", "id", "num")).getEsperado());
    }

    // Un error sin nada esperado sería un mensaje vacío: es un defecto del
    // compilador, no una entrada inválida.
    @Test
    void unErrorSinAlternativasEsUnDefectoDelCompilador() {
        assertThrows(IllegalArgumentException.class,
                () -> new ExcepcionSintactica(token("{", 1), List.of()));
    }

    @Test
    void elLexemaDelFinDeArchivoLlegaTalCualAlMensaje() {
        var excepcion = new ExcepcionSintactica(new Token(TipoDeToken.EOF, "$", 12), "}");

        assertEquals("$", excepcion.getLexemaOfensivo());
        assertEquals(12, excepcion.getLinea());
        assertEquals("Error Sintactico en linea 12: se esperaba } se encontro \"$\"",
                excepcion.getMessage());
    }
}
