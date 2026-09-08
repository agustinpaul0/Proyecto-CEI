package minijava.sintactico.partes;

import minijava.comun.config.Logro;
import minijava.lexico.errores.ExcepcionLexica;
import minijava.lexico.tokens.TipoDeToken;
import minijava.lexico.tokens.Token;
import minijava.sintactico.Sintaxis;
import minijava.sintactico.errores.ExcepcionSintactica;
import minijava.sintactico.primeros.PrimerosDeReferencias;

import java.util.Set;

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
        while (estaEn(PrimerosDeReferencias.RESTO_REFERENCIA_ENCADENADO)
                || estaEn(PrimerosDeReferencias.RESTO_REFERENCIA_ACCESO_ARREGLO)) {
            if (estaEn(PrimerosDeReferencias.RESTO_REFERENCIA_ENCADENADO)) {
                encadenado();
            } else {
                accesoArreglo();
            }
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

    // PrimarioSinIdClase -> this | stringLiteral | ParentizadaOLambda
    //                     | AccesoVarLlamadaOLambda | Creacion
    public void primarioSinIdClase() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeReferencias.PRIMARIO_SIN_ID_CLASE_THIS)) {
            sintaxis.restriccionesDeLambda().verificarThisExplicito(actual());
            match(TipoDeToken.PR_THIS);
        } else if (estaEn(PrimerosDeReferencias.PRIMARIO_SIN_ID_CLASE_STRING_LITERAL)) {
            match(TipoDeToken.LIT_STRING);
        } else if (estaEn(PrimerosDeReferencias.PRIMARIO_SIN_ID_CLASE_PARENTIZADA_OLAMBDA)) {
            sintaxis.lambdas().parentizadaOLambda();
        } else if (estaEn(PrimerosDeReferencias.PRIMARIO_SIN_ID_CLASE_ACCESO_VAR_LLAMADA_OLAMBDA)) {
            accesoVarLlamadaOLambda();
        } else if (estaEn(PrimerosDeReferencias.PRIMARIO_SIN_ID_CLASE_CREACION)) {
            sintaxis.creacion().creacion();
        } else {
            throw errorEsperando(PrimerosDeReferencias.PRIMARIO_SIN_ID_CLASE_THIS,
                    PrimerosDeReferencias.PRIMARIO_SIN_ID_CLASE_STRING_LITERAL,
                    PrimerosDeReferencias.PRIMARIO_SIN_ID_CLASE_PARENTIZADA_OLAMBDA,
                    PrimerosDeReferencias.PRIMARIO_SIN_ID_CLASE_ACCESO_VAR_LLAMADA_OLAMBDA,
                    PrimerosDeReferencias.PRIMARIO_SIN_ID_CLASE_CREACION);
        }
    }

    // AccesoVarLlamadaOLambda -> idMetVar AccesoLlamadaOLambda
    // AccesoLlamadaOLambda -> ArgsActuales
    // AccesoLlamadaOLambda -> -> CuerpoLambda   [+lambda]
    // AccesoLlamadaOLambda -> EPSILON
    //
    // El no terminal dedicado distingue los tres usos de un idMetVar, que es lo
    // que permite chequear la captura de variables sólo donde corresponde.
    //
    // La llamada sin punto NO se chequea: `x -> ayuda(x)` es `this.ayuda(x)` sólo
    // si `ayuda` es un método de instancia, y si es `static` de la misma clase no
    // hay ningún `this` y Java lo acepta. Distinguirlos exige resolver el nombre
    // contra una declaración, que es trabajo del analizador semántico.
    private void accesoVarLlamadaOLambda() throws ExcepcionLexica, ExcepcionSintactica {
        Token nombre = actual();
        match(TipoDeToken.ID_MV);

        if (estaEn(PrimerosDeReferencias.ACCESO_LLAMADA_OLAMBDA_ARGS_ACTUALES)) {
            sintaxis.argumentos().argsActuales();
        } else if (hayLogro(Logro.LAMBDA) && estaEn(PrimerosDeReferencias.ACCESO_LLAMADA_OLAMBDA_FLECHA)) {
            match(TipoDeToken.OP_FLECHA);
            sintaxis.lambdas().cuerpoLambda(Set.of(nombre.lexema()));
        } else {
            sintaxis.restriccionesDeLambda().verificarQueNoCaptura(nombre);
        }
    }

    // Encadenado -> . idMetVar EncadenadoAtributoOMetodo
    // EncadenadoAtributoOMetodo -> ArgsActuales | EPSILON
    private void encadenado() throws ExcepcionLexica, ExcepcionSintactica {
        match(TipoDeToken.PUNTO);
        match(TipoDeToken.ID_MV);
        if (estaEn(PrimerosDeReferencias.ENCADENADO_ATRIBUTO_OMETODO_ARGS_ACTUALES)) {
            sintaxis.argumentos().argsActuales();
        }
    }

    // AccesoArreglo -> [ Expresion ]
    private void accesoArreglo() throws ExcepcionLexica, ExcepcionSintactica {
        match(TipoDeToken.CORCHETE_A);
        sintaxis.expresiones().expresion();
        match(TipoDeToken.CORCHETE_C);
    }
}
