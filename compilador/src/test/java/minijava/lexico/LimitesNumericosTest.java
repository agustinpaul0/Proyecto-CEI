package minijava.lexico;

import minijava.lexico.errores.ExcepcionLexica;
import minijava.lexico.errores.TipoDeErrorLexico;
import minijava.comun.io.ManejadorDeArchivo;
import minijava.comun.io.ManejadorDeArchivoEficiente;
import minijava.lexico.tokens.TipoDeToken;
import minijava.lexico.tokens.Token;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Tres reglas distintas que conviene no mezclar: intLiteral se acota a nueve
// DÍGITOS (no a un valor, por eso 0000000001 es error); floatLiteral no tiene
// límite de dígitos ni de valor, porque el rango en Java es semántico.
@Tag("lexico")
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

    // Divergencia deliberada: en Java `int d = 0000000001;` compila, porque allá el
    // límite es el valor. Acá es la cantidad de dígitos, como dice la ER.
    @Test
    void elLimiteDelEnteroEsDeDigitosNoDeValor(@TempDir Path dir) throws Exception {
        ExcepcionLexica excepcion = assertThrows(ExcepcionLexica.class,
                () -> crear(dir, "0000000001 ").proximoToken());

        assertEquals(TipoDeErrorLexico.ENTERO_DEMASIADO_LARGO, excepcion.getTipo());
        assertEquals("0000000001", excepcion.getLexemaOfensivo());
    }

    // El chequeo sólo corre si la corrida no sigue hacia un float: por eso una parte
    // entera de diez dígitos es válida dentro de un floatLiteral.
    @ParameterizedTest
    @ValueSource(strings = {"1234567890.5", "9999999999.0", "9999999999e1", "9999999999f",
                            "1234567890e5"})
    void unaParteEnteraDeDiezDigitosEsValidaDentroDeUnFloat(String fuente, @TempDir Path dir)
            throws Exception {
        Token token = unicoToken(dir, fuente + " ");

        assertEquals(TipoDeToken.FLOAT_LITERAL, token.tipo());
        assertEquals(fuente, token.lexema());
    }

    // Verificado con javac: un literal de 60+ dígitos compila sin una queja mientras
    // su valor entre en el tipo.
    @Test
    void unFloatNoTieneLimiteDeCantidadDeDigitos(@TempDir Path dir) throws Exception {
        String muchosDecimales = "1." + "0".repeat(60) + "1";
        String muchosEnteros = "1".repeat(40) + ".5";
        String muchosDelExponente = "1e" + "9".repeat(20);

        assertEquals(muchosDecimales, unicoToken(dir, muchosDecimales + " ").lexema());
        assertEquals(muchosEnteros, unicoToken(dir, muchosEnteros + " ").lexema());
        assertEquals(muchosDelExponente, unicoToken(dir, muchosDelExponente + " ").lexema());
    }

    // javac SÍ los rechaza (3.5e38f -> floating-point number too large), pero al
    // convertir el literal, no al tokenizarlo. Pendiente de consultar (todos.md).
    @ParameterizedTest
    @ValueSource(strings = {"3.5e38f", "1e-46f", "1.8e308", "1e999"})
    void unFloatFueraDelRangoDeJavaSigueSiendoUnTokenValido(String fuente, @TempDir Path dir)
            throws Exception {
        Token token = unicoToken(dir, fuente + " ");

        assertEquals(TipoDeToken.FLOAT_LITERAL, token.tipo());
        assertEquals(fuente, token.lexema());
    }
}
