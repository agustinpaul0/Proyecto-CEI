package minijava.lexico;

import minijava.lexico.tokens.TipoDeToken;
import minijava.lexico.tokens.Token;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("lexico")
class AnalizadorLexicoIdentificadoresTest extends BaseDeAnalisisLexico {

    // Los tres identificadores (idMetVar, idClase, idGen) y las palabras clave.

    @Test
    void reconoceIdMvSimple(@TempDir Path dir) throws Exception {
        Token token = crearAnalizador(dir, "v1").proximoToken();

        assertEquals(TipoDeToken.ID_MV, token.tipo());
        assertEquals("v1", token.lexema());
    }

    @Test
    void idMvAceptaMayusculasDigitosYGuionBajo(@TempDir Path dir) throws Exception {
        Token token = crearAnalizador(dir, "abC_1;").proximoToken();

        assertEquals(TipoDeToken.ID_MV, token.tipo());
        assertEquals("abC_1", token.lexema());
    }

    @Test
    void idMvDeUnaSolaLetra(@TempDir Path dir) throws Exception {
        Token token = crearAnalizador(dir, "x").proximoToken();

        assertEquals(TipoDeToken.ID_MV, token.tipo());
        assertEquals("x", token.lexema());
    }

    @Test
    void reconoceIdGenUnaSolaMayuscula(@TempDir Path dir) throws Exception {
        Token token = crearAnalizador(dir, "A").proximoToken();

        assertEquals(TipoDeToken.ID_GEN, token.tipo());
        assertEquals("A", token.lexema());
    }

    @Test
    void reconoceIdClaseDosCaracteres(@TempDir Path dir) throws Exception {
        Token token = crearAnalizador(dir, "Ab").proximoToken();

        assertEquals(TipoDeToken.ID_CLASE, token.tipo());
        assertEquals("Ab", token.lexema());
    }

    @Test
    void idClaseAceptaMayusculasMinusculasDigitosYGuionBajo(@TempDir Path dir) throws Exception {
        Token token = crearAnalizador(dir, "AlphaBeta_1;").proximoToken();

        assertEquals(TipoDeToken.ID_CLASE, token.tipo());
        assertEquals("AlphaBeta_1", token.lexema());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "class|PR_CLASS", "extends|PR_EXTENDS", "interface|PR_INTERFACE",
            "implements|PR_IMPLEMENTS", "static|PR_STATIC", "boolean|PR_BOOLEAN",
            "char|PR_CHAR", "int|PR_INT", "void|PR_VOID", "public|PR_PUBLIC",
            "if|PR_IF", "else|PR_ELSE", "while|PR_WHILE", "return|PR_RETURN",
            "var|PR_VAR", "this|PR_THIS", "new|PR_NEW", "null|PR_NULL",
            "true|PR_TRUE", "false|PR_FALSE", "float|PR_FLOAT",
    })
    // float es reservada por confirmación directa del profesor; "double" NO lo es
    // (los sufijos f/F/d/D del literal ya cubren ambos).
    // documentacion/lexico/especificacion-lexica-de-minijava.md.
    void reconoceLasVeintiunaPalabrasClave(String fuente, String tipoEsperado, @TempDir Path dir)
            throws Exception {
        Token token = crearAnalizador(dir, fuente).proximoToken();

        assertEquals(TipoDeToken.valueOf(tipoEsperado), token.tipo());
        assertEquals(fuente, token.lexema());
    }

    @Test
    void dobleNoEsPalabraClaveEsUnIdentificadorComun(@TempDir Path dir) throws Exception {
        Token token = crearAnalizador(dir, "double").proximoToken();

        assertEquals(TipoDeToken.ID_MV, token.tipo());
        assertEquals("double", token.lexema());
    }

    @Test
    void unaPalabraQueEmpiezaComoPalabraClaveNoLaConfunde(@TempDir Path dir) throws Exception {
        Token token = crearAnalizador(dir, "ifx").proximoToken();

        assertEquals(TipoDeToken.ID_MV, token.tipo());
        assertEquals("ifx", token.lexema());
    }

}
