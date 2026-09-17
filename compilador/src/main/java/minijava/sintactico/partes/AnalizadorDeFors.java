package minijava.sintactico.partes;

import minijava.lexico.errores.ExcepcionLexica;
import minijava.lexico.tokens.TipoDeToken;
import minijava.sintactico.Sintaxis;
import minijava.sintactico.errores.ExcepcionSintactica;
import minijava.sintactico.primeros.PrimerosDeFors;

// El logro Fors! E2: una sola sentencia o expresión por separador. El `:`
// distingue el for de iteradores del estándar y aparece justo después del
// declarador, así que el desempate es de un solo token.
public final class AnalizadorDeFors extends ParteDelAnalizador {

    public AnalizadorDeFors(Sintaxis sintaxis) {
        super(sintaxis);
    }

    // For -> for ( EncabezadoFor ) Sentencia
    public void sentenciaFor() throws ExcepcionLexica, ExcepcionSintactica {
        match(TipoDeToken.PR_FOR);
        match(TipoDeToken.PAREN_A);
        encabezadoFor();
        match(TipoDeToken.PAREN_C);
        sintaxis.sentencias().sentencia();
    }

    // EncabezadoFor -> ; ExpresionOpcional ; ExpresionOpcional
    //                | var idMetVar EncabezadoForTrasVar
    //                | TipoPrimitivo DimensionesOpcionales idMetVar EncabezadoForTrasDeclarador
    //                | idGen DimensionesOpcionales idMetVar EncabezadoForTrasDeclarador
    //                | idClase EncabezadoForDesdeIdClase
    //                | ExpresionSinIdClase ClausulasFinalesFor
    private void encabezadoFor() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeFors.ENCABEZADO_FOR_PUNTO_Y_COMA)) {
            clausulasFinalesFor();
        } else if (estaEn(PrimerosDeFors.ENCABEZADO_FOR_VAR)) {
            match(TipoDeToken.PR_VAR);
            match(TipoDeToken.ID_MV);
            encabezadoForTrasVar();
        } else if (estaEn(PrimerosDeFors.ENCABEZADO_FOR_TIPO_PRIMITIVO)) {
            sintaxis.tipos().tipoPrimitivo();
            declaradorDelFor();
        } else if (estaEn(PrimerosDeFors.ENCABEZADO_FOR_ID_GEN)) {
            match(TipoDeToken.ID_GEN);
            declaradorDelFor();
        } else if (estaEn(PrimerosDeFors.ENCABEZADO_FOR_ID_CLASE)) {
            match(TipoDeToken.ID_CLASE);
            encabezadoForDesdeIdClase();
        } else {
            sintaxis.expresiones().expresionSinIdClase();
            clausulasFinalesFor();
        }
    }

    private void declaradorDelFor() throws ExcepcionLexica, ExcepcionSintactica {
        sintaxis.tipos().dimensionesOpcionales();
        match(TipoDeToken.ID_MV);
        encabezadoForTrasDeclarador();
    }

    // EncabezadoForDesdeIdClase -> TipoGenericoOpcional DimensionesOpcionales idMetVar
    //                              EncabezadoForTrasDeclarador
    // EncabezadoForDesdeIdClase -> . idMetVar ArgsActuales ColaDeExpresionDesdePrimario
    //                              ClausulasFinalesFor
    private void encabezadoForDesdeIdClase() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeFors.ENCABEZADO_FOR_DESDE_ID_CLASE_PUNTO)) {
            match(TipoDeToken.PUNTO);
            match(TipoDeToken.ID_MV);
            sintaxis.argumentos().argsActuales();
            sintaxis.expresiones().colaDeExpresionDesdePrimario();
            clausulasFinalesFor();
        } else {
            sintaxis.tipos().tipoGenericoOpcional();
            declaradorDelFor();
        }
    }

    // EncabezadoForTrasVar -> : Expresion
    // EncabezadoForTrasVar -> = ExpresionCondicionalOLambda ClausulasFinalesFor
    private void encabezadoForTrasVar() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeFors.ENCABEZADO_FOR_TRAS_VAR_DOS_PUNTOS)) {
            recorridoDeIterador();
        } else if (estaEn(PrimerosDeFors.ENCABEZADO_FOR_TRAS_VAR_ASIGNACION)) {
            match(TipoDeToken.OP_ASIGNACION);
            sintaxis.expresiones().expresionCondicionalOLambda();
            clausulasFinalesFor();
        } else {
            throw errorEsperando(PrimerosDeFors.ENCABEZADO_FOR_TRAS_VAR_DOS_PUNTOS,
                    PrimerosDeFors.ENCABEZADO_FOR_TRAS_VAR_ASIGNACION);
        }
    }

    // EncabezadoForTrasDeclarador -> : Expresion
    // EncabezadoForTrasDeclarador -> InicializacionDeDeclaradorFor ClausulasFinalesFor
    private void encabezadoForTrasDeclarador() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeFors.ENCABEZADO_FOR_TRAS_DECLARADOR_DOS_PUNTOS)) {
            recorridoDeIterador();
            return;
        }
        inicializacionDeDeclaradorFor();
        clausulasFinalesFor();
    }

    // InicializacionDeDeclaradorFor -> = ExpresionCondicionalOLambda | EPSILON
    //
    // No es <InicializacionDeDeclarador>: el encabezado del for declara su propia
    // variable, y tener el suyo deja esa decisión separada para el semántico.
    private void inicializacionDeDeclaradorFor() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeFors.INICIALIZACION_DE_DECLARADOR_FOR_ASIGNACION)) {
            match(TipoDeToken.OP_ASIGNACION);
            sintaxis.expresiones().expresionCondicionalOLambda();
        } else {
            // InicializacionDeDeclaradorFor -> EPSILON
        }
    }

    private void recorridoDeIterador() throws ExcepcionLexica, ExcepcionSintactica {
        match(TipoDeToken.DOS_PUNTOS);
        sintaxis.expresiones().expresion();
    }

    // ClausulasFinalesFor -> ; ExpresionOpcional ; ExpresionOpcional
    private void clausulasFinalesFor() throws ExcepcionLexica, ExcepcionSintactica {
        match(TipoDeToken.PUNTO_Y_COMA);
        sintaxis.sentencias().expresionOpcional();
        match(TipoDeToken.PUNTO_Y_COMA);
        sintaxis.sentencias().expresionOpcional();
    }
}
