package minijava.sintactico;

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

    // --------------------------- lo que el sintáctico NO mira de una lambda ---
    // Qué nombres usa el cuerpo —`this`, un campo, una variable de afuera— y si
    // los parámetros se repiten son restricciones de SIGNIFICADO: para resolverlas
    // hay que asociar cada nombre con su declaración, que es la tabla de símbolos
    // del analizador semántico. El sintáctico las acepta a todas.

    @Test
    void elThisDentroDeUnaLambdaSeAceptaYQuedaParaElSemantico(@TempDir Path dir) {
        aceptarLambda(dir, "x -> this.campo");
        aceptarLambda(dir, "x -> this");
    }

    @Test
    void capturarUnaVariableQueNoEsParametroSeAceptaYQuedaParaElSemantico(@TempDir Path dir) {
        aceptarLambda(dir, "x -> x + n");
        aceptarLambda(dir, "x -> metodo(n)");
        aceptarLambda(dir, "x -> y -> x + y");
    }

    @Test
    void unParametroRepetidoSeAceptaYQuedaParaElSemantico(@TempDir Path dir) {
        aceptarLambda(dir, "(a, a) -> a");
    }

    @Test
    void unaLlamadaDentroDeUnaLambdaSeAcepta(@TempDir Path dir) {
        aceptarLambda(dir, "x -> metodo(x)");
        aceptarLambda(dir, "() -> arrancar()");
        aceptarLambda(dir, "x -> x.metodo()");
        aceptarLambda(dir, "x -> Clase.metodo(x)");
    }

    @Test
    void fueraDeUnaLambdaElThisSigueSiendoValido(@TempDir Path dir) {
        aceptar(dir, envolverEnMetodo("this.campo = 1;"));
        aceptar(dir, envolverEnMetodo("metodo();"));
        aceptar(dir, envolverEnMetodo("var f = x -> x; this.campo = 1;"));
        aceptar(dir, envolverEnMetodo("var f = m1(x -> x, this);"));
    }

    @Test
    void usarElPropioParametroSeAcepta(@TempDir Path dir) {
        aceptarLambda(dir, "x -> x");
        aceptarLambda(dir, "(a, b) -> a * b");
        aceptarLambda(dir, "(a, b) -> b");
        aceptarLambda(dir, "x -> x.n");
        aceptarLambda(dir, "x -> x.a.b.c");
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

    // El paréntesis anidado no lava la restricción: lo que cuenta es si la RAÍZ de
    // lo que hay adentro es una lambda, por muchos paréntesis que la envuelvan.
    @ParameterizedTest
    @ValueSource(strings = {
            "((x -> x)).metodo()",
            "(((x -> x))).metodo()",
            "((() -> 1)) + 1",
    })
    void elParentesisAnidadoNoHabilitaLaCola(String expresion, @TempDir Path dir) {
        rechazarLambda(dir, expresion);
    }

    // La restricción es sobre la RAÍZ, no sobre «se vio una lambda». Con un `=` o
    // un `?` arriba, la expresión es una asignación o un ternario —no una lambda—
    // y puede llevar cola: `javac` acepta la forma de las tres.
    @ParameterizedTest
    @ValueSource(strings = {
            "(a = x -> x).metodo()",
            "(a ? x -> x : b).metodo()",
            "(a ? b : x -> x).metodo()",
    })
    void unaLambdaQueNoEsLaRaizNoBloqueaLaCola(String expresion, @TempDir Path dir) {
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

    // ------------------------------------- el parametro entre parentesis -------
    // El contenido de un paréntesis se lee como expresión, porque hasta ver el
    // `->` que sigue al `)` no hay forma de saber que era un parámetro. Mientras
    // tanto el chequeo de captura queda EN DUDA: darlo por captura rechazaría la
    // misma lambda que sin paréntesis se acepta.

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "x -> y -> y        | x -> (y) -> y",
            "a -> a             | (a) -> a",
            "x -> y -> z -> z   | x -> (y) -> (z) -> z",
    })
    void parentizarElParametroNoCambiaElVeredicto(String sinParentesis, String conParentesis,
                                                  @TempDir Path dir) {
        aceptarLambda(dir, sinParentesis);
        aceptarLambda(dir, conParentesis);
    }

    // Parentizar de más NO es parentizar el parámetro: `((a)) -> a` no es una
    // lambda en Java tampoco, porque la lista de parámetros es `(a)` y envolverla
    // otra vez la deja de ser.
    @ParameterizedTest
    @ValueSource(strings = {
            "((a)) -> a",
            "x -> ((a, b)) -> a",
    })
    void parentizarDeMasNoEsUnaListaDeParametros(String lambda, @TempDir Path dir) {
        rechazarLambda(dir, lambda);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "x -> (y) -> y",
            "x -> (y, z) -> y + z",
            "x -> ((y) -> y)",
            "x -> (y) -> (z) -> z",
    })
    void elParametroDeUnaLambdaAnidadaEntreParentesisNoEsUnaCaptura(String lambda,
                                                                    @TempDir Path dir) {
        aceptarLambda(dir, lambda);
    }

    // El paréntesis que resultó ser una expresión y no una lista de parámetros
    // sigue siendo una expresión válida: qué nombra cada nombre es del semántico.
    @ParameterizedTest
    @ValueSource(strings = {
            "x -> (a + b)",
            "x -> (a)",
            "x -> ((a))",
            "x -> (y) -> x",
            "x -> (y) -> z",
            "x -> ((y) -> z)",
    })
    void elParentesisQueResultoSerExpresionSigueSiendoUnaExpresion(String lambda,
                                                                   @TempDir Path dir) {
        aceptarLambda(dir, lambda);
    }
}
