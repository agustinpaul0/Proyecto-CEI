package minijava.sintactico.partes;

import minijava.lexico.errores.ExcepcionLexica;
import minijava.lexico.tokens.TipoDeToken;
import minijava.sintactico.Sintaxis;
import minijava.sintactico.errores.ExcepcionSintactica;
import minijava.sintactico.primeros.PrimerosDeReferencias;

// Referencias, encadenados y primarios: de dónde sale un valor antes de que los
// operadores lo combinen.
public final class AnalizadorDeReferencias extends ParteDelAnalizador {

    public AnalizadorDeReferencias(Sintaxis sintaxis) {
        super(sintaxis);
    }

    // Referencia -> Primario RestoReferencia
    public void referencia() throws ExcepcionLexica, ExcepcionSintactica {
        primario();
        restoReferencia();
    }

    // RestoReferencia -> Encadenado RestoReferencia
    //                  | AccesoArreglo RestoReferencia
    //                  | EPSILON
    public void restoReferencia() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeReferencias.RESTO_REFERENCIA_ENCADENADO)) {
            encadenado();
            restoReferencia();
        } else if (estaEn(PrimerosDeReferencias.RESTO_REFERENCIA_ACCESO_ARREGLO)) {
            accesoArreglo();
            restoReferencia();
        } else {
            // RestoReferencia -> EPSILON
        }
    }

    // Primario -> LlamadaMetodoEstatico | PrimarioSinIdClase
    // LlamadaMetodoEstatico -> idClase . idMetVar ArgsActuales
    private void primario() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeReferencias.PRIMARIO_LLAMADA_METODO_ESTATICO)) {
            match(TipoDeToken.ID_CLASE);
            match(TipoDeToken.PUNTO);
            match(TipoDeToken.ID_MV);
            sintaxis.argumentos().argsActuales();
        } else {
            primarioSinIdClase();
        }
    }

    // PrimarioSinIdClase -> this | stringLiteral | ExpresionParentizada
    //                     | AccesoVarOLlamadaMetodo | Creacion
    public void primarioSinIdClase() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeReferencias.PRIMARIO_SIN_ID_CLASE_THIS)) {
            match(TipoDeToken.PR_THIS);
        } else if (estaEn(PrimerosDeReferencias.PRIMARIO_SIN_ID_CLASE_STRING_LITERAL)) {
            match(TipoDeToken.LIT_STRING);
        } else if (estaEn(PrimerosDeReferencias.PRIMARIO_SIN_ID_CLASE_EXPRESION_PARENTIZADA)) {
            expresionParentizada();
        } else if (estaEn(PrimerosDeReferencias.PRIMARIO_SIN_ID_CLASE_ACCESO_VAR_OLLAMADA_METODO)) {
            accesoVarOLlamadaMetodo();
        } else if (estaEn(PrimerosDeReferencias.PRIMARIO_SIN_ID_CLASE_CREACION)) {
            sintaxis.creacion().creacion();
        } else {
            throw errorEsperando(PrimerosDeReferencias.PRIMARIO_SIN_ID_CLASE_THIS,
                    PrimerosDeReferencias.PRIMARIO_SIN_ID_CLASE_STRING_LITERAL,
                    PrimerosDeReferencias.PRIMARIO_SIN_ID_CLASE_EXPRESION_PARENTIZADA,
                    PrimerosDeReferencias.PRIMARIO_SIN_ID_CLASE_ACCESO_VAR_OLLAMADA_METODO,
                    PrimerosDeReferencias.PRIMARIO_SIN_ID_CLASE_CREACION);
        }
    }

    // ExpresionParentizada -> ( Expresion )
    private void expresionParentizada() throws ExcepcionLexica, ExcepcionSintactica {
        match(TipoDeToken.PAREN_A);
        sintaxis.expresiones().expresion();
        match(TipoDeToken.PAREN_C);
    }

    // AccesoVarOLlamadaMetodo -> idMetVar AccesoOLlamada
    private void accesoVarOLlamadaMetodo() throws ExcepcionLexica, ExcepcionSintactica {
        match(TipoDeToken.ID_MV);
        accesoOLlamada();
    }

    // AccesoOLlamada -> ArgsActuales | EPSILON
    //
    // El no terminal dedicado distingue los dos usos de un idMetVar: con
    // argumentos es una llamada, sin ellos un acceso a variable. Cuál de los dos
    // nombra algo declarado, y si ese algo es alcanzable desde donde se lo usa,
    // lo resuelve el analizador semántico.
    private void accesoOLlamada() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeReferencias.ACCESO_OLLAMADA_ARGS_ACTUALES)) {
            sintaxis.argumentos().argsActuales();
        } else {
            // AccesoOLlamada -> EPSILON
        }
    }

    // Encadenado -> . idMetVar EncadenadoAtributoOMetodo
    // EncadenadoAtributoOMetodo -> ArgsActuales | EPSILON
    private void encadenado() throws ExcepcionLexica, ExcepcionSintactica {
        match(TipoDeToken.PUNTO);
        match(TipoDeToken.ID_MV);
        if (estaEn(PrimerosDeReferencias.ENCADENADO_ATRIBUTO_OMETODO_ARGS_ACTUALES)) {
            sintaxis.argumentos().argsActuales();
        } else {
            // EncadenadoAtributoOMetodo -> EPSILON
        }
    }

    // AccesoArreglo -> [ Expresion ]
    private void accesoArreglo() throws ExcepcionLexica, ExcepcionSintactica {
        match(TipoDeToken.CORCHETE_A);
        sintaxis.expresiones().expresion();
        match(TipoDeToken.CORCHETE_C);
    }
}
