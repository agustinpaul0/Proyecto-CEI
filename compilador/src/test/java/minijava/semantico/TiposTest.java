package minijava.semantico;

import minijava.lexico.tokens.TipoDeToken;
import minijava.lexico.tokens.Token;
import minijava.semantico.tipos.Sustitucion;
import minijava.semantico.tipos.Tipo;
import minijava.semantico.tipos.TipoArreglo;
import minijava.semantico.tipos.TipoPrimitivo;
import minijava.semantico.tipos.TipoReferencia;
import minijava.semantico.tipos.TipoVoid;
import minijava.semantico.tipos.VariableDeTipo;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("semantico")
class TiposTest {

    private static final Sustitucion T_POR_STRING = Sustitucion.de("T", referencia("String"));

    @Test
    void cadaTipoSeNombraComoSeEscribe() {
        assertEquals("boolean", TipoPrimitivo.BOOLEAN.nombre());
        assertEquals("float", TipoPrimitivo.FLOAT.nombre());
        assertEquals("void", TipoVoid.VOID.nombre());
        assertEquals("T", variable("T").nombre());
        assertEquals("Caja", referencia("Caja").nombre());
        assertEquals("Caja<T>", generica("Caja", variable("T")).nombre());
        assertEquals("Caja<Par<String>>", generica("Caja", generica("Par", referencia("String"))).nombre());
        assertEquals("int[][]", TipoArreglo.de(TipoPrimitivo.INT, 2).nombre());
    }

    @Test
    void ceroDimensionesEsElTipoBase() {
        assertSame(TipoPrimitivo.CHAR, TipoArreglo.de(TipoPrimitivo.CHAR, 0));
    }

    // Dos tipos escritos en líneas distintas son el mismo tipo.
    @Test
    void laIgualdadNoDependeDelTokenNiDeLaLinea() {
        Tipo enLaLineaUno = new TipoReferencia(new Token(TipoDeToken.ID_CLASE, "Caja", 1));
        Tipo enLaLineaNueve = new TipoReferencia(new Token(TipoDeToken.ID_CLASE, "Caja", 9));

        assertTrue(enLaLineaUno.esIgualA(enLaLineaNueve));
        assertFalse(referencia("Caja").esIgualA(generica("Caja", referencia("String"))));
        assertFalse(TipoArreglo.de(TipoPrimitivo.INT, 1).esIgualA(TipoArreglo.de(TipoPrimitivo.INT, 2)));
        assertFalse(TipoPrimitivo.INT.esIgualA(TipoPrimitivo.CHAR));
    }

    @Test
    void instanciarReemplazaElParametroDondeAparezca() {
        assertEquals("String", variable("T").instanciar(T_POR_STRING).nombre());
        assertEquals("String[][]", TipoArreglo.de(variable("T"), 2).instanciar(T_POR_STRING).nombre());
        assertEquals("Caja<String>", generica("Caja", variable("T")).instanciar(T_POR_STRING).nombre());
    }

    @Test
    void instanciarNoTocaOtroParametroNiLosTiposSinParametro() {
        assertEquals("U", variable("U").instanciar(T_POR_STRING).nombre());
        assertSame(TipoPrimitivo.INT, TipoPrimitivo.INT.instanciar(T_POR_STRING));
        assertSame(TipoVoid.VOID, TipoVoid.VOID.instanciar(T_POR_STRING));
        assertEquals("Caja", referencia("Caja").instanciar(T_POR_STRING).nombre());
    }

    @Test
    void laIdentidadConservaElParametro() {
        assertEquals("T", variable("T").instanciar(Sustitucion.identidad()).nombre());
    }

    private static Tipo variable(String nombre) {
        return new VariableDeTipo(new Token(TipoDeToken.ID_GEN, nombre, 1));
    }

    private static TipoReferencia referencia(String nombre) {
        return new TipoReferencia(new Token(TipoDeToken.ID_CLASE, nombre, 1));
    }

    private static TipoReferencia generica(String nombre, Tipo argumento) {
        return new TipoReferencia(new Token(TipoDeToken.ID_CLASE, nombre, 1), argumento);
    }
}
