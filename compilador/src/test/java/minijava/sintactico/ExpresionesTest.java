package minijava.sintactico;

import minijava.comun.config.Logro;
import minijava.logros.RequiereLogro;
import minijava.logros.RequiereLogroApagado;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;

import minijava.logros.ConfiguracionDeLaCorrida;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("sintactico")
class ExpresionesTest extends BaseDeAnalisis {

    private void aceptarExpresion(Path dir, String expresion) {
        aceptar(dir, envolverEnMetodo("var x = " + expresion + ";"));
    }

    private String rechazarExpresion(Path dir, String expresion) {
        return rechazar(dir, envolverEnMetodo("var x = " + expresion + ";")).getLexemaOfensivo();
    }

    // ------------------------------------------------------------- operandos ---

    @ParameterizedTest
    @ValueSource(strings = {
            "true", "false", "null", "1", "0", "999999999", "'c'", "'\\n'", "\"hola\"", "\"\"",
            "this", "a", "a.b", "a.b.c", "a[0]", "a[0][1]", "a[i + 1]",
            "m1()", "m1(a)", "m1(a, b, c)", "this.m1()", "Clase.m1()", "Clase.m1(a)",
            "a.m1().b.m2()", "new Persona()", "new Persona(a, b)",
    })
    void seAceptaCadaFormaDeOperando(String expresion, @TempDir Path dir) {
        aceptarExpresion(dir, expresion);
    }

    @Test
    @RequiereLogro(Logro.FLOAT)
    void conFloatsUnLiteralFlotanteEsUnOperando(@TempDir Path dir) {
        aceptarExpresion(dir, "1.5");
    }

    // ------------------------------------------------------------ operadores ---

    @ParameterizedTest
    @ValueSource(strings = {
            "a + b", "a - b", "a * b", "a / b", "a % b",
            "a < b", "a > b", "a <= b", "a >= b", "a == b", "a != b",
            "a && b", "a || b",
    })
    void seAceptaCadaOperadorBinario(String expresion, @TempDir Path dir) {
        aceptarExpresion(dir, expresion);
    }

    @ParameterizedTest
    @ValueSource(strings = {"+a", "-a", "!a", "-1", "!true"})
    void seAceptaCadaOperadorUnarioPrefijo(String expresion, @TempDir Path dir) {
        aceptarExpresion(dir, expresion);
    }

    // Los trece binarios van en UN solo nivel de precedencia, como en la gramática
    // original: la asociatividad y la precedencia real las resuelve el semántico.
    @Test
    void losOperadoresBinariosSeEncadenanSinLimite(@TempDir Path dir) {
        aceptarExpresion(dir, "a + b * c - d / e % f < g && h || i");
        aceptarExpresion(dir, String.join(" + ", java.util.Collections.nCopies(200, "a")));
    }

    @Test
    void seAceptaElParentizadoAnidadoProfundo(@TempDir Path dir) {
        aceptarExpresion(dir, "(".repeat(80) + "a" + ")".repeat(80));
    }

    // `a = b = c` no es válido: el lado derecho de una asignación es una
    // ExpresionCondicional, igual que en la gramática original de la cátedra.
    @Test
    void laAsignacionNoEncadena(@TempDir Path dir) {
        aceptar(dir, envolverEnMetodo("a = b;"));
        assertEquals("=", rechazar(dir, envolverEnMetodo("a = b = c;")).getLexemaOfensivo());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "a +        | ;",
            "* a        | *",
            "a b        | b",
            "( a        | ;",
            "a )        | )",
            "a[         | ;",
            "m1(,)      | ,",
            "5++        | ++",
    })
    void unaExpresionInvalidaFallaEnElTokenExacto(String expresion, String lexema,
                                                  @TempDir Path dir) {
        assertEquals(lexema, rechazarExpresion(dir, expresion));
    }

    // -------------------------------------------------------------- ternario ---

    @Test
    @RequiereLogro(Logro.TERNARIO)
    void conTernarioSeAceptaElOperadorCondicional(@TempDir Path dir) {
        aceptarExpresion(dir, "a ? b : c");
        aceptarExpresion(dir, "(edad >= 18) ? true : false");
    }

    // Asociativo a derecha, como en Java: `a ? b : c ? d : e` es `a ? b : (c ? d : e)`.
    @Test
    @RequiereLogro(Logro.TERNARIO)
    void elTernarioAnidaEnLasDosRamas(@TempDir Path dir) {
        aceptarExpresion(dir, "a ? b ? c : d : e");
        aceptarExpresion(dir, "a ? b : c ? d : e");
        aceptarExpresion(dir, "a ? b : c ? d : e ? f : g");
    }

    // Sin el logro el `?` ni siquiera es un token de MiniJava, así que el fuente
    // se rechaza en el LÉXICO y nunca llega al analizador. Que el rechazo sea
    // léxico y no sintáctico es lo correcto: el símbolo no existe en el lenguaje
    // base. El caso léxico está cubierto en TokensDeLaEtapaDosTest.
    @Test
    @RequiereLogroApagado(Logro.TERNARIO)
    void sinTernarioUnFuenteConSignoDePreguntaSeRechaza(@TempDir Path dir) {
        assertThrows(Exception.class,
                () -> analizar(dir, envolverEnMetodo("var x = a ? b : c;"),
                        ConfiguracionDeLaCorrida.actual()));
    }

    @Test
    @RequiereLogro(Logro.TERNARIO)
    void unTernarioSinRamaFalsaSeRechaza(@TempDir Path dir) {
        assertEquals(";", rechazarExpresion(dir, "a ? b"));
    }

    // JLS 15.25: el operando del medio es una Expression completa y el de la
    // derecha una ConditionalExpression. O sea que la asignación entra en el
    // medio y no a la derecha, que es exactamente lo que hace javac.
    @Test
    @RequiereLogro(Logro.TERNARIO)
    void laAsignacionEntraEnElOperandoDelMedio(@TempDir Path dir) {
        aceptar(dir, envolverEnMetodo("var y = a ? b = 1 : c;"));
        aceptar(dir, envolverEnMetodo("var y = a ? b = c + 1 : d;"));
    }

    // Una sola asignación, no una cadena: el lado derecho de un `=` sigue siendo
    // una ExpresionCondicional, así que `a = b = c` no vale ni dentro del medio
    // del ternario. Es la regla de la gramática original, que el logro no cambia.
    @Test
    @RequiereLogro(Logro.TERNARIO)
    void elOperandoDelMedioNoEncadenaAsignaciones(@TempDir Path dir) {
        assertEquals("=", rechazarExpresion(dir, "a ? b = c = 1 : d"));
    }

    @Test
    @RequiereLogro(Logro.TERNARIO)
    void laAsignacionNoEntraEnElOperandoDerecho(@TempDir Path dir) {
        assertEquals("=", rechazarExpresion(dir, "a ? b : c = 1"));
    }

    // -------------------------------------------------------------- posfijos ---

    @Test
    @RequiereLogro(Logro.POSFIJO)
    void conPosfijosSeAceptanIncrementoYDecremento(@TempDir Path dir) {
        aceptar(dir, envolverEnMetodo("x++;"));
        aceptar(dir, envolverEnMetodo("x--;"));
        aceptar(dir, envolverEnMetodo("a.b++;"));
        aceptar(dir, envolverEnMetodo("a[0]++;"));
    }

    @Test
    @RequiereLogroApagado(Logro.POSFIJO)
    void sinPosfijosElIncrementoSeRechaza(@TempDir Path dir) {
        assertEquals("++", rechazar(dir, envolverEnMetodo("x++;")).getLexemaOfensivo());
    }

    // El posfijo va sobre Referencia y no sobre Operando: `5++` no tiene sentido.
    @Test
    @RequiereLogro(Logro.POSFIJO)
    void unLiteralNoAdmitePosfijo(@TempDir Path dir) {
        assertEquals("++", rechazarExpresion(dir, "5++"));
    }

    // `+` y `-` son binarios y también unarios prefijos, así que `a + + b` es
    // `a + (+b)` y es válido. No es un descuido de la gramática: está en la
    // original, y Java hace lo mismo.
    @Test
    void unBinarioSeguidoDeUnUnarioEsValido(@TempDir Path dir) {
        aceptarExpresion(dir, "a + + b");
        aceptarExpresion(dir, "a - -b");
        aceptarExpresion(dir, "a * !b");
    }

    // Un solo posfijo por referencia: `x++++` no es válido.
    @Test
    @RequiereLogro(Logro.POSFIJO)
    void noSeEncadenanDosPosfijos(@TempDir Path dir) {
        assertEquals("++", rechazar(dir, envolverEnMetodo("x++++;")).getLexemaOfensivo());
    }

    // -------------------------------------------------------------- creación ---

    @ParameterizedTest
    @ValueSource(strings = {
            "new Persona()", "new Persona(a)", "new Persona(a, b)",
            "new int[3]", "new int[3][4]", "new int[a + 1]",
            "new Persona[3]", "new T[3]",
            "new Lista<Persona>()", "new Lista<T>()",
    })
    void seAceptaCadaFormaDeCreacion(String expresion, @TempDir Path dir) {
        aceptarExpresion(dir, expresion);
    }

    // `new int[3][4]` es una creación 2-D, no un acceso al arreglo recién creado:
    // el `[` se consume voraz como otra dimensión. Es la ambigüedad heredada de la
    // gramática de la cátedra, y coincide con Java.
    @Test
    void unaCreacionConDosDimensionesEsUnaSolaCreacion(@TempDir Path dir) {
        aceptarExpresion(dir, "new int[3][4][5]");
    }

    @Test
    @RequiereLogro(Logro.ARRINIT)
    void conInicializacionDeArreglosSeAceptaElInicializador(@TempDir Path dir) {
        aceptarExpresion(dir, "new int[]{1, 2, 3}");
        aceptarExpresion(dir, "new int[]{}");
        aceptarExpresion(dir, "new int[]{a + 1, m1()}");
    }

    // El logro dice «como en Java» y sólo restringe DÓNDE, no cuántas dimensiones.
    @Test
    @RequiereLogro(Logro.ARRINIT)
    void elInicializadorDeArreglosEsAnidable(@TempDir Path dir) {
        aceptarExpresion(dir, "new int[][]{{1, 2}, {3, 4}}");
        aceptarExpresion(dir, "new int[][][]{{{1}}}");
    }

    @Test
    @RequiereLogroApagado(Logro.ARRINIT)
    void sinInicializacionDeArreglosElCorcheteVacioSeRechaza(@TempDir Path dir) {
        assertEquals("]", rechazarExpresion(dir, "new int[]{1, 2}"));
    }

    // El inicializador sólo se admite dentro de un `new`, nunca suelto.
    @Test
    @RequiereLogro(Logro.ARRINIT)
    void elInicializadorNoSeAdmiteFueraDeUnNew(@TempDir Path dir) {
        assertEquals("{", rechazarExpresion(dir, "{1, 2}"));
    }

    @Test
    @RequiereLogro(Logro.GENERICO)
    void conGenericidadAvanzadaSeAceptaElDiamante(@TempDir Path dir) {
        aceptarExpresion(dir, "new Lista<>()");
    }

    // El diamante sólo vale al instanciar una clase genérica: `new C<>[3]` no.
    @Test
    @RequiereLogro(Logro.GENERICO)
    void elDiamanteNoSeAdmiteAlCrearUnArreglo(@TempDir Path dir) {
        assertEquals("[", rechazarExpresion(dir, "new Lista<>[3]"));
    }

    @Test
    @RequiereLogroApagado(Logro.GENERICO)
    void sinGenericidadAvanzadaElDiamanteSeRechaza(@TempDir Path dir) {
        assertEquals(">", rechazarExpresion(dir, "new Lista<>()"));
    }
}
