package minijava.lexico;

import minijava.errores.ExcepcionLexica;
import minijava.errores.TipoDeErrorLexico;
import minijava.io.ManejadorDeArchivo;
import minijava.io.ManejadorDeArchivoEficiente;
import minijava.tokens.TipoDeToken;
import minijava.tokens.Token;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Los límites de los literales numéricos, que son tres reglas distintas y
// conviene no mezclarlas:
//
//  1. `intLiteral` está acotado a NUEVE DÍGITOS. Es un límite de CANTIDAD DE
//     DÍGITOS, no de valor: por eso 0000000001 (que vale 1) es un error.
//     En Java el límite es de valor, así que ese mismo literal es válido.
//  2. `floatLiteral` NO tiene ningún límite de dígitos. La ER de la cátedra
//     usa Digito+ sin cota en las tres partes, y Pautas Etapa 1 habla de "la
//     misma estructura que los floats de Java" — estructura, no rango.
//  3. `floatLiteral` tampoco tiene límite de VALOR. Java sí rechaza un
//     literal no nulo que se redondee a infinito o a cero, pero lo hace al
//     convertirlo, no al tokenizarlo: es semántico, no léxico.
class LimitesNumericosTest {

    private AnalizadorLexico crear(Path dir, String fuente) throws IOException {
        Path archivo = dir.resolve("fuente.mjava");
        Files.writeString(archivo, fuente);
        ManejadorDeArchivo manejador = new ManejadorDeArchivoEficiente();
        manejador.open(archivo.toString());
        return new AnalizadorLexico(manejador);
    }

    private Token unicoToken(Path dir, String fuente) throws Exception {
        return crear(dir, fuente).proximoToken();
    }

    @Test
    void nueveDigitosEsUnEnteroValidoYDiezEsError(@TempDir Path dir) throws Exception {
        assertEquals(TipoDeToken.INT_LITERAL, unicoToken(dir, "999999999 ").tipo());

        ExcepcionLexica excepcion = assertThrows(ExcepcionLexica.class,
                () -> crear(dir, "9999999999 ").proximoToken());

        assertEquals(TipoDeErrorLexico.ENTERO_DEMASIADO_LARGO, excepcion.getTipo());
    }

    // Divergencia deliberada con Java: allá `int d = 0000000001;` compila sin
    // problema porque el límite es el VALOR (2147483648). Acá el límite es la
    // cantidad de dígitos, tal como lo escribe la ER de la cátedra.
    @Test
    void elLimiteDelEnteroEsDeDigitosNoDeValor(@TempDir Path dir) throws Exception {
        ExcepcionLexica excepcion = assertThrows(ExcepcionLexica.class,
                () -> crear(dir, "0000000001 ").proximoToken());

        assertEquals(TipoDeErrorLexico.ENTERO_DEMASIADO_LARGO, excepcion.getTipo());
        assertEquals("0000000001", excepcion.getLexemaOfensivo());
    }

    // El chequeo de nueve dígitos sólo corre si la corrida NO continúa hacia
    // un float: por eso una parte entera de diez dígitos es válida mientras
    // sea parte de un floatLiteral.
    @ParameterizedTest
    @ValueSource(strings = {"1234567890.5", "9999999999.0", "9999999999e1", "9999999999f",
                            "1234567890e5"})
    void unaParteEnteraDeDiezDigitosEsValidaDentroDeUnFloat(String fuente, @TempDir Path dir)
            throws Exception {
        Token token = unicoToken(dir, fuente + " ");

        assertEquals(TipoDeToken.FLOAT_LITERAL, token.tipo());
        assertEquals(fuente, token.lexema());
    }

    // Verificado con javac: un literal con 60+ dígitos compila sin una queja
    // mientras su valor entre en el tipo.
    //
    //     float a = 1.0000000000000000000000000000000000000000000000000000000001f;
    //     float b = 123456789012345678901234567890.0f;
    //     -> compilan las dos
    @Test
    void unFloatNoTieneLimiteDeCantidadDeDigitos(@TempDir Path dir) throws Exception {
        String muchosDecimales = "1." + "0".repeat(60) + "1";
        String muchosEnteros = "1".repeat(40) + ".5";
        String muchosDelExponente = "1e" + "9".repeat(20);

        assertEquals(muchosDecimales, unicoToken(dir, muchosDecimales + " ").lexema());
        assertEquals(muchosEnteros, unicoToken(dir, muchosEnteros + " ").lexema());
        assertEquals(muchosDelExponente, unicoToken(dir, muchosDelExponente + " ").lexema());
    }

    // Verificado con javac, que SÍ los rechaza:
    //
    //     float d = 3.5e38f;   -> error: floating-point number too large
    //     float g = 1e-46f;    -> error: floating-point number too small
    //     double i = 1.8e308;  -> error: floating-point number too large
    //
    // ...pero como chequeo de VALOR al convertir el literal, no al
    // tokenizarlo. MiniJava no lo implementa: la consigna pidió la misma
    // estructura, y el rango es semántico. Queda pendiente de consultar al
    // profesor (todos/todos.md).
    @ParameterizedTest
    @ValueSource(strings = {"3.5e38f", "1e-46f", "1.8e308", "1e999"})
    void unFloatFueraDelRangoDeJavaSigueSiendoUnTokenValido(String fuente, @TempDir Path dir)
            throws Exception {
        Token token = unicoToken(dir, fuente + " ");

        assertEquals(TipoDeToken.FLOAT_LITERAL, token.tipo());
        assertEquals(fuente, token.lexema());
    }
}
