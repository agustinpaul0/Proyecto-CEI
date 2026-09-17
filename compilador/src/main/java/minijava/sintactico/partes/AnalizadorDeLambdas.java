package minijava.sintactico.partes;

import minijava.lexico.errores.ExcepcionLexica;
import minijava.lexico.tokens.TipoDeToken;
import minijava.lexico.tokens.Token;
import minijava.sintactico.Sintaxis;
import minijava.sintactico.errores.ExcepcionSintactica;
import minijava.sintactico.primeros.PrimerosDeExpresiones;
import minijava.sintactico.primeros.PrimerosDeLambdas;

// MiniLambdas, y la desambiguación de sus dos cabezas.
//
// La lambda vive en el nivel MÁS ALTO de la jerarquía de expresiones, como en el
// JLS: no es un Primario y por lo tanto no puede ser operando de ningún
// operador. Las dos cabezas que comparte con una expresión normal son `idMetVar`
// —AccesoLlamadaOLambda— y `(` —ParentizadaOLambda—.
//
// `(` abre tanto una expresión parentizada como la lista de parámetros de una
// lambda. Parece necesitar lookahead ilimitado (`(a,b,c) ->` contra `(a+b)`),
// pero no: `()` sólo puede ser una lambda de cero parámetros, `(e ,` sólo puede
// ser una lambda porque MiniJava no tiene operador coma, y `(e)` se decide con
// el token que sigue al `)`. Un token de lookahead en cada punto.
//
// Lo que la lambda usa —`this`, los nombres que captura, que sus parámetros no
// se repitan— no lo mira el sintáctico: son restricciones de significado y las
// resuelve el analizador semántico.
public final class AnalizadorDeLambdas extends ParteDelAnalizador {

    public AnalizadorDeLambdas(Sintaxis sintaxis) {
        super(sintaxis);
    }

    // AccesoLlamadaOLambda -> -> CuerpoLambda
    // AccesoLlamadaOLambda -> AccesoLlamada ColaDesdePrimarioSinAsignacion
    //
    // El idMetVar ya lo consumió ExpresionDesdePrimarioOLambda; acá se decide si
    // era el único parámetro de una lambda o el arranque de una expresión normal.
    public void accesoLlamadaOLambda(Token nombre) throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeLambdas.ACCESO_LLAMADA_OLAMBDA_FLECHA)) {
            match(TipoDeToken.OP_FLECHA);
            cuerpoLambda();
            return;
        }
        sintaxis.referencias().accesoLlamada(nombre);
        sintaxis.expresiones().colaDesdePrimarioSinAsignacion();
    }

    // ParentizadaOLambda -> ( ContenidoDeParentesis
    // ContenidoDeParentesis -> ) -> CuerpoLambda
    // ContenidoDeParentesis -> Expresion RestoDeParentesis
    // RestoDeParentesis -> ) FinDeParentizadaOLambda
    public void parentizadaOLambda() throws ExcepcionLexica, ExcepcionSintactica {
        match(TipoDeToken.PAREN_A);

        if (estaEn(PrimerosDeLambdas.CONTENIDO_DE_PARENTESIS_PAREN_C)) {
            match(TipoDeToken.PAREN_C);
            match(TipoDeToken.OP_FLECHA);
            cuerpoLambda();
            return;
        }

        Candidato primero = leerCandidatoAParametro();
        if (estaEn(PrimerosDeLambdas.RESTO_DE_PARENTESIS_COMA)) {
            lambdaDeVariosParametros(primero);
        } else {
            match(TipoDeToken.PAREN_C);
            finDeParentizadaOLambda(primero);
        }
    }

    // CuerpoLambda -> Expresion
    //
    // Voraz: la expresión consume todo lo que puede, así que `x -> a + b` es
    // `x -> (a + b)`. Es una de las ambigüedades declaradas.
    public void cuerpoLambda() throws ExcepcionLexica, ExcepcionSintactica {
        sintaxis.expresiones().expresion();
        sintaxis.anotarQueEsLambda();
    }

    // RestoDeParentesis -> , ListaParametrosLambda ) -> CuerpoLambda
    // ListaParametrosLambda -> idMetVar RestoListaParametrosLambda
    private void lambdaDeVariosParametros(Candidato primero)
            throws ExcepcionLexica, ExcepcionSintactica {
        exigirParametro(primero);
        restoListaParametrosLambda();
        match(TipoDeToken.PAREN_C);
        match(TipoDeToken.OP_FLECHA);
        cuerpoLambda();
    }

    // RestoListaParametrosLambda -> , idMetVar RestoListaParametrosLambda | EPSILON
    private void restoListaParametrosLambda() throws ExcepcionLexica, ExcepcionSintactica {
        if (!estaEn(PrimerosDeLambdas.RESTO_DE_PARENTESIS_COMA)) {
            return;
        }
        match(TipoDeToken.COMA);
        match(TipoDeToken.ID_MV);
        restoListaParametrosLambda();
    }

    // FinDeParentizadaOLambda -> -> CuerpoLambda
    // FinDeParentizadaOLambda -> ColaDesdePrimarioSinAsignacion
    private void finDeParentizadaOLambda(Candidato primero)
            throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeLambdas.FIN_DE_PARENTIZADA_OLAMBDA_FLECHA)) {
            exigirParametro(primero);
            match(TipoDeToken.OP_FLECHA);
            cuerpoLambda();
            return;
        }
        exigirQueLaLambdaParentizadaNoLleveCola(primero);
        sintaxis.expresiones().colaDesdePrimarioSinAsignacion();
    }

    // `f((x -> x))` es válido y `(x -> x).m()` no: en Java una lambda, aunque
    // esté parentizada, no puede ser operando ni receptor. Es la única forma de
    // esa regla que la gramática no alcanza a expresar, porque distinguirlas
    // exigiría clonar la cadena de colas en su variante «el contenido fue una
    // lambda».
    private void exigirQueLaLambdaParentizadaNoLleveCola(Candidato primero)
            throws ExcepcionSintactica {
        if (primero.fueLambda()
                && estaEn(PrimerosDeExpresiones.COLA_DESDE_PRIMARIO_SIN_ASIGNACION_RESTO_REFERENCIA)) {
            throw ExcepcionSintactica.porRestriccion(actual(),
                    "una lambda no puede ser operando de " + actual().lexema());
        }
    }

    // Hasta ver el `->` no se sabe si lo que hay entre paréntesis es el primer
    // parámetro de una lambda o una expresión parentizada, así que se lee como
    // expresión y se recuerda si fue exactamente un idMetVar.
    private Candidato leerCandidatoAParametro() throws ExcepcionLexica, ExcepcionSintactica {
        Token primero = actual();
        int antes = marca();
        sintaxis.expresiones().expresion();
        boolean fueUnIdMetVarSinTipo =
                primero.tipo() == TipoDeToken.ID_MV && consumioUnSoloTokenDesde(antes);
        return new Candidato(primero, fueUnIdMetVarSinTipo, sintaxis.ultimaExpresionFueLambda());
    }

    // Un parámetro de lambda tiene que ser un identificador sin tipo: `(a) -> a` es
    // una lambda y `(a + 1) -> a` no. La gramática no puede distinguirlos —los dos
    // son una Expresion hasta ver el `->`—, así que lo verifica el analizador.
    // Que el nombre no se repita SÍ es del semántico, y no se mira acá.
    private void exigirParametro(Candidato candidato) throws ExcepcionSintactica {
        if (!candidato.esIdMetVarSinTipo()) {
            throw ExcepcionSintactica.porRestriccion(candidato.token(),
                    "un parametro de lambda tiene que ser un identificador");
        }
    }

    private record Candidato(Token token, boolean esIdMetVarSinTipo, boolean fueLambda) {
    }
}
