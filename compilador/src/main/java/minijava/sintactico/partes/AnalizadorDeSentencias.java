package minijava.sintactico.partes;

import minijava.comun.config.Logro;
import minijava.lexico.errores.ExcepcionLexica;
import minijava.lexico.tokens.TipoDeToken;
import minijava.sintactico.Sintaxis;
import minijava.sintactico.errores.ExcepcionSintactica;
import minijava.sintactico.primeros.PrimerosDeSentencias;
import minijava.sintactico.recuperacion.ContextoDeSincronizacion;

// Bloques y sentencias. Con el logro de variables locales clásicas una
// declaración puede empezar con idClase (`C x;`) y una sentencia-expresión
// también (`C.m();`): ese idClase se factoriza y el desempate lo hace el token
// siguiente, donde `.` es expresión y cualquier otro es declaración.
public final class AnalizadorDeSentencias extends ParteDelAnalizador {

    public AnalizadorDeSentencias(Sintaxis sintaxis) {
        super(sintaxis);
    }

    // Bloque -> { ListaSentencias }
    // ListaSentencias -> Sentencia ListaSentencias | EPSILON
    public void bloque() throws ExcepcionLexica, ExcepcionSintactica {
        match(TipoDeToken.LLAVE_A);
        while (estaEn(PrimerosDeSentencias.LISTA_SENTENCIAS_SENTENCIA)) {
            if (!reanudaTrasError(ContextoDeSincronizacion.LISTA_DE_SENTENCIAS, this::sentencia)) {
                break;
            }
        }
        match(TipoDeToken.LLAVE_C);
    }

    // Sentencia -> ; | VarLocal ; | Return ; | If | While | Bloque
    // Sentencia -> For                                                        [+for]
    // Sentencia -> SentenciaExpresion ;                                       [-varclasica]
    // SentenciaExpresion -> Expresion                                          [-varclasica]
    // Sentencia -> ExpresionSinIdClase ;                                      [+varclasica]
    // Sentencia -> TipoPrimitivo DimensionesOpcionales ListaDeclaradores ;    [+varclasica]
    // Sentencia -> idGen DimensionesOpcionales ListaDeclaradores ;            [+varclasica]
    // Sentencia -> idClase SentenciaDesdeIdClase                              [+varclasica]
    public void sentencia() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeSentencias.SENTENCIA_PUNTO_Y_COMA)) {
            match(TipoDeToken.PUNTO_Y_COMA);
        } else if (estaEn(PrimerosDeSentencias.SENTENCIA_VAR_LOCAL)) {
            varLocal();
            match(TipoDeToken.PUNTO_Y_COMA);
        } else if (estaEn(PrimerosDeSentencias.SENTENCIA_RETURN)) {
            sentenciaReturn();
            match(TipoDeToken.PUNTO_Y_COMA);
        } else if (estaEn(PrimerosDeSentencias.SENTENCIA_IF)) {
            sentenciaIf();
        } else if (estaEn(PrimerosDeSentencias.SENTENCIA_WHILE)) {
            sentenciaWhile();
        } else if (estaEn(PrimerosDeSentencias.SENTENCIA_BLOQUE)) {
            bloque();
        } else if (hayLogro(Logro.FOR) && estaEn(PrimerosDeSentencias.SENTENCIA_FOR)) {
            sintaxis.fors().sentenciaFor();
        } else if (!hayLogro(Logro.VARCLASICA)) {
            sintaxis.expresiones().expresion();
            match(TipoDeToken.PUNTO_Y_COMA);
        } else {
            sentenciaODeclaracionLocal();
        }
    }

    // ExpresionOpcional -> Expresion | EPSILON
    public void expresionOpcional() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeSentencias.EXPRESION_OPCIONAL_EXPRESION)) {
            sintaxis.expresiones().expresion();
        }
    }

    // ListaDeclaradores -> idMetVar InicializacionDeDeclarador RestoListaDeclaradores
    // RestoListaDeclaradores -> , idMetVar InicializacionDeDeclarador RestoListaDeclaradores
    //                         | EPSILON
    //
    // Varias variables por sentencia, cada una con inicialización opcional:
    // `int x,y,z = 10;`
    public void listaDeclaradores() throws ExcepcionLexica, ExcepcionSintactica {
        match(TipoDeToken.ID_MV);
        inicializacionDeDeclarador();
        while (estaEn(PrimerosDeSentencias.RESTO_LISTA_DECLARADORES_COMA)) {
            match(TipoDeToken.COMA);
            match(TipoDeToken.ID_MV);
            inicializacionDeDeclarador();
        }
    }

    // InicializacionDeDeclarador -> = ExpresionCondicional | EPSILON
    private void inicializacionDeDeclarador() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeSentencias.INICIALIZACION_DE_DECLARADOR_ASIGNACION)) {
            match(TipoDeToken.OP_ASIGNACION);
            sintaxis.expresiones().expresionCondicional();
        }
    }

    private void sentenciaODeclaracionLocal() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeSentencias.SENTENCIA_TIPO_PRIMITIVO)) {
            sintaxis.tipos().tipoPrimitivo();
            declaracionLocalTrasTipo();
        } else if (estaEn(PrimerosDeSentencias.SENTENCIA_ID_GEN)) {
            match(TipoDeToken.ID_GEN);
            declaracionLocalTrasTipo();
        } else if (estaEn(PrimerosDeSentencias.SENTENCIA_ID_CLASE)) {
            match(TipoDeToken.ID_CLASE);
            sentenciaDesdeIdClase();
        } else {
            sintaxis.expresiones().expresionSinIdClase();
            match(TipoDeToken.PUNTO_Y_COMA);
        }
    }

    private void declaracionLocalTrasTipo() throws ExcepcionLexica, ExcepcionSintactica {
        sintaxis.tipos().dimensionesOpcionales();
        listaDeclaradores();
        match(TipoDeToken.PUNTO_Y_COMA);
    }

    // SentenciaDesdeIdClase -> TipoGenericoOpcional DimensionesOpcionales ListaDeclaradores ;
    // SentenciaDesdeIdClase -> . idMetVar ArgsActuales ColaDeExpresionDesdePrimario ;
    private void sentenciaDesdeIdClase() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeSentencias.SENTENCIA_DESDE_ID_CLASE_PUNTO)) {
            match(TipoDeToken.PUNTO);
            match(TipoDeToken.ID_MV);
            sintaxis.argumentos().argsActuales();
            sintaxis.expresiones().colaDeExpresionDesdePrimario();
            match(TipoDeToken.PUNTO_Y_COMA);
        } else {
            sintaxis.tipos().tipoGenericoOpcional();
            declaracionLocalTrasTipo();
        }
    }

    // VarLocal -> var idMetVar = ExpresionCondicional
    private void varLocal() throws ExcepcionLexica, ExcepcionSintactica {
        match(TipoDeToken.PR_VAR);
        match(TipoDeToken.ID_MV);
        match(TipoDeToken.OP_ASIGNACION);
        sintaxis.expresiones().expresionCondicional();
    }

    // Return -> return ExpresionOpcional
    private void sentenciaReturn() throws ExcepcionLexica, ExcepcionSintactica {
        match(TipoDeToken.PR_RETURN);
        expresionOpcional();
    }

    // If -> if ( Expresion ) Sentencia ElseOpcional
    // ElseOpcional -> else Sentencia | EPSILON
    //
    // Dangling else: se toma la rama `else` apenas se ve el token, así que liga al
    // `if` más cercano. Es una de las siete ambigüedades declaradas, resuelta por
    // regla de implementación (Clase 4, slide 49) y no reescribiendo la gramática.
    private void sentenciaIf() throws ExcepcionLexica, ExcepcionSintactica {
        match(TipoDeToken.PR_IF);
        condicionEntreParentesis();
        sentencia();
        if (estaEn(PrimerosDeSentencias.ELSE_OPCIONAL_ELSE)) {
            match(TipoDeToken.PR_ELSE);
            sentencia();
        }
    }

    // While -> while ( Expresion ) Sentencia
    private void sentenciaWhile() throws ExcepcionLexica, ExcepcionSintactica {
        match(TipoDeToken.PR_WHILE);
        condicionEntreParentesis();
        sentencia();
    }

    private void condicionEntreParentesis() throws ExcepcionLexica, ExcepcionSintactica {
        match(TipoDeToken.PAREN_A);
        sintaxis.expresiones().expresion();
        match(TipoDeToken.PAREN_C);
    }
}
