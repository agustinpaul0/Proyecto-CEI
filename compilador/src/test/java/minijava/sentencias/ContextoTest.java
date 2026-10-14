package minijava.sentencias;

import minijava.lexico.tokens.TipoDeToken;
import minijava.lexico.tokens.Token;
import minijava.semantico.ast.Contexto;
import minijava.semantico.entidades.Clase;
import minijava.semantico.errores.ExcepcionSemantica;
import minijava.semantico.tabla.TablaDeSimbolos;
import minijava.semantico.tipos.TipoPrimitivo;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Los ámbitos de Contexto: locales por bloque, parámetros y el orden del §10.1.
@Tag("sentencias")
class ContextoTest extends BaseDeSentencias {

    private Contexto contextoDe(String metodo, int aridad) throws Exception {
        parsear("""
                class Caja<T> {
                    int x;
                    Caja() { }
                    void instancia(int p, boolean q) { }
                    static void estatico() { }
                }
                class Aa { }
                """);
        Clase caja = (Clase) TablaDeSimbolos.tipo("Caja").orElseThrow();
        if (metodo == null) {
            return new Contexto(caja, caja.constructor(0).orElseThrow());
        }
        return new Contexto(caja, metodo("Caja", metodo, aridad));
    }

    private static Token nombre(String lexema) {
        return new Token(TipoDeToken.ID_MV, lexema, 7);
    }

    @Test
    void unParametroSeVeDesdeElComienzo() throws Exception {
        Contexto contexto = contextoDe("instancia", 2);
        assertEquals(Optional.of(TipoPrimitivo.INT), contexto.localOParametro("p"));
        assertEquals(Optional.empty(), contexto.localOParametro("x"));
    }

    @Test
    void unaLocalTapaAlParametroSoloSiNoExiste() throws Exception {
        Contexto contexto = contextoDe("instancia", 2);
        contexto.abrirBloque();
        ExcepcionSemantica error = assertThrows(ExcepcionSemantica.class,
                () -> contexto.declararLocal(nombre("p"), TipoPrimitivo.CHAR));
        assertEquals("p", error.getLexemaOfensivo());
        assertEquals(7, error.getLinea());
    }

    @Test
    void unaLocalEsVisibleEnLosBloquesAnidadosYDesapareceAlCerrarElSuyo() throws Exception {
        Contexto contexto = contextoDe("instancia", 2);
        contexto.abrirBloque();
        contexto.declararLocal(nombre("y"), TipoPrimitivo.CHAR);
        contexto.abrirBloque();
        assertEquals(Optional.of(TipoPrimitivo.CHAR), contexto.localOParametro("y"));
        assertThrows(ExcepcionSemantica.class, () -> contexto.declararLocal(nombre("y"), TipoPrimitivo.INT));
        contexto.cerrarBloque();
        contexto.cerrarBloque();
        contexto.abrirBloque();
        assertEquals(Optional.empty(), contexto.localOParametro("y"));
        contexto.declararLocal(nombre("y"), TipoPrimitivo.INT);
        assertEquals(Optional.of(TipoPrimitivo.INT), contexto.localOParametro("y"));
    }

    @Test
    void unaLocalDeclaradaSeEncuentraPorSuNombre() throws Exception {
        Contexto contexto = contextoDe("instancia", 2);
        contexto.abrirBloque();
        contexto.declararLocal(nombre("z"), TipoPrimitivo.BOOLEAN);
        assertEquals(Optional.of(TipoPrimitivo.BOOLEAN), contexto.localOParametro("z"));
    }

    @Test
    void sabeSiLaUnidadEsEstaticaOEsUnConstructor() throws Exception {
        assertTrue(contextoDe("estatico", 0).esEstatico());
        assertFalse(contextoDe("instancia", 2).esEstatico());
        assertFalse(contextoDe("instancia", 2).esConstructor());
        assertTrue(contextoDe(null, 0).esConstructor());
        assertFalse(contextoDe(null, 0).esEstatico());
    }

    @Test
    void thisTieneElTipoDeLaClaseConSuParametro() throws Exception {
        assertEquals("Caja<T>", contextoDe("instancia", 2).tipoDeThis().nombre());
        assertEquals("Caja<T@Caja>", contextoDe("instancia", 2).tipoDeThis().identidad());
    }

    @Test
    void elRetornoEsElDelMetodo() throws Exception {
        assertEquals("void", contextoDe("instancia", 2).retorno().nombre());
    }
}
