package minijava.tokens;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TipoDeTokenTest {

    // Tabla copiada independientemente de AGENTS.md §5 y §2 (ejemplos de la cátedra),
    // no derivada del enum: sirve para detectar errores de transcripción.
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

    @Test
    void tieneExactamenteCincuentaYCincoTipos() {
        assertEquals(55, TipoDeToken.values().length);
    }
}
