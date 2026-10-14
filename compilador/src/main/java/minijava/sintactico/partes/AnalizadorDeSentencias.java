package minijava.sintactico.partes;

import minijava.lexico.errores.ExcepcionLexica;
import minijava.lexico.tokens.TipoDeToken;
import minijava.lexico.tokens.Token;
import minijava.semantico.ast.expresiones.NodoExpresion;
import minijava.semantico.ast.expresiones.NodoLlamadaEstatica;
import minijava.semantico.ast.sentencias.NodoBloque;
import minijava.semantico.ast.sentencias.NodoIf;
import minijava.semantico.ast.sentencias.NodoReturn;
import minijava.semantico.ast.sentencias.NodoSentencia;
import minijava.semantico.ast.sentencias.NodoSentenciaExpresion;
import minijava.semantico.ast.sentencias.NodoVacia;
import minijava.semantico.ast.sentencias.NodoVarLocal;
import minijava.semantico.ast.sentencias.NodoWhile;
import minijava.semantico.ast.sentencias.SentenciaNoControlada;
import minijava.sintactico.Sintaxis;
import minijava.sintactico.errores.ExcepcionSintactica;
import minijava.sintactico.primeros.PrimerosDeSentencias;
import minijava.sintactico.recuperacion.ContextoDeSincronizacion;

import java.util.ArrayList;
import java.util.List;

// Bloques y sentencias. Una declaración local puede empezar con idClase
// (`C x;`) y una sentencia-expresión también (`C.m();`): ese idClase se
// factoriza y el desempate lo hace el token siguiente, donde `.` es expresión y
// cualquier otro es declaración.
//
// Cada método devuelve el nodo del AST de lo que reconoció (Clase 09, slides
// 40-41). La declaración local con tipo es del logro Variables Locales Clásicas,
// que no se controla: se reconoce entera y queda como SentenciaNoControlada.
public final class AnalizadorDeSentencias extends ParteDelAnalizador {

    public AnalizadorDeSentencias(Sintaxis sintaxis) {
        super(sintaxis);
    }

    // Bloque -> { ListaSentencias }
    public NodoBloque bloque() throws ExcepcionLexica, ExcepcionSintactica {
        match(TipoDeToken.LLAVE_A);
        List<NodoSentencia> sentencias = new ArrayList<>();
        listaSentencias(sentencias);
        match(TipoDeToken.LLAVE_C);
        return new NodoBloque(sentencias);
    }

    // ListaSentencias -> Sentencia ListaSentencias | EPSILON
    //
    // La recursión es la de la producción; lo único que agrega `reanudaTrasError`
    // es que un elemento roto no corte la lista: registra el error, sincroniza y
    // dice si se puede seguir con el siguiente. `sentencias` acumula los nodos.
    private void listaSentencias(List<NodoSentencia> sentencias) throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeSentencias.LISTA_SENTENCIAS_SENTENCIA)) {
            if (reanudaTrasError(ContextoDeSincronizacion.LISTA_DE_SENTENCIAS,
                    () -> sentencias.add(sentencia()))) {
                listaSentencias(sentencias);
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
    public NodoSentencia sentencia() throws ExcepcionLexica, ExcepcionSintactica {
        Token inicio = actual();
        if (estaEn(PrimerosDeSentencias.SENTENCIA_PUNTO_Y_COMA)) {
            match(TipoDeToken.PUNTO_Y_COMA);
            return new NodoVacia();
        } else if (estaEn(PrimerosDeSentencias.SENTENCIA_VAR_LOCAL)) {
            NodoVarLocal declaracion = varLocal();
            match(TipoDeToken.PUNTO_Y_COMA);
            return declaracion;
        } else if (estaEn(PrimerosDeSentencias.SENTENCIA_RETURN)) {
            NodoReturn retorno = sentenciaReturn();
            match(TipoDeToken.PUNTO_Y_COMA);
            return retorno;
        } else if (estaEn(PrimerosDeSentencias.SENTENCIA_IF)) {
            return sentenciaIf();
        } else if (estaEn(PrimerosDeSentencias.SENTENCIA_WHILE)) {
            return sentenciaWhile();
        } else if (estaEn(PrimerosDeSentencias.SENTENCIA_BLOQUE)) {
            return bloque();
        } else if (estaEn(PrimerosDeSentencias.SENTENCIA_FOR)) {
            return sintaxis.fors().sentenciaFor();
        } else if (estaEn(PrimerosDeSentencias.SENTENCIA_EXPRESION_SIN_ID_CLASE)) {
            NodoExpresion expresion = sintaxis.expresiones().expresionSinIdClase();
            match(TipoDeToken.PUNTO_Y_COMA);
            return new NodoSentenciaExpresion(expresion);
        } else if (estaEn(PrimerosDeSentencias.SENTENCIA_TIPO_PRIMITIVO)) {
            sintaxis.tipos().tipoPrimitivo();
            sintaxis.tipos().dimensionesOpcionales();
            listaDeclaradores();
            match(TipoDeToken.PUNTO_Y_COMA);
            return new SentenciaNoControlada(inicio);
        } else if (estaEn(PrimerosDeSentencias.SENTENCIA_ID_GEN)) {
            match(TipoDeToken.ID_GEN);
            sintaxis.tipos().dimensionesOpcionales();
            listaDeclaradores();
            match(TipoDeToken.PUNTO_Y_COMA);
            return new SentenciaNoControlada(inicio);
        } else if (estaEn(PrimerosDeSentencias.SENTENCIA_ID_CLASE)) {
            match(TipoDeToken.ID_CLASE);
            return sentenciaDesdeIdClase(inicio);
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
    //
    // `idClase` es el token que ya consumió `sentencia`.
    private NodoSentencia sentenciaDesdeIdClase(Token idClase) throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeSentencias.SENTENCIA_DESDE_ID_CLASE_TIPO_GENERICO_OPCIONAL)) {
            sintaxis.tipos().tipoGenericoOpcional();
            sintaxis.tipos().dimensionesOpcionales();
            listaDeclaradores();
            match(TipoDeToken.PUNTO_Y_COMA);
            return new SentenciaNoControlada(idClase);
        } else if (estaEn(PrimerosDeSentencias.SENTENCIA_DESDE_ID_CLASE_PUNTO)) {
            Token punto = actual();
            match(TipoDeToken.PUNTO);
            Token nombre = actual();
            match(TipoDeToken.ID_MV);
            Token parentesis = actual();
            List<NodoExpresion> argumentos = sintaxis.argumentos().argsActuales();
            NodoExpresion expresion = sintaxis.expresiones().colaDeExpresionDesdePrimario(
                    new NodoLlamadaEstatica(idClase, punto, nombre, parentesis, argumentos));
            match(TipoDeToken.PUNTO_Y_COMA);
            return new NodoSentenciaExpresion(expresion);
        } else {
            throw errorEsperando(PrimerosDeSentencias.SENTENCIA_DESDE_ID_CLASE_TIPO_GENERICO_OPCIONAL,
                    PrimerosDeSentencias.SENTENCIA_DESDE_ID_CLASE_PUNTO);
        }
    }

    // VarLocal -> var idMetVar = ExpresionCondicional
    private NodoVarLocal varLocal() throws ExcepcionLexica, ExcepcionSintactica {
        match(TipoDeToken.PR_VAR);
        Token nombre = actual();
        match(TipoDeToken.ID_MV);
        match(TipoDeToken.OP_ASIGNACION);
        return new NodoVarLocal(nombre, sintaxis.expresiones().expresionCondicional());
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
    private NodoReturn sentenciaReturn() throws ExcepcionLexica, ExcepcionSintactica {
        Token palabraReturn = actual();
        match(TipoDeToken.PR_RETURN);
        return new NodoReturn(palabraReturn, expresionOpcional());
    }

    // ExpresionOpcional -> Expresion | EPSILON
    //
    // Sin expresión devuelve null: el return sin valor.
    private NodoExpresion expresionOpcional() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeSentencias.EXPRESION_OPCIONAL_EXPRESION)) {
            return sintaxis.expresiones().expresion();
        } else {
            // ExpresionOpcional -> EPSILON
            return null;
        }
    }

    // If -> if ( Expresion ) Sentencia ElseOpcional
    private NodoIf sentenciaIf() throws ExcepcionLexica, ExcepcionSintactica {
        match(TipoDeToken.PR_IF);
        match(TipoDeToken.PAREN_A);
        NodoExpresion condicion = sintaxis.expresiones().expresion();
        match(TipoDeToken.PAREN_C);
        NodoSentencia entonces = sentencia();
        return new NodoIf(condicion, entonces, elseOpcional());
    }

    // ElseOpcional -> else Sentencia | EPSILON
    //
    // Dangling else: se toma la rama `else` apenas se ve el token, así que liga al
    // `if` más cercano. Es una de las dos ambigüedades que quedan en la gramática,
    // resuelta por regla de implementación (Clase 4, slide 49). Sin else devuelve null.
    private NodoSentencia elseOpcional() throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeSentencias.ELSE_OPCIONAL_ELSE)) {
            match(TipoDeToken.PR_ELSE);
            return sentencia();
        } else {
            // ElseOpcional -> EPSILON
            return null;
        }
    }

    // While -> while ( Expresion ) Sentencia
    private NodoWhile sentenciaWhile() throws ExcepcionLexica, ExcepcionSintactica {
        match(TipoDeToken.PR_WHILE);
        match(TipoDeToken.PAREN_A);
        NodoExpresion condicion = sintaxis.expresiones().expresion();
        match(TipoDeToken.PAREN_C);
        return new NodoWhile(condicion, sentencia());
    }
}
