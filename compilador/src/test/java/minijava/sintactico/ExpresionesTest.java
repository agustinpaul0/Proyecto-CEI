package minijava.sintactico;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;


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
    void conTernarioSeAceptaElOperadorCondicional(@TempDir Path dir) {
        aceptarExpresion(dir, "a ? b : c");
        aceptarExpresion(dir, "(edad >= 18) ? true : false");
    }

    // Asociativo a derecha, como en Java: `a ? b : c ? d : e` es `a ? b : (c ? d : e)`.
    @Test
    void elTernarioAnidaEnLasDosRamas(@TempDir Path dir) {
        aceptarExpresion(dir, "a ? b ? c : d : e");
        aceptarExpresion(dir, "a ? b : c ? d : e");
        aceptarExpresion(dir, "a ? b : c ? d : e ? f : g");
    }

    // La rama falsa es obligatoria: `a ? b` no deriva, y el rechazo cae en el `;`
    // que la expresión cierra, no en el `?`.
    @Test
    void unTernarioSinRamaFalsaSeRechaza(@TempDir Path dir) {
        assertEquals(";", rechazarExpresion(dir, "a ? b"));
    }

    // JLS 15.25: el operando del medio es una Expression completa y el de la
    // derecha una ConditionalExpression. O sea que la asignación entra en el
    // medio y no a la derecha, que es exactamente lo que hace javac.
    @Test
    void laAsignacionEntraEnElOperandoDelMedio(@TempDir Path dir) {
        aceptar(dir, envolverEnMetodo("var y = a ? b = 1 : c;"));
        aceptar(dir, envolverEnMetodo("var y = a ? b = c + 1 : d;"));
    }

    // Una sola asignación, no una cadena: el lado derecho de un `=` es una
    // ExpresionCondicional, así que `a = b = c` no vale ni dentro del medio del
    // ternario. Es la regla de la gramática original.
    @Test
    void elOperandoDelMedioNoEncadenaAsignaciones(@TempDir Path dir) {
        assertEquals("=", rechazarExpresion(dir, "a ? b = c = 1 : d"));
    }

    @Test
    void laAsignacionNoEntraEnElOperandoDerecho(@TempDir Path dir) {
        assertEquals("=", rechazarExpresion(dir, "a ? b : c = 1"));
    }

    // El lado izquierdo del `=` es una ExpresionCompuesta, como en la gramática
    // original, y un ternario no lo es. javac también lo rechaza.
    @Test
    void unTernarioNoEsElLadoIzquierdoDeUnaAsignacion(@TempDir Path dir) {
        assertEquals("=", rechazar(dir, envolverEnMetodo("a ? b : c = 1;")).getLexemaOfensivo());
        assertEquals("=", rechazar(dir, envolverEnMetodo("a + b ? c : d = 1;")).getLexemaOfensivo());
    }

    // Entre paréntesis el ternario es un primario, o sea una ExpresionCompuesta:
    // la gramática original lo admite como lado izquierdo, igual que `a + b = 1`.
    @Test
    void unTernarioParentizadoEsUnLadoIzquierdoValido(@TempDir Path dir) {
        aceptar(dir, envolverEnMetodo("(a ? b : c) = 1;"));
    }

    // -------------------------------------------------------------- posfijos ---

    @Test
    void conPosfijosSeAceptanIncrementoYDecremento(@TempDir Path dir) {
        aceptar(dir, envolverEnMetodo("x++;"));
        aceptar(dir, envolverEnMetodo("x--;"));
        aceptar(dir, envolverEnMetodo("a.b++;"));
        aceptar(dir, envolverEnMetodo("a[0]++;"));
    }

    // El posfijo va sobre Referencia y no sobre Operando: `5++` no tiene sentido.
    @Test
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

    // Con inicializador ninguna dimensión lleva tamaño, como en Java (JLS 15.10.1).
    @Test
    void elInicializadorVaConDimensionesSinTamanio(@TempDir Path dir) {
        aceptarExpresion(dir, "new int[]{1, 2, 3}");
        aceptarExpresion(dir, "new int[]{}");
        aceptarExpresion(dir, "new int[]{a + 1, m1()}");
        aceptarExpresion(dir, "new Clase[]{a, b}");
    }

    // Anidable: una dimensión más, un nivel más de llaves.
    @Test
    void elInicializadorDeArreglosEsAnidable(@TempDir Path dir) {
        aceptarExpresion(dir, "new int[][]{{1, 2}, {3, 4}}");
        aceptarExpresion(dir, "new int[][][]{{{1}}}");
        aceptarExpresion(dir, "new int[][]{{1}, {}}");
    }

    @Test
    void elInicializadorAnidadoAdmiteListasVacias(@TempDir Path dir) {
        aceptarExpresion(dir, "new int[][]{}");
        aceptarExpresion(dir, "new int[][]{{}}");
        aceptarExpresion(dir, "new int[][]{{}, {}}");
        aceptarExpresion(dir, "new int[][][]{{}, {{}}}");
    }

    @Test
    void losElementosDelInicializadorVanSeparadosPorComas(@TempDir Path dir) {
        assertEquals("{", rechazarExpresion(dir, "new int[][]{{}{}}"));
        assertEquals("{", rechazarExpresion(dir, "new int[][]{{1}{2}}"));
        assertEquals("2", rechazarExpresion(dir, "new int[]{1 2}"));
    }

    // Que el anidamiento coincida con las dimensiones es un chequeo de tipos: el
    // parser de javac acepta los dos, y el error lo da el análisis semántico.
    @Test
    void elAnidamientoDelInicializadorNoSeChequeaContraLasDimensiones(@TempDir Path dir) {
        aceptarExpresion(dir, "new int[]{{}}");
        aceptarExpresion(dir, "new int[][]{1}");
    }

    // La coma final es opcional, como en Java (JLS 10.6).
    @Test
    void elInicializadorAdmiteLaComaFinal(@TempDir Path dir) {
        aceptarExpresion(dir, "new int[]{1, 2,}");
        aceptarExpresion(dir, "new int[]{,}");
        aceptarExpresion(dir, "new int[][]{{1,}, {2},}");
    }

    @Test
    void elInicializadorNoAdmiteComasDeMas(@TempDir Path dir) {
        assertEquals(",", rechazarExpresion(dir, "new int[]{1,,}"));
        assertEquals("1", rechazarExpresion(dir, "new int[]{,1}"));
        assertEquals(",", rechazarExpresion(dir, "new int[]{,,}"));
    }

    // Tamaño e inicializador no van juntos: javac rechaza «array creation with both
    // dimension expression and initialization is illegal».
    @Test
    void elInicializadorNoAdmiteDimensionesConTamanio(@TempDir Path dir) {
        assertEquals("{", rechazarExpresion(dir, "new int[3]{1, 2, 3}"));
        assertEquals("{", rechazarExpresion(dir, "new int[2][2]{{1, 2}, {3, 4}}"));
    }

    // Sin inicializador toda dimensión lleva su tamaño, como en MiniJava base.
    @Test
    void sinInicializadorTodaDimensionLlevaTamanio(@TempDir Path dir) {
        assertEquals(";", rechazarExpresion(dir, "new int[]"));
        assertEquals("]", rechazarExpresion(dir, "new int[2][]"));
        assertEquals("2", rechazarExpresion(dir, "new int[][2]{}"));
    }

    // Un arreglo inicializado es un primario como cualquier creación: arrastra
    // cadena, igual que en Java.
    @Test
    void unArregloInicializadoArrastraCadena(@TempDir Path dir) {
        aceptarExpresion(dir, "new int[]{1, 2}.length");
        aceptarExpresion(dir, "new int[]{1, 2}[0]");
    }

    // El inicializador sólo se admite dentro de un `new`, nunca suelto.
    @Test
    void elInicializadorNoSeAdmiteFueraDeUnNew(@TempDir Path dir) {
        assertEquals("{", rechazarExpresion(dir, "{1, 2}"));
    }

    @Test
    void conGenericidadAvanzadaSeAceptaElDiamante(@TempDir Path dir) {
        aceptarExpresion(dir, "new Lista<>()");
    }

    // El diamante sólo vale al instanciar una clase genérica: `new C<>[3]` no.
    @Test
    void elDiamanteNoSeAdmiteAlCrearUnArreglo(@TempDir Path dir) {
        assertEquals("[", rechazarExpresion(dir, "new Lista<>[3]"));
    }

}
