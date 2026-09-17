package minijava.sintactico;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

// El caso difícil de las lambdas: el `(`.
//
// Un `(` abre tanto una expresión parentizada como la lista de parámetros de una
// lambda, y `(x -> y)` es las dos cosas a la vez según lo que venga después. Esta
// suite fija el comportamiento de toda la familia, porque es el punto donde este
// analizador diverge de otras implementaciones de MiniJava: la mayoría produce la
// lambda en <Primario>, y entonces una lambda parentizada puede ser operando.
//
// Acá la lambda vive en el nivel de <Expresion>, como el JLS, así que NO puede
// serlo. Cada rechazo de abajo está verificado contra javac, que responde
// «lambda expression not expected here».
@Tag("sintactico")
class ParentesisYLambdaTest extends BaseDeAnalisis {

    // --------------------------------------------------- la lambda es toda la ---
    // Cuando `(x -> y)` es la expresión entera, parentizarla no molesta.

    @ParameterizedTest
    @ValueSource(strings = {
            "var a = (x -> y);",
            "var a = ((x -> y));",
            "var a = (((x -> y)));",
            "a = (x -> y);",
            "f((x -> y));",
            "f((x -> y), 1);",
            "f(1, (x -> y));",
            "var a = b[(x -> y)];",
            "var a = c ? (x -> y) : (z -> w);",
            "var a = (x -> (y -> z));",
            "var a = (x -> (y));",
            "var a = () -> (x -> y);",
    })
    void unaLambdaParentizadaQueEsTodaLaExpresionSeAcepta(String sentencia, @TempDir Path dir) {
        aceptar(dir, envolverEnMetodo(sentencia));
    }

    // ------------------------------------------- la lambda como operando: no ---
    // `f((x -> x))` es válido en Java y `(x -> x).m()` no. El paréntesis no la
    // convierte en operando, y la gramática no puede distinguir los dos casos sin
    // clonar la cadena de colas entera, así que lo cierra el analizador.

    @ParameterizedTest
    @ValueSource(strings = {
            "var a = (x -> y).m();",
            "var a = (x -> y).campo;",
            "var a = (x -> y)[0];",
            "var a = (x -> y) + 1;",
            "var a = (x -> y) * 2;",
            "var a = (x -> y) == null;",
            "var a = (x -> y) ? 1 : 2;",
            "var a = ((x -> y)).m();",
            "var a = (((x -> y))).m();",
            "var a = (x -> y)++;",
    })
    void unaLambdaParentizadaNoPuedeSerOperando(String sentencia, @TempDir Path dir) {
        var excepcion = rechazar(dir, envolverEnMetodo(sentencia));

        assertTrue(excepcion.getMessage().contains(
                        "una lambda no puede ser operando de " + excepcion.getLexemaOfensivo()),
                sentencia + " -> " + excepcion.getMessage());
    }

    // La raíz importa, no si se vio una lambda en algún lado: con un `=` o un `?`
    // por encima, la raíz ya no es la lambda y la cola vuelve a ser válida.
    @ParameterizedTest
    @ValueSource(strings = {
            "var a = (b = x -> y).m();",
            "var a = (c ? d : x -> y).m();",
            "var a = (b = x -> y) + 1;",
    })
    void conOtraCosaEnLaRaizLaColaVuelveAserValida(String sentencia, @TempDir Path dir) {
        aceptar(dir, envolverEnMetodo(sentencia));
    }

    // ------------------------------------------ paréntesis: lista o expresión ---
    // Los cuatro casos que deciden con UN token de anticipación.

    @Test
    void elParentesisSeDesambiguaConUnSoloToken(@TempDir Path dir) {
        aceptar(dir, envolverEnMetodo("var a = () -> 1;"));          // `()` sólo puede ser lambda
        aceptar(dir, envolverEnMetodo("var a = (p, q) -> p;"));      // `(e ,` sólo puede ser lambda
        aceptar(dir, envolverEnMetodo("var a = (p) -> p;"));         // `(e)` seguido de `->`
        aceptar(dir, envolverEnMetodo("var a = (p) + 1;"));          // `(e)` seguido de otra cosa
        aceptar(dir, envolverEnMetodo("var a = x -> y;"));           // sin paréntesis
    }

    // Parentizar de más NO es parentizar la lista de parámetros: la lista es `(a)`
    // y envolverla otra vez la deja de ser. Java hace lo mismo.
    @ParameterizedTest
    @ValueSource(strings = {
            "var a = ((p)) -> p;",
            "var a = x -> ((p, q)) -> p;",
    })
    void parentizarDeMasNoEsUnaListaDeParametros(String sentencia, @TempDir Path dir) {
        rechazar(dir, envolverEnMetodo(sentencia));
    }

    // El parámetro tiene que ser un identificador sin tipo. Es lo otro que la
    // gramática no puede expresar: hasta ver el `->` las dos formas son una
    // <Expresion> entre paréntesis.
    @ParameterizedTest
    @ValueSource(strings = {
            "var a = (p + 1) -> p;",
            "var a = (m1()) -> p;",
            "var a = (1) -> p;",
            "var a = (p.q) -> p;",
            "var a = (p + 1, q) -> p;",
    })
    void unParametroQueNoEsUnIdentificadorSeRechaza(String sentencia, @TempDir Path dir) {
        var excepcion = rechazar(dir, envolverEnMetodo(sentencia));

        assertTrue(excepcion.getMessage().contains(
                        "un parametro de lambda tiene que ser un identificador"),
                sentencia + " -> " + excepcion.getMessage());
    }

    // Un paréntesis que resultó ser una expresión sigue siendo una expresión, y
    // qué nombra cada nombre adentro lo resuelve el analizador semántico.
    @ParameterizedTest
    @ValueSource(strings = {
            "var a = x -> (p + q);",
            "var a = x -> (p);",
            "var a = x -> ((p));",
            "var a = x -> (p) -> x;",
            "var a = x -> (p) -> z;",
            "var a = x -> ((p) -> z);",
    })
    void elParentesisQueResultoSerExpresionSigueSiendoValido(String sentencia, @TempDir Path dir) {
        aceptar(dir, envolverEnMetodo(sentencia));
    }
}
