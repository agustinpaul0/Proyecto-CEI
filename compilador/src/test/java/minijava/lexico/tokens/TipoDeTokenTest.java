package minijava.lexico.tokens;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("lexico")
class TipoDeTokenTest {

    // Copiada de la especificación y no derivada del enum: detecta errores de
    // transcripción.
    @ParameterizedTest
    @CsvSource({
            "PR_CLASS,pr_class", "PR_IF,pr_if", "PR_ELSE,pr_else", "PR_FALSE,pr_false",
            "ID_CLASE,idClase", "ID_GEN,idGen", "ID_MV,idMV",
            "INT_LITERAL,intLiteral", "CHAR_LITERAL,charLiteral",
            "LIT_STRING,litString", "FLOAT_LITERAL,floatLiteral",
            "PAREN_A,parenA", "LLAVE_C,llaveC", "PUNTO_Y_COMA,puntoYComa",
            "OP_MAYOR,op_mayor", "OP_MAYOR_IGUAL,op_mayorIgual",
            "OP_SUMA,op+", "OP_INCREMENTO,op_incremento",
            "OP_AND,op_and", "OP_OR,op_or",
            "EOF,EOF",
    })
    void elNombreImpresoCoincideConLaConvencionDeLaCatedra(String constante, String nombreEsperado) {
        TipoDeToken tipo = TipoDeToken.valueOf(constante);
        assertEquals(nombreEsperado, tipo.getNombre());
    }

    // 56 de la Etapa 1 más los 3 que traen los logros de la Etapa 2.
    @Test
    void tieneExactamenteCincuentaYNueveTipos() {
        assertEquals(59, TipoDeToken.values().length);
    }
}
