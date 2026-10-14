package minijava.sintactico.partes;

import minijava.lexico.errores.ExcepcionLexica;
import minijava.lexico.tokens.TipoDeToken;
import minijava.lexico.tokens.Token;
import minijava.semantico.ast.expresiones.NodoAccesoArreglo;
import minijava.semantico.ast.expresiones.NodoAccesoAtributo;
import minijava.semantico.ast.expresiones.NodoExpresion;
import minijava.semantico.ast.expresiones.NodoLiteral;
import minijava.semantico.ast.expresiones.NodoLlamada;
import minijava.semantico.ast.expresiones.NodoLlamadaConReceptor;
import minijava.semantico.ast.expresiones.NodoLlamadaEstatica;
import minijava.semantico.ast.expresiones.NodoParentizada;
import minijava.semantico.ast.expresiones.NodoThis;
import minijava.semantico.ast.expresiones.NodoVariable;
import minijava.sintactico.Sintaxis;
import minijava.sintactico.errores.ExcepcionSintactica;
import minijava.sintactico.primeros.PrimerosDeReferencias;

import java.util.List;

// Referencias, encadenados y primarios: de dónde sale un valor antes de que los
// operadores lo combinen.
//
// Un encadenado se arma sobre su receptor: `RestoReferencia` recibe el nodo
// armado hasta acá y lo envuelve con cada `.x`, `.m(...)` o `[i]` que sigue, así
// que `a.b[0]` queda como el acceso a `[0]` sobre el acceso a `b` sobre `a`.
public final class AnalizadorDeReferencias extends ParteDelAnalizador {

    public AnalizadorDeReferencias(Sintaxis sintaxis) {
        super(sintaxis);
    }

    // Referencia -> Primario RestoReferencia
    public NodoExpresion referencia() throws ExcepcionLexica, ExcepcionSintactica {
        return restoReferencia(primario());
    }

    // RestoReferencia -> Encadenado RestoReferencia
    //                  | AccesoArreglo RestoReferencia
    //                  | EPSILON
    public NodoExpresion restoReferencia(NodoExpresion receptor) throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeReferencias.RESTO_REFERENCIA_ENCADENADO)) {
            return restoReferencia(encadenado(receptor));
        } else if (estaEn(PrimerosDeReferencias.RESTO_REFERENCIA_ACCESO_ARREGLO)) {
            return restoReferencia(accesoArreglo(receptor));
        } else {
            // RestoReferencia -> EPSILON
            return receptor;
        }
    }

    // Primario -> LlamadaMetodoEstatico | PrimarioSinIdClase
    private NodoExpresion primario() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeReferencias.PRIMARIO_LLAMADA_METODO_ESTATICO)) {
            return llamadaMetodoEstatico();
        } else if (estaEn(PrimerosDeReferencias.PRIMARIO_PRIMARIO_SIN_ID_CLASE)) {
            return primarioSinIdClase();
        } else {
            throw errorEsperando(PrimerosDeReferencias.PRIMARIO_LLAMADA_METODO_ESTATICO,
                    PrimerosDeReferencias.PRIMARIO_PRIMARIO_SIN_ID_CLASE);
        }
    }

    // LlamadaMetodoEstatico -> idClase . idMetVar ArgsActuales
    private NodoLlamadaEstatica llamadaMetodoEstatico() throws ExcepcionLexica, ExcepcionSintactica {
        Token clase = actual();
        match(TipoDeToken.ID_CLASE);
        Token punto = actual();
        match(TipoDeToken.PUNTO);
        Token nombre = actual();
        match(TipoDeToken.ID_MV);
        Token parentesis = actual();
        return new NodoLlamadaEstatica(clase, punto, nombre, parentesis, sintaxis.argumentos().argsActuales());
    }

    // PrimarioSinIdClase -> this | stringLiteral | ExpresionParentizada
    //                     | AccesoVarOLlamadaMetodo | Creacion
    public NodoExpresion primarioSinIdClase() throws ExcepcionLexica, ExcepcionSintactica {
        Token inicio = actual();
        if (estaEn(PrimerosDeReferencias.PRIMARIO_SIN_ID_CLASE_THIS)) {
            match(TipoDeToken.PR_THIS);
            return new NodoThis(inicio);
        } else if (estaEn(PrimerosDeReferencias.PRIMARIO_SIN_ID_CLASE_STRING_LITERAL)) {
            match(TipoDeToken.LIT_STRING);
            return new NodoLiteral(inicio);
        } else if (estaEn(PrimerosDeReferencias.PRIMARIO_SIN_ID_CLASE_EXPRESION_PARENTIZADA)) {
            return expresionParentizada();
        } else if (estaEn(PrimerosDeReferencias.PRIMARIO_SIN_ID_CLASE_ACCESO_VAR_OLLAMADA_METODO)) {
            return accesoVarOLlamadaMetodo();
        } else if (estaEn(PrimerosDeReferencias.PRIMARIO_SIN_ID_CLASE_CREACION)) {
            return sintaxis.creacion().creacion();
        } else {
            throw errorEsperando(PrimerosDeReferencias.PRIMARIO_SIN_ID_CLASE_THIS,
                    PrimerosDeReferencias.PRIMARIO_SIN_ID_CLASE_STRING_LITERAL,
                    PrimerosDeReferencias.PRIMARIO_SIN_ID_CLASE_EXPRESION_PARENTIZADA,
                    PrimerosDeReferencias.PRIMARIO_SIN_ID_CLASE_ACCESO_VAR_OLLAMADA_METODO,
                    PrimerosDeReferencias.PRIMARIO_SIN_ID_CLASE_CREACION);
        }
    }

    // ExpresionParentizada -> ( Expresion )
    private NodoParentizada expresionParentizada() throws ExcepcionLexica, ExcepcionSintactica {
        Token parentesis = actual();
        match(TipoDeToken.PAREN_A);
        NodoExpresion interna = sintaxis.expresiones().expresion();
        match(TipoDeToken.PAREN_C);
        return new NodoParentizada(parentesis, interna);
    }

    // AccesoVarOLlamadaMetodo -> idMetVar AccesoOLlamada
    private NodoExpresion accesoVarOLlamadaMetodo() throws ExcepcionLexica, ExcepcionSintactica {
        Token nombre = actual();
        match(TipoDeToken.ID_MV);
        return accesoOLlamada(nombre);
    }

    // AccesoOLlamada -> ArgsActuales | EPSILON
    //
    // El no terminal dedicado distingue los dos usos de un idMetVar: con
    // argumentos es una llamada, sin ellos un acceso a variable. Cuál de los dos
    // nombra algo declarado, y si ese algo es alcanzable desde donde se lo usa,
    // lo resuelve el analizador semántico.
    private NodoExpresion accesoOLlamada(Token nombre) throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeReferencias.ACCESO_OLLAMADA_ARGS_ACTUALES)) {
            Token parentesis = actual();
            return new NodoLlamada(nombre, parentesis, sintaxis.argumentos().argsActuales());
        } else {
            // AccesoOLlamada -> EPSILON
            return new NodoVariable(nombre);
        }
    }

    // Encadenado -> . idMetVar EncadenadoAtributoOMetodo
    private NodoExpresion encadenado(NodoExpresion receptor) throws ExcepcionLexica, ExcepcionSintactica {
        Token punto = actual();
        match(TipoDeToken.PUNTO);
        Token nombre = actual();
        match(TipoDeToken.ID_MV);
        return encadenadoAtributoOMetodo(receptor, punto, nombre);
    }

    // EncadenadoAtributoOMetodo -> ArgsActuales | EPSILON
    //
    // Con argumentos es una llamada encadenada; sin ellos, el acceso a un
    // atributo de otro objeto.
    private NodoExpresion encadenadoAtributoOMetodo(NodoExpresion receptor, Token punto, Token nombre)
            throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeReferencias.ENCADENADO_ATRIBUTO_OMETODO_ARGS_ACTUALES)) {
            Token parentesis = actual();
            List<NodoExpresion> argumentos = sintaxis.argumentos().argsActuales();
            return new NodoLlamadaConReceptor(receptor, punto, nombre, parentesis, argumentos);
        } else {
            // EncadenadoAtributoOMetodo -> EPSILON
            return new NodoAccesoAtributo(receptor, punto, nombre);
        }
    }

    // AccesoArreglo -> [ Expresion ]
    private NodoAccesoArreglo accesoArreglo(NodoExpresion receptor) throws ExcepcionLexica, ExcepcionSintactica {
        Token corchete = actual();
        match(TipoDeToken.CORCHETE_A);
        NodoExpresion indice = sintaxis.expresiones().expresion();
        match(TipoDeToken.CORCHETE_C);
        return new NodoAccesoArreglo(receptor, corchete, indice);
    }
}
