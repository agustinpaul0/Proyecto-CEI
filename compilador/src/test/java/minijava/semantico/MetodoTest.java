package minijava.semantico;

import minijava.lexico.tokens.TipoDeToken;
import minijava.lexico.tokens.Token;
import minijava.semantico.entidades.Metodo;
import minijava.semantico.tipos.Tipo;
import minijava.semantico.tipos.TipoPrimitivo;
import minijava.semantico.tipos.TipoVoid;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Documento de semántica §6.2: la signatura son los tipos de los parámetros, en
// orden, y el retorno. Los nombres de los parámetros no cuentan.
@Tag("semantico")
class MetodoTest {

    @Test
    void losNombresDeLosParametrosNoCuentan() {
        assertTrue(metodo(TipoVoid.VOID, "a", TipoPrimitivo.INT)
                .mismaSignaturaQue(metodo(TipoVoid.VOID, "b", TipoPrimitivo.INT)));
    }

    @Test
    void otraAridadEsOtraSignatura() {
        assertFalse(metodo(TipoVoid.VOID).mismaSignaturaQue(metodo(TipoVoid.VOID, "a", TipoPrimitivo.INT)));
    }

    @Test
    void otroRetornoEsOtraSignatura() {
        assertFalse(metodo(TipoVoid.VOID).mismaSignaturaQue(metodo(TipoPrimitivo.INT)));
    }

    @Test
    void otroTipoDeParametroEsOtraSignatura() {
        assertFalse(metodo(TipoVoid.VOID, "a", TipoPrimitivo.INT)
                .mismaSignaturaQue(metodo(TipoVoid.VOID, "a", TipoPrimitivo.CHAR)));
    }

    private Metodo metodo(Tipo retorno) {
        Metodo metodo = new Metodo(new Token(TipoDeToken.ID_MV, "m", 1));
        metodo.setRetorno(retorno);
        return metodo;
    }

    private Metodo metodo(Tipo retorno, String parametro, Tipo tipo) {
        Metodo metodo = metodo(retorno);
        metodo.agregarParametro(new Token(TipoDeToken.ID_MV, parametro, 1), tipo);
        return metodo;
    }
}
