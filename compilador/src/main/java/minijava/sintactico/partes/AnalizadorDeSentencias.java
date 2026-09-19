package minijava.sintactico.partes;

import minijava.lexico.errores.ExcepcionLexica;
import minijava.lexico.tokens.TipoDeToken;
import minijava.sintactico.Sintaxis;
import minijava.sintactico.errores.ExcepcionSintactica;
import minijava.sintactico.primeros.PrimerosDeSentencias;
import minijava.sintactico.recuperacion.ContextoDeSincronizacion;

// Bloques y sentencias. Una declaración local puede empezar con idClase
// (`C x;`) y una sentencia-expresión también (`C.m();`): ese idClase se
// factoriza y el desempate lo hace el token siguiente, donde `.` es expresión y
// cualquier otro es declaración.
public final class AnalizadorDeSentencias extends ParteDelAnalizador {

    public AnalizadorDeSentencias(Sintaxis sintaxis) {
        super(sintaxis);
    }

    // Bloque -> { ListaSentencias }
    public void bloque() throws ExcepcionLexica, ExcepcionSintactica {
        match(TipoDeToken.LLAVE_A);
        listaSentencias();
        match(TipoDeToken.LLAVE_C);
    }

    // ListaSentencias -> Sentencia ListaSentencias | EPSILON
    //
    // La recursión es la de la producción; lo único que agrega `reanudaTrasError`
    // es que un elemento roto no corte la lista: registra el error, sincroniza y
    // dice si se puede seguir con el siguiente.
    private void listaSentencias() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeSentencias.LISTA_SENTENCIAS_SENTENCIA)) {
            if (reanudaTrasError(ContextoDeSincronizacion.LISTA_DE_SENTENCIAS, this::sentencia)) {
                listaSentencias();
            }
        } else {
            // ListaSentencias -> EPSILON
        }
    }

    // Sentencia -> ; | VarLocal ; | Return ; | If | While | Bloque | For
    // Sentencia -> ExpresionSinIdClase ;
    // Sentencia -> TipoPrimitivo DimensionesOpcionales ListaDeclaradores ;
    // Sentencia -> idGen DimensionesOpcionales ListaDeclaradores ;
    // Sentencia -> idClase SentenciaDesdeIdClase
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
        } else if (estaEn(PrimerosDeSentencias.SENTENCIA_FOR)) {
            sintaxis.fors().sentenciaFor();
        } else if (estaEn(PrimerosDeSentencias.SENTENCIA_EXPRESION_SIN_ID_CLASE)) {
            sintaxis.expresiones().expresionSinIdClase();
            match(TipoDeToken.PUNTO_Y_COMA);
        } else if (estaEn(PrimerosDeSentencias.SENTENCIA_TIPO_PRIMITIVO)) {
            sintaxis.tipos().tipoPrimitivo();
            sintaxis.tipos().dimensionesOpcionales();
            listaDeclaradores();
            match(TipoDeToken.PUNTO_Y_COMA);
        } else if (estaEn(PrimerosDeSentencias.SENTENCIA_ID_GEN)) {
            match(TipoDeToken.ID_GEN);
            sintaxis.tipos().dimensionesOpcionales();
            listaDeclaradores();
            match(TipoDeToken.PUNTO_Y_COMA);
        } else if (estaEn(PrimerosDeSentencias.SENTENCIA_ID_CLASE)) {
            match(TipoDeToken.ID_CLASE);
            sentenciaDesdeIdClase();
        } else {
            throw errorEsperando(PrimerosDeSentencias.SENTENCIA_PUNTO_Y_COMA,
                    PrimerosDeSentencias.SENTENCIA_VAR_LOCAL, PrimerosDeSentencias.SENTENCIA_RETURN,
                    PrimerosDeSentencias.SENTENCIA_IF, PrimerosDeSentencias.SENTENCIA_WHILE,
                    PrimerosDeSentencias.SENTENCIA_BLOQUE, PrimerosDeSentencias.SENTENCIA_FOR,
                    PrimerosDeSentencias.SENTENCIA_EXPRESION_SIN_ID_CLASE,
                    PrimerosDeSentencias.SENTENCIA_TIPO_PRIMITIVO, PrimerosDeSentencias.SENTENCIA_ID_GEN,
                    PrimerosDeSentencias.SENTENCIA_ID_CLASE);
        }
    }

    // SentenciaDesdeIdClase -> TipoGenericoOpcional DimensionesOpcionales ListaDeclaradores ;
    // SentenciaDesdeIdClase -> . idMetVar ArgsActuales ColaDeExpresionDesdePrimario ;
    private void sentenciaDesdeIdClase() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeSentencias.SENTENCIA_DESDE_ID_CLASE_TIPO_GENERICO_OPCIONAL)) {
            sintaxis.tipos().tipoGenericoOpcional();
            sintaxis.tipos().dimensionesOpcionales();
            listaDeclaradores();
            match(TipoDeToken.PUNTO_Y_COMA);
        } else if (estaEn(PrimerosDeSentencias.SENTENCIA_DESDE_ID_CLASE_PUNTO)) {
            match(TipoDeToken.PUNTO);
            match(TipoDeToken.ID_MV);
            sintaxis.argumentos().argsActuales();
            sintaxis.expresiones().colaDeExpresionDesdePrimario();
            match(TipoDeToken.PUNTO_Y_COMA);
        } else {
            throw errorEsperando(PrimerosDeSentencias.SENTENCIA_DESDE_ID_CLASE_TIPO_GENERICO_OPCIONAL,
                    PrimerosDeSentencias.SENTENCIA_DESDE_ID_CLASE_PUNTO);
        }
    }

    // VarLocal -> var idMetVar = ExpresionCondicional
    private void varLocal() throws ExcepcionLexica, ExcepcionSintactica {
        match(TipoDeToken.PR_VAR);
        match(TipoDeToken.ID_MV);
        match(TipoDeToken.OP_ASIGNACION);
        sintaxis.expresiones().expresionCondicional();
    }

    // ListaDeclaradores -> idMetVar InicializacionDeDeclarador RestoListaDeclaradores
    //
    // Varias variables por sentencia, cada una con inicialización opcional:
    // `int x,y,z = 10;`
    private void listaDeclaradores() throws ExcepcionLexica, ExcepcionSintactica {
        match(TipoDeToken.ID_MV);
        inicializacionDeDeclarador();
        restoListaDeclaradores();
    }

    // RestoListaDeclaradores -> , idMetVar InicializacionDeDeclarador RestoListaDeclaradores
    //                         | EPSILON
    private void restoListaDeclaradores() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeSentencias.RESTO_LISTA_DECLARADORES_COMA)) {
            match(TipoDeToken.COMA);
            match(TipoDeToken.ID_MV);
            inicializacionDeDeclarador();
            restoListaDeclaradores();
        } else {
            // RestoListaDeclaradores -> EPSILON
        }
    }

    // InicializacionDeDeclarador -> = ExpresionCondicional | EPSILON
    private void inicializacionDeDeclarador() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeSentencias.INICIALIZACION_DE_DECLARADOR_ASIGNACION)) {
            match(TipoDeToken.OP_ASIGNACION);
            sintaxis.expresiones().expresionCondicional();
        } else {
            // InicializacionDeDeclarador -> EPSILON
        }
    }

    // Return -> return ExpresionOpcional
    private void sentenciaReturn() throws ExcepcionLexica, ExcepcionSintactica {
        match(TipoDeToken.PR_RETURN);
        expresionOpcional();
    }

    // ExpresionOpcional -> Expresion | EPSILON
    private void expresionOpcional() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeSentencias.EXPRESION_OPCIONAL_EXPRESION)) {
            sintaxis.expresiones().expresion();
        } else {
            // ExpresionOpcional -> EPSILON
        }
    }

    // If -> if ( Expresion ) Sentencia ElseOpcional
    private void sentenciaIf() throws ExcepcionLexica, ExcepcionSintactica {
        match(TipoDeToken.PR_IF);
        match(TipoDeToken.PAREN_A);
        sintaxis.expresiones().expresion();
        match(TipoDeToken.PAREN_C);
        sentencia();
        elseOpcional();
    }

    // ElseOpcional -> else Sentencia | EPSILON
    //
    // Dangling else: se toma la rama `else` apenas se ve el token, así que liga al
    // `if` más cercano. Es una de las dos ambigüedades que quedan en la gramática,
    // resuelta por regla de implementación (Clase 4, slide 49).
    private void elseOpcional() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeSentencias.ELSE_OPCIONAL_ELSE)) {
            match(TipoDeToken.PR_ELSE);
            sentencia();
        } else {
            // ElseOpcional -> EPSILON
        }
    }

    // While -> while ( Expresion ) Sentencia
    private void sentenciaWhile() throws ExcepcionLexica, ExcepcionSintactica {
        match(TipoDeToken.PR_WHILE);
        match(TipoDeToken.PAREN_A);
        sintaxis.expresiones().expresion();
        match(TipoDeToken.PAREN_C);
        sentencia();
    }
}
