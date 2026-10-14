package minijava.sentencias;

import minijava.lexico.tokens.TipoDeToken;
import minijava.lexico.tokens.Token;
import minijava.semantico.ast.expresiones.NodoAccesoArreglo;
import minijava.semantico.ast.expresiones.NodoAccesoAtributo;
import minijava.semantico.ast.expresiones.NodoAsignacion;
import minijava.semantico.ast.expresiones.NodoBinario;
import minijava.semantico.ast.expresiones.NodoCreacionArreglo;
import minijava.semantico.ast.expresiones.NodoCreacionObjeto;
import minijava.semantico.ast.expresiones.NodoExpresion;
import minijava.semantico.ast.expresiones.NodoLiteral;
import minijava.semantico.ast.expresiones.NodoLlamada;
import minijava.semantico.ast.expresiones.NodoLlamadaConReceptor;
import minijava.semantico.ast.expresiones.NodoLlamadaEstatica;
import minijava.semantico.ast.expresiones.NodoParentizada;
import minijava.semantico.ast.expresiones.NodoThis;
import minijava.semantico.ast.expresiones.NodoUnario;
import minijava.semantico.ast.expresiones.NodoVariable;
import minijava.semantico.tipos.TipoPrimitivo;
import minijava.semantico.tipos.TipoReferencia;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

// La información estructural de cada nodo (Clase 11, slides 24-32): si es un
// destino asignable, una llamada o una asignación, y el token de sus errores.
// No depende de los tipos, así que se mira sin chequear nada.
@Tag("sentencias")
class EstructuraDeLosNodosTest {

    private static Token token(TipoDeToken tipo, String lexema) {
        return new Token(tipo, lexema, 3);
    }

    private static final Token X = token(TipoDeToken.ID_MV, "x");
    private static final Token PUNTO = token(TipoDeToken.PUNTO, ".");
    private static final Token PARENTESIS = token(TipoDeToken.PAREN_A, "(");
    private static final Token NUEVO = token(TipoDeToken.PR_NEW, "new");

    private static NodoExpresion variable() {
        return new NodoVariable(X);
    }

    private static NodoExpresion parentizada(NodoExpresion interna) {
        return new NodoParentizada(PARENTESIS, interna);
    }

    @Test
    void sonAsignablesLasVariablesLosAtributosYLosComponentes() {
        assertTrue(variable().esAsignable());
        assertTrue(new NodoAccesoAtributo(variable(), PUNTO, X).esAsignable());
        assertTrue(new NodoAccesoArreglo(variable(), token(TipoDeToken.CORCHETE_A, "["), variable()).esAsignable());
        assertTrue(new NodoAccesoAtributo(new NodoLlamada(X, PARENTESIS, List.of()), PUNTO, X).esAsignable());
    }

    @Test
    void noSonAsignablesLosDemas() {
        Token uno = token(TipoDeToken.INT_LITERAL, "1");
        List<NodoExpresion> noAsignables = List.of(
                new NodoLiteral(uno),
                new NodoThis(token(TipoDeToken.PR_THIS, "this")),
                parentizada(variable()),
                new NodoLlamada(X, PARENTESIS, List.of()),
                new NodoLlamadaConReceptor(variable(), PUNTO, X, PARENTESIS, List.of()),
                new NodoLlamadaEstatica(token(TipoDeToken.ID_CLASE, "Aa"), PUNTO, X, PARENTESIS, List.of()),
                new NodoCreacionObjeto(NUEVO, new TipoReferencia(token(TipoDeToken.ID_CLASE, "Aa")), List.of()),
                new NodoCreacionArreglo(NUEVO, TipoPrimitivo.INT, List.of(new NodoLiteral(uno))),
                new NodoBinario(variable(), token(TipoDeToken.OP_SUMA, "+"), variable()),
                new NodoUnario(token(TipoDeToken.OP_RESTA, "-"), variable()),
                new NodoAsignacion(variable(), token(TipoDeToken.OP_ASIGNACION, "="), variable()),
                new NodoAccesoAtributo(parentizada(variable()), PUNTO, X),
                new NodoAccesoArreglo(parentizada(variable()), token(TipoDeToken.CORCHETE_A, "["), variable()));
        for (NodoExpresion nodo : noAsignables) {
            assertFalse(nodo.esAsignable(), nodo.toString());
        }
    }

    @Test
    void sonLlamadasLasInvocacionesYLaCreacionDeObjetos() {
        assertTrue(new NodoLlamada(X, PARENTESIS, List.of()).esLlamada());
        assertTrue(new NodoLlamadaConReceptor(variable(), PUNTO, X, PARENTESIS, List.of()).esLlamada());
        assertTrue(new NodoLlamadaEstatica(token(TipoDeToken.ID_CLASE, "Aa"), PUNTO, X, PARENTESIS, List.of()).esLlamada());
        assertTrue(new NodoCreacionObjeto(NUEVO, new TipoReferencia(token(TipoDeToken.ID_CLASE, "Aa")), List.of()).esLlamada());
    }

    // Documento §11.4 y §12.2: ni `(m());` ni `(o).m();` son sentencias de llamada.
    @Test
    void losParentesisQuitanLaFormaDeLlamadaYDeAsignacion() {
        assertFalse(parentizada(new NodoLlamada(X, PARENTESIS, List.of())).esLlamada());
        assertFalse(new NodoLlamadaConReceptor(parentizada(variable()), PUNTO, X, PARENTESIS, List.of()).esLlamada());
        assertFalse(new NodoCreacionArreglo(NUEVO, TipoPrimitivo.INT, List.of()).esLlamada());
        assertFalse(variable().esLlamada());
        assertTrue(new NodoAsignacion(variable(), token(TipoDeToken.OP_ASIGNACION, "="), variable()).esAsignacion());
        assertFalse(parentizada(new NodoAsignacion(variable(), token(TipoDeToken.OP_ASIGNACION, "="), variable()))
                .esAsignacion());
    }

    @Test
    void laMarcaDeParentesisSePropagaPorElEncadenado() {
        NodoExpresion base = parentizada(variable());
        NodoExpresion encadenado = new NodoAccesoArreglo(new NodoAccesoAtributo(base, PUNTO, X),
                token(TipoDeToken.CORCHETE_A, "["), variable());
        assertTrue(encadenado.empiezaConParentesis());
        assertFalse(new NodoAccesoAtributo(variable(), PUNTO, X).empiezaConParentesis());
    }

    // El token de la expresión entera es el que javac marca.
    @Test
    void elTokenDeCadaNodoEsElDeJavac() {
        Token mas = token(TipoDeToken.OP_SUMA, "+");
        assertSame(mas, new NodoBinario(variable(), mas, variable()).token());
        assertSame(PARENTESIS, new NodoLlamada(X, PARENTESIS, List.of()).token());
        assertSame(PARENTESIS, new NodoLlamadaConReceptor(variable(), PUNTO, X, PARENTESIS, List.of()).token());
        assertSame(PUNTO, new NodoAccesoAtributo(variable(), PUNTO, X).token());
        assertSame(NUEVO, new NodoCreacionArreglo(NUEVO, TipoPrimitivo.INT, List.of()).token());
        assertSame(X, parentizada(variable()).token());
        assertEquals("(", ((NodoParentizada) parentizada(variable())).parentesis().lexema());
    }
}
