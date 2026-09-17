package minijava.sintactico.partes;

import minijava.lexico.errores.ExcepcionLexica;
import minijava.lexico.tokens.TipoDeToken;
import minijava.lexico.tokens.Token;
import minijava.sintactico.Sintaxis;
import minijava.sintactico.errores.ExcepcionSintactica;
import minijava.sintactico.primeros.PrimerosDeLambdas;

// MiniLambdas, y la desambiguación de sus dos cabezas.
//
// La lambda se produce en el nivel MÁS ALTO de la jerarquía, así que sin
// paréntesis no es operando: `1 + x -> x` no deriva. Entre paréntesis sí lo es,
// porque MiniJava base define `ExpresionParentizada -> ( Expresion )` y
// `Primario -> ExpresionParentizada`, y un Primario arrastra cadena: de ahí que
// `(x -> y).m()` sea válido. Java lo rechaza; se eligió respetar el molde de la
// gramática base. Ver decisiones-de-logica.md §2.2.
//
// Las dos cabezas que comparte con una expresión normal son `idMetVar`
// —AccesoLlamadaOLambda— y `(` —ParentizadaOLambda—.
//
// `(` abre tanto una expresión parentizada como la lista de parámetros de una
// lambda. Parece necesitar lookahead ilimitado (`(a,b,c) ->` contra `(a+b)`),
// pero no: `()` sólo puede ser una lambda de cero parámetros, `(e ,` sólo puede
// ser una lambda porque MiniJava no tiene operador coma, y `(e)` se decide con
// el token que sigue al `)`. Un token de lookahead en cada punto.
//
// Lo único que este analizador verifica y la gramática no puede expresar es que
// el primer parámetro sea un identificador sin tipo. El `this`, los nombres que
// captura y que sus parámetros no se repitan son restricciones de significado y
// las resuelve el analizador semántico.
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
        sintaxis.expresiones().colaDesdePrimarioSinAsignacion();
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
        return new Candidato(primero, fueUnIdMetVarSinTipo);
    }

    // Sólo el PRIMER parámetro llega hasta acá, y por eso el chequeo existe: como
    // `(` abre tanto una lambda como una expresión parentizada, se lo leyó como
    // Expresion, y hay que confirmar que fue un identificador sin tipo. `(a) -> a`
    // es una lambda y `(a + 1) -> a` no.
    //
    // Del segundo en adelante no hay ambigüedad posible, así que la gramática
    // escribe `idMetVar` como terminal y los rechaza sola: `(x, int y) -> x` muere
    // en el match, sin pasar por acá.
    //
    // Que el nombre no se repita SÍ es del semántico: `(x, x) -> x` se acepta.
    private void exigirParametro(Candidato candidato) throws ExcepcionSintactica {
        if (!candidato.esIdMetVarSinTipo()) {
            throw ExcepcionSintactica.porRestriccion(candidato.token(),
                    "un parametro de lambda tiene que ser un identificador");
        }
    }

    private record Candidato(Token token, boolean esIdMetVarSinTipo) {
    }
}
