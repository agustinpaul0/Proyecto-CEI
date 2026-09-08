package minijava.sintactico.partes;

import minijava.lexico.errores.ExcepcionLexica;
import minijava.lexico.tokens.TipoDeToken;
import minijava.sintactico.Sintaxis;
import minijava.sintactico.errores.ExcepcionSintactica;
import minijava.sintactico.primeros.Primeros;

// El logro Fors! E2: una sola sentencia o expresión por separador. El `:`
// distingue el for de iteradores del estándar y aparece justo después del
// declarador, así que el desempate es de un solo token.
public final class AnalizadorDeFors extends ParteDelAnalizador {

    public AnalizadorDeFors(Sintaxis sintaxis) {
        super(sintaxis);
    }

    // For -> for ( EncabezadoFor ) Sentencia
    public void sentenciaFor() throws ExcepcionLexica, ExcepcionSintactica {
        entrada().match(TipoDeToken.PR_FOR);
        entrada().match(TipoDeToken.PAREN_A);
        encabezadoFor();
        entrada().match(TipoDeToken.PAREN_C);
        sintaxis.sentencias().sentencia();
    }

    // EncabezadoFor -> ; ExpresionOpcional ; ExpresionOpcional
    //                | var idMetVar EncabezadoForTrasVar
    //                | TipoPrimitivo DimensionesOpcionales idMetVar EncabezadoForTrasDeclarador
    //                | idGen DimensionesOpcionales idMetVar EncabezadoForTrasDeclarador
    //                | idClase EncabezadoForDesdeIdClase
    //                | ExpresionSinIdClase ClausulasFinalesFor
    private void encabezadoFor() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(Primeros.ENCABEZADO_FOR_PUNTO_Y_COMA)) {
            clausulasFinalesFor();
        } else if (estaEn(Primeros.ENCABEZADO_FOR_VAR)) {
            entrada().match(TipoDeToken.PR_VAR);
            entrada().match(TipoDeToken.ID_MV);
            encabezadoForTrasVar();
        } else if (estaEn(Primeros.ENCABEZADO_FOR_TIPO_PRIMITIVO)) {
            sintaxis.tipos().tipoPrimitivo();
            declaradorDelFor();
        } else if (estaEn(Primeros.ENCABEZADO_FOR_ID_GEN)) {
            entrada().match(TipoDeToken.ID_GEN);
            declaradorDelFor();
        } else if (estaEn(Primeros.ENCABEZADO_FOR_ID_CLASE)) {
            entrada().match(TipoDeToken.ID_CLASE);
            encabezadoForDesdeIdClase();
        } else {
            sintaxis.expresiones().expresionSinIdClase();
            clausulasFinalesFor();
        }
    }

    private void declaradorDelFor() throws ExcepcionLexica, ExcepcionSintactica {
        sintaxis.tipos().dimensionesOpcionales();
        entrada().match(TipoDeToken.ID_MV);
        encabezadoForTrasDeclarador();
    }

    // EncabezadoForDesdeIdClase -> TipoGenericoOpcional DimensionesOpcionales idMetVar
    //                              EncabezadoForTrasDeclarador
    // EncabezadoForDesdeIdClase -> . idMetVar ArgsActuales ColaDeExpresionDesdePrimario
    //                              ClausulasFinalesFor
    private void encabezadoForDesdeIdClase() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(Primeros.ENCABEZADO_FOR_DESDE_ID_CLASE_PUNTO)) {
            entrada().match(TipoDeToken.PUNTO);
            entrada().match(TipoDeToken.ID_MV);
            sintaxis.creacion().argsActuales();
            sintaxis.expresiones().colaDeExpresionDesdePrimario();
            clausulasFinalesFor();
        } else {
            sintaxis.tipos().tipoGenericoOpcional();
            declaradorDelFor();
        }
    }

    // EncabezadoForTrasVar -> : Expresion
    // EncabezadoForTrasVar -> = ExpresionCondicional ClausulasFinalesFor
    private void encabezadoForTrasVar() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(Primeros.ENCABEZADO_FOR_TRAS_VAR_DOS_PUNTOS)) {
            recorridoDeIterador();
        } else if (estaEn(Primeros.ENCABEZADO_FOR_TRAS_VAR_ASIGNACION)) {
            entrada().match(TipoDeToken.OP_ASIGNACION);
            sintaxis.expresiones().expresionCondicional();
            clausulasFinalesFor();
        } else {
            throw entrada().errorEsperando(Primeros.ENCABEZADO_FOR_TRAS_VAR_DOS_PUNTOS,
                    Primeros.ENCABEZADO_FOR_TRAS_VAR_ASIGNACION);
        }
    }

    // EncabezadoForTrasDeclarador -> : Expresion
    // EncabezadoForTrasDeclarador -> InicializacionDeDeclaradorFor ClausulasFinalesFor
    private void encabezadoForTrasDeclarador() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(Primeros.ENCABEZADO_FOR_TRAS_DECLARADOR_DOS_PUNTOS)) {
            recorridoDeIterador();
            return;
        }
        sintaxis.sentencias().inicializacionDeDeclarador();
        clausulasFinalesFor();
    }

    private void recorridoDeIterador() throws ExcepcionLexica, ExcepcionSintactica {
        entrada().match(TipoDeToken.DOS_PUNTOS);
        sintaxis.expresiones().expresion();
    }

    // ClausulasFinalesFor -> ; ExpresionOpcional ; ExpresionOpcional
    private void clausulasFinalesFor() throws ExcepcionLexica, ExcepcionSintactica {
        entrada().match(TipoDeToken.PUNTO_Y_COMA);
        sintaxis.sentencias().expresionOpcional();
        entrada().match(TipoDeToken.PUNTO_Y_COMA);
        sintaxis.sentencias().expresionOpcional();
    }
}
