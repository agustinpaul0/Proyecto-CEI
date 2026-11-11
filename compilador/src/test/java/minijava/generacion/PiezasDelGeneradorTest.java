package minijava.generacion;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Las piezas del paquete de generación, cada una aislada.
@Tag("generacion")
class PiezasDelGeneradorTest {

    // CeIASM no distingue mayúsculas y sólo admite ASCII (Apunte §2).
    @Test
    void laCodificacionDeNombresNoConfundeMayusculasNiLetrasNoAscii() {
        assertEquals("abc_9", Etiquetas.codificar("abc_9"));
        assertEquals("$41$a", Etiquetas.codificar("Aa"));
        assertEquals("$41$$41$", Etiquetas.codificar("AA"));
        assertEquals("a$f1$o", Etiquetas.codificar("año"));
        assertNotEquals(Etiquetas.codificar("Aa"), Etiquetas.codificar("aA"));
    }

    @Test
    void lasEtiquetasTienenSusPartesSeparadasPorArroba() {
        assertEquals("metodo@$41$a@m@2", Etiquetas.metodo("Aa", "m", 2));
        assertEquals("constructor@$41$a@0", Etiquetas.constructor("Aa", 0));
        assertEquals("vt@$41$a", Etiquetas.tablaVirtual("Aa"));
        assertEquals("x@runtime", Etiquetas.rutina("x"));
    }

    @Test
    void elGeneradorAcumulaSeccionesInstruccionesEtiquetasYDatos() {
        Generador g = new Generador();
        g.seccionDeCodigo();
        g.gen("PUSH 1");
        g.etiqueta("fin");
        g.seccionDeDatos();
        g.datos("vt", List.of("a", "b"));
        g.cadena("msj", "hola");
        assertEquals(".CODE\n    PUSH 1\nfin: NOP\n.DATA\nvt: DW a, b\nmsj: DW \"hola\", 0\n", g.texto());
    }

    @Test
    void lasEtiquetasNuevasNoSeRepiten() {
        Generador g = new Generador();
        assertEquals("if@1", g.etiquetaNueva("if"));
        assertEquals("if@2", g.etiquetaNueva("if"));
        assertEquals("while@3", g.etiquetaNueva("while"));
    }

    @ParameterizedTest(name = "{0} vale {1}")
    @CsvSource(delimiter = '|', quoteCharacter = '`', value = {
            "'a'|97", "'Z'|90", "' '|32",
            "'\\n'|10", "'\\t'|9", "'\\b'|8", "'\\f'|12", "'\\r'|13", "'\\s'|32",
            "'\\''|39", "'\\\"'|34", "'\\\\'|92",
            "'\\0'|0", "'\\7'|7", "'\\101'|65", "'\\377'|255",
            "'\\u0041'|65", "'\\uu0041'|65", "'\\u00f1'|241",
            "'\\q'|113", "'\\x'|120", "'ñ'|241"})
    void elValorDeUnLiteralChar(String lexema, int valor) {
        assertEquals(valor, ValorDeLiteral.caracter(lexema));
    }

    @Test
    void elValorDeUnLiteralString() {
        assertEquals(List.of(), ValorDeLiteral.cadena("\"\""));
        assertEquals(List.of(97, 9, 98), ValorDeLiteral.cadena("\"a\\tb\""));
        assertEquals(List.of(65, 48), ValorDeLiteral.cadena("\"\\1010\""));
        assertEquals(List.of(32, 48), ValorDeLiteral.cadena("\"\\400\""));
        assertEquals(List.of(34, 92), ValorDeLiteral.cadena("\"\\\"\\\\\""));
        assertEquals(List.of(65, 66), ValorDeLiteral.cadena("\"\\u0041\\uuu0042\""));
        assertEquals(List.of(113), ValorDeLiteral.cadena("\"\\q\""));
    }

    // El malloc deja el bloque en cero aunque la memoria tenga otra cosa: acá se la
    // ensucia a mano, justo por encima del límite del heap, antes de alojar.
    @Test
    void elMallocDejaElBloqueEnCero() {
        Generador g = new Generador();
        g.seccionDeCodigo();
        for (int i = 1; i <= 4; i++) {
            g.gen("LOADHL");
            g.gen("PUSH 77");
            g.gen("STOREREF " + i);
        }
        g.gen("RMEM 1");
        g.gen("PUSH 4");
        g.gen("PUSH " + Etiquetas.MALLOC);
        g.gen("CALL");
        for (int i = 0; i < 4; i++) {
            g.gen("DUP");
            g.gen("LOADREF " + i);
            g.gen("IPRINT");
        }
        g.gen("HALT");
        RutinasDeSoporte.generar(g);
        g.seccionDeDatos();
        g.datos(Etiquetas.tablaVirtual("Object"), List.of("0"));
        g.datos(Etiquetas.tablaVirtual("String"), List.of("0"));
        EjecutorCeIVM.Ejecucion ejecucion = EjecutorCeIVM.correr(g.texto());
        assertNull(ejecucion.error());
        assertEquals("0000", ejecucion.salida());
    }

    @Test
    void lasRutinasDeSoporteEnsamblanSolas() {
        Generador g = new Generador();
        RutinasDeSoporte.generar(g);
        g.seccionDeDatos();
        g.datos(Etiquetas.tablaVirtual("Object"), List.of("0"));
        g.datos(Etiquetas.tablaVirtual("String"), List.of("0"));
        assertNull(EjecutorCeIVM.errorDeEnsamblado(g.texto()));
        assertTrue(g.texto().contains(RutinasDeSoporte.NUEVO_ARREGLO_MULTI + ": NOP"));
    }
}
