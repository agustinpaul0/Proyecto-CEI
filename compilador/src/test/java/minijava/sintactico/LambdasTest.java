package minijava.sintactico;

import minijava.comun.config.Logro;
import minijava.logros.RequiereLogro;
import minijava.logros.RequiereLogroApagado;
import minijava.sintactico.errores.ExcepcionSintactica;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// El logro MiniLambdas E2, que es el que más reglas trae: la desambiguación del
// paréntesis, y las tres restricciones que la gramática no puede expresar.
@Tag("sintactico")
@RequiereLogro(Logro.LAMBDA)
class LambdasTest extends BaseDeAnalisis {

    private void aceptarLambda(Path dir, String lambda) {
        aceptar(dir, envolverEnMetodo("var f = " + lambda + ";"));
    }

    private ExcepcionSintactica rechazarLambda(Path dir, String lambda) {
        return rechazar(dir, envolverEnMetodo("var f = " + lambda + ";"));
    }

    // ------------------------------------------------------------ lo válido ---

    @ParameterizedTest
    @ValueSource(strings = {
            "x -> x",
            "x -> x + 1",
            "x -> x.campo",
            "x -> x.metodo()",
            "x -> x[0]",
            "() -> 1",
            "() -> null",
            "(a) -> a",
            "(a, b) -> a * b",
            "(a, b, c) -> a + b + c",
            "x -> Clase.metodo(x)",
            "x -> new Persona(x)",
            "x -> y -> y",
    })
    void seAceptaCadaFormaDeLambda(String lambda, @TempDir Path dir) {
        aceptarLambda(dir, lambda);
    }

    // Se permite cualquier cantidad de parámetros, incluido ninguno.
    @Test
    void seAceptaUnaLambdaDeMuchosParametros(@TempDir Path dir) {
        String parametros = java.util.stream.IntStream.range(0, 50)
                .mapToObj(i -> "p" + i)
                .collect(java.util.stream.Collectors.joining(", "));

        aceptarLambda(dir, "(" + parametros + ") -> p0");
    }

    // El cuerpo es voraz: `x -> a + b` es `x -> (a + b)`, no `(x -> a) + b`.
    @Test
    void elCuerpoDeLaLambdaConsumeTodaLaExpresion(@TempDir Path dir) {
        aceptarLambda(dir, "x -> x + x * x");
        aceptarLambda(dir, "x -> x.a().b().c()");
    }

    // ------------------------------------------- el paréntesis ambiguo ---

    // `(` abre una parentizada y también los parámetros de una lambda. Con un
    // token de lookahead alcanza en cada punto de decisión.
    @Test
    void unParentesisSigueAbriendoUnaExpresionParentizada(@TempDir Path dir) {
        aceptar(dir, envolverEnMetodo("var x = (a + 1);"));
        aceptar(dir, envolverEnMetodo("var x = (a);"));
        aceptar(dir, envolverEnMetodo("var x = (a) + 1;"));
    }

    @Test
    void unaListaDeParametrosYUnaParentizadaSeDistinguenPorElTokenQueSigue(@TempDir Path dir) {
        aceptar(dir, envolverEnMetodo("var x = (a);"));
        aceptarLambda(dir, "(a) -> a");
    }

    // --------------------------------------------- el cuerpo es UNA expresión ---

    @Test
    void elCuerpoDeLaLambdaNoAdmiteLlaves(@TempDir Path dir) {
        assertEquals("{", rechazarLambda(dir, "x -> { return x; }").getLexemaOfensivo());
    }

    @Test
    void elCuerpoDeLaLambdaNoAdmiteUnaSentencia(@TempDir Path dir) {
        assertEquals("return", rechazarLambda(dir, "x -> return x").getLexemaOfensivo());
    }

    // -------------------------------------------------------- `this` prohibido ---

    @Test
    void elThisExplicitoDentroDeUnaLambdaSeRechaza(@TempDir Path dir) {
        ExcepcionSintactica excepcion = rechazarLambda(dir, "x -> this.campo");

        assertEquals("this", excepcion.getLexemaOfensivo());
        assertTrue(excepcion.getMessage().contains("this"), excepcion.getMessage());
    }

    // Llamar a un método propio sin escribir `this` es un uso de `this` igual: en
    // MiniJava no hay funciones sueltas y un parámetro no se puede llamar, así que
    // toda llamada sin punto dentro de una lambda es un `this` implícito.
    @Test
    void elThisImplicitoDentroDeUnaLambdaSeRechaza(@TempDir Path dir) {
        ExcepcionSintactica excepcion = rechazarLambda(dir, "x -> metodo(x)");

        assertEquals("metodo", excepcion.getLexemaOfensivo());
        assertTrue(excepcion.getMessage().contains("implicito"), excepcion.getMessage());
    }

    // Una llamada CON punto sí se permite: es un método de otro objeto.
    @Test
    void unaLlamadaConPuntoDentroDeUnaLambdaSeAcepta(@TempDir Path dir) {
        aceptarLambda(dir, "x -> x.metodo()");
        aceptarLambda(dir, "x -> Clase.metodo(x)");
    }

    @Test
    void fueraDeUnaLambdaElThisSigueSiendoValido(@TempDir Path dir) {
        aceptar(dir, envolverEnMetodo("this.campo = 1;"));
        aceptar(dir, envolverEnMetodo("metodo();"));
    }

    // Después de la lambda el código vuelve a poder usar `this`: el indicador se
    // restaura al salir.
    @Test
    void elIndicadorDeLambdaSeRestauraAlSalir(@TempDir Path dir) {
        aceptar(dir, envolverEnMetodo("var f = x -> x; this.campo = 1;"));
        aceptar(dir, envolverEnMetodo("var f = m1(x -> x, this);"));
    }

    // ------------------------------------------------ captura de variables ---

    @Test
    void capturarUnaVariableQueNoEsParametroSeRechaza(@TempDir Path dir) {
        ExcepcionSintactica excepcion = rechazarLambda(dir, "x -> x + n");

        assertEquals("n", excepcion.getLexemaOfensivo());
        assertTrue(excepcion.getMessage().contains("capturar"), excepcion.getMessage());
    }

    @Test
    void usarElPropioParametroSeAcepta(@TempDir Path dir) {
        aceptarLambda(dir, "x -> x");
        aceptarLambda(dir, "(a, b) -> a * b");
        aceptarLambda(dir, "(a, b) -> b");
    }

    // Un nombre después de un `.` es un campo de otro objeto, no una variable del
    // alcance: no se chequea.
    @Test
    void unCampoDespuesDeUnPuntoNoCuentaComoCaptura(@TempDir Path dir) {
        aceptarLambda(dir, "x -> x.n");
        aceptarLambda(dir, "x -> x.a.b.c");
    }

    // Usar el parámetro de la lambda de AFUERA también está prohibido, así que
    // alcanza con el conjunto de la más interna.
    @Test
    void unaLambdaAnidadaNoPuedeUsarElParametroDeLaDeAfuera(@TempDir Path dir) {
        ExcepcionSintactica excepcion = rechazarLambda(dir, "x -> y -> x + y");

        assertEquals("x", excepcion.getLexemaOfensivo());
    }

    @Test
    void unaLambdaAnidadaSiPuedeUsarElPropio(@TempDir Path dir) {
        aceptarLambda(dir, "x -> y -> y + 1");
    }

    // ---------------------------------------- el parámetro es un identificador ---

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "(a + 1) -> a   | a",
            "(m1()) -> a    | m1",
            "(1) -> a       | 1",
            "(a.b) -> a     | a",
            "(a + 1, b) -> a| a",
    })
    void unParametroQueNoEsUnIdentificadorSeRechaza(String lambda, String lexema,
                                                    @TempDir Path dir) {
        ExcepcionSintactica excepcion = rechazarLambda(dir, lambda);

        assertEquals(lexema, excepcion.getLexemaOfensivo());
        assertTrue(excepcion.getMessage().contains("identificador"), excepcion.getMessage());
    }

    @Test
    void unParametroRepetidoSeRechaza(@TempDir Path dir) {
        ExcepcionSintactica excepcion = rechazarLambda(dir, "(a, a) -> a");

        assertEquals("a", excepcion.getLexemaOfensivo());
        assertTrue(excepcion.getMessage().contains("repetido"), excepcion.getMessage());
    }

    // ------------------------------------------------------ el logro apagado ---

    @Test
    @RequiereLogroApagado(Logro.LAMBDA)
    void sinElLogroLaFlechaNoFormaUnaLambda(@TempDir Path dir) {
        assertEquals(">", rechazarLambda(dir, "x -> x").getLexemaOfensivo());
    }
}
