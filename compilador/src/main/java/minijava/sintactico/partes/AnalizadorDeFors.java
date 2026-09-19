package minijava.sintactico.partes;

import minijava.lexico.errores.ExcepcionLexica;
import minijava.lexico.tokens.TipoDeToken;
import minijava.sintactico.Sintaxis;
import minijava.sintactico.errores.ExcepcionSintactica;
import minijava.sintactico.primeros.PrimerosDeFors;

// El logro Fors! E2: una sola sentencia o expresión por separador, obligatoria, y
// la variable de control declarada con `var`. El `:` distingue el for de
// iteradores del estándar y aparece justo después de la variable, así que el
// desempate es de un solo token.
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

    // EncabezadoFor -> var idMetVar EncabezadoForTrasVar
    //                | Expresion ClausulasFinalesFor
    private void encabezadoFor() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeFors.ENCABEZADO_FOR_VAR)) {
            match(TipoDeToken.PR_VAR);
            match(TipoDeToken.ID_MV);
            encabezadoForTrasVar();
        } else if (estaEn(PrimerosDeFors.ENCABEZADO_FOR_EXPRESION)) {
            sintaxis.expresiones().expresion();
            clausulasFinalesFor();
        } else {
            throw errorEsperando(PrimerosDeFors.ENCABEZADO_FOR_VAR,
                    PrimerosDeFors.ENCABEZADO_FOR_EXPRESION);
        }
    }

    // EncabezadoForTrasVar -> : Expresion
    // EncabezadoForTrasVar -> = ExpresionCondicional ClausulasFinalesFor
    private void encabezadoForTrasVar() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeFors.ENCABEZADO_FOR_TRAS_VAR_DOS_PUNTOS)) {
            match(TipoDeToken.DOS_PUNTOS);
            sintaxis.expresiones().expresion();
        } else if (estaEn(PrimerosDeFors.ENCABEZADO_FOR_TRAS_VAR_ASIGNACION)) {
            match(TipoDeToken.OP_ASIGNACION);
            sintaxis.expresiones().expresionCondicional();
            clausulasFinalesFor();
        } else {
            throw errorEsperando(PrimerosDeFors.ENCABEZADO_FOR_TRAS_VAR_DOS_PUNTOS,
                    PrimerosDeFors.ENCABEZADO_FOR_TRAS_VAR_ASIGNACION);
        }
    }

    // ClausulasFinalesFor -> ; Expresion ; Expresion
    private void clausulasFinalesFor() throws ExcepcionLexica, ExcepcionSintactica {
        match(TipoDeToken.PUNTO_Y_COMA);
        sintaxis.expresiones().expresion();
        match(TipoDeToken.PUNTO_Y_COMA);
        sintaxis.expresiones().expresion();
    }
}
