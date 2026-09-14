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

// El logro MiniLambdas E2, que es el que más reglas trae: dónde vive la lambda en
// la jerarquía de expresiones, la desambiguación de sus dos cabezas, y las cuatro
// restricciones que la gramática no puede expresar.
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

    // Una llamada sin punto NO se rechaza. Sería `this.metodo(x)` sólo si `metodo`
    // es de instancia; si es `static` de la misma clase no hay ningún `this` y
    // Java lo acepta. Distinguirlos exige resolver el nombre contra su
    // declaración, o sea la tabla de símbolos de la Etapa 3.
    @Test
    void unaLlamadaSinPuntoDentroDeUnaLambdaSeAcepta(@TempDir Path dir) {
        aceptarLambda(dir, "x -> metodo(x)");
        aceptarLambda(dir, "() -> arrancar()");
    }

    // Pero el argumento sí se sigue chequeando: `n` no es parámetro de la lambda.
    @Test
    void elArgumentoDeUnaLlamadaSinPuntoSigueSiendoUnaCaptura(@TempDir Path dir) {
        ExcepcionSintactica excepcion = rechazarLambda(dir, "x -> metodo(n)");

        assertEquals("n", excepcion.getLexemaOfensivo());
        assertTrue(excepcion.getMessage().contains("capturar"), excepcion.getMessage());
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

    // ------------------------------------------------- la precedencia del `->` ---
    // La lambda no es un operando: vive en el nivel más alto de la jerarquía de
    // expresiones, igual que en el JLS («Expression: LambdaExpression |
    // AssignmentExpression»). Por eso `->` no compite en precedencia con ningún
    // operador. Cada caso de acá se contrastó con javac.

    @ParameterizedTest
    @ValueSource(strings = {
            "1 + x -> x",
            "a * x -> x",
            "a == x -> x",
            "a || x -> x",
            "- x -> x",
            "! x -> x",
            "1 + (x -> x)",
            "a * () -> 1",
            "1 + (a, b) -> a",
    })
    void unaLambdaNoPuedeSerOperandoDeUnOperador(String expresion, @TempDir Path dir) {
        rechazarLambda(dir, expresion);
    }

    // `f((x -> x))` es válido y `(x -> x).m()` no: parentizarla no la convierte en
    // un operando. javac dice «lambda expression not expected here» en los dos.
    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "(x -> x).metodo() | .",
            "(x -> x).campo    | .",
            "(x -> x)[0]       | [",
            "(x -> x) + 1      | +",
            "(x -> x) ? a : b  | ?",
            "(() -> 1) * 2     | *",
            "(x -> x)++        | ++",
    })
    void unaLambdaParentizadaNoPuedeLlevarColaDeExpresion(String expresion, String lexema,
                                                          @TempDir Path dir) {
        ExcepcionSintactica excepcion = rechazarLambda(dir, expresion);

        assertEquals(lexema, excepcion.getLexemaOfensivo());
        assertTrue(excepcion.getMessage().contains("no puede ser operando"), excepcion.getMessage());
    }

    // La lambda parentizada SÍ es válida cuando ella misma es toda la expresión.
    @ParameterizedTest
    @ValueSource(strings = {
            "(x -> x)",
            "((x -> x))",
            "((() -> 1))",
    })
    void unaLambdaParentizadaSinColaSeAcepta(String expresion, @TempDir Path dir) {
        aceptarLambda(dir, expresion);
    }

    // Los lugares donde el JLS sí admite una lambda: argumento, índice de arreglo,
    // las dos ramas del ternario, lado derecho de un `=` y cuerpo de otra lambda.
    @ParameterizedTest
    @ValueSource(strings = {
            "metodo(x -> x)",
            "metodo((x -> x))",
            "arreglo[x -> x]",
            "a ? x -> x : b",
            "a ? b : x -> x",
            "x -> x = 1",
            "x -> y -> y",
    })
    void unaLambdaSeAceptaDondeElJlsLaAdmite(String expresion, @TempDir Path dir) {
        aceptarLambda(dir, expresion);
    }

    // En Java una sentencia-expresión es un StatementExpression, que nunca es una
    // lambda: `x -> x;` suelto es «not a statement».
    @ParameterizedTest
    @ValueSource(strings = {
            "x -> x;",
            "(a, b) -> a;",
            "() -> 1;",
    })
    void unaLambdaSueltaNoEsUnaSentencia(String sentencia, @TempDir Path dir) {
        rechazar(dir, envolverEnMetodo(sentencia));
    }

    // El lado derecho de un `=` es una Expression del JLS, así que admite lambda;
    // el de una asignación encadenada sigue sin existir.
    @ParameterizedTest
    @ValueSource(strings = {
            "a = x -> x;",
            "a.campo = x -> x;",
            "arreglo[0] = () -> 1;",
    })
    void elLadoDerechoDeUnaAsignacionAdmiteLambda(String sentencia, @TempDir Path dir) {
        aceptar(dir, envolverEnMetodo(sentencia));
    }

    // ------------------------------------------------------ el logro apagado ---

    @Test
    @RequiereLogroApagado(Logro.LAMBDA)
    void sinElLogroLaFlechaNoFormaUnaLambda(@TempDir Path dir) {
        assertEquals(">", rechazarLambda(dir, "x -> x").getLexemaOfensivo());
    }
}
