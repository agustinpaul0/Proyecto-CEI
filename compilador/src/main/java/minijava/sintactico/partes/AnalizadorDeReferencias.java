package minijava.sintactico.partes;

import minijava.lexico.errores.ExcepcionLexica;
import minijava.lexico.tokens.TipoDeToken;
import minijava.lexico.tokens.Token;
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

    // PrimarioSinIdClase -> this | stringLiteral | Parentizada
    //                     | AccesoVarOLlamada | Creacion
    //
    // Ningún Primario puede ser una lambda: un Primario es un operando, y en Java
    // una lambda nunca lo es. Ésa es exactamente la corrección de precedencia.
    public void primarioSinIdClase() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeReferencias.PRIMARIO_SIN_ID_CLASE_THIS)) {
            match(TipoDeToken.PR_THIS);
        } else if (estaEn(PrimerosDeReferencias.PRIMARIO_SIN_ID_CLASE_STRING_LITERAL)) {
            match(TipoDeToken.LIT_STRING);
        } else if (estaEn(PrimerosDeReferencias.PRIMARIO_SIN_ID_CLASE_PARENTIZADA)) {
            parentizada();
        } else if (estaEn(PrimerosDeReferencias.PRIMARIO_SIN_ID_CLASE_ACCESO_VAR_OLLAMADA)) {
            accesoVarOLlamada();
        } else if (estaEn(PrimerosDeReferencias.PRIMARIO_SIN_ID_CLASE_CREACION)) {
            sintaxis.creacion().creacion();
        } else {
            throw errorEsperando(PrimerosDeReferencias.PRIMARIO_SIN_ID_CLASE_THIS,
                    PrimerosDeReferencias.PRIMARIO_SIN_ID_CLASE_STRING_LITERAL,
                    PrimerosDeReferencias.PRIMARIO_SIN_ID_CLASE_PARENTIZADA,
                    PrimerosDeReferencias.PRIMARIO_SIN_ID_CLASE_ACCESO_VAR_OLLAMADA,
                    PrimerosDeReferencias.PRIMARIO_SIN_ID_CLASE_CREACION);
        }
    }

    // Parentizada -> ( ExpresionSinLambda )
    //
    // El contenido no puede ser una lambda: `(x -> x) + 1` y `(x -> x).m()` son
    // «lambda expression not expected here» en Java. La lambda parentizada que sí
    // es válida —cuando ella misma es toda la expresión— la cubre
    // ParentizadaOLambda, en el nivel más alto.
    private void parentizada() throws ExcepcionLexica, ExcepcionSintactica {
        match(TipoDeToken.PAREN_A);
        sintaxis.expresiones().expresionSinLambda();
        match(TipoDeToken.PAREN_C);
    }

    // AccesoVarOLlamada -> idMetVar AccesoLlamada
    private void accesoVarOLlamada() throws ExcepcionLexica, ExcepcionSintactica {
        Token nombre = actual();
        match(TipoDeToken.ID_MV);
        accesoLlamada(nombre);
    }

    // AccesoLlamada -> ArgsActuales | EPSILON
    //
    // El no terminal dedicado distingue los dos usos de un idMetVar: con
    // argumentos es una llamada, sin ellos un acceso a variable. Cuál de los dos
    // nombra algo declarado, y si ese algo es alcanzable desde donde se lo usa,
    // lo resuelve el analizador semántico.
    public void accesoLlamada(Token nombre) throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeReferencias.ACCESO_LLAMADA_ARGS_ACTUALES)) {
            sintaxis.argumentos().argsActuales();
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
