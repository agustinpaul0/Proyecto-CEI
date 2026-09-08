package minijava.sintactico.partes;

import minijava.comun.config.Logro;
import minijava.lexico.errores.ExcepcionLexica;
import minijava.lexico.tokens.TipoDeToken;
import minijava.lexico.tokens.Token;
import minijava.sintactico.RestriccionesDeLambda;
import minijava.sintactico.Sintaxis;
import minijava.sintactico.errores.ExcepcionSintactica;
import minijava.sintactico.primeros.Primeros;

import java.util.HashSet;
import java.util.Set;

// El logro MiniLambdas E2, y la desambiguación del paréntesis.
//
// `(` abre tanto una expresión parentizada como la lista de parámetros de una
// lambda. Parece necesitar lookahead ilimitado (`(a,b,c) ->` contra `(a+b)`),
// pero no: `()` sólo puede ser una lambda de cero parámetros, `(e ,` sólo puede
// ser una lambda porque MiniJava no tiene operador coma, y `(e)` se decide con
// el token que sigue al `)`. Un token de lookahead en cada punto.
public final class AnalizadorDeLambdas extends ParteDelAnalizador {

    public AnalizadorDeLambdas(Sintaxis sintaxis) {
        super(sintaxis);
    }

    // ParentizadaOLambda -> ( ContenidoDeParentesis
    // ContenidoDeParentesis -> ) -> CuerpoLambda   [+lambda]
    // ContenidoDeParentesis -> Expresion RestoDeParentesis
    public void parentizadaOLambda() throws ExcepcionLexica, ExcepcionSintactica {
        match(TipoDeToken.PAREN_A);

        if (hayLogro(Logro.LAMBDA) && estaEn(Primeros.CONTENIDO_DE_PARENTESIS_PAREN_C)) {
            match(TipoDeToken.PAREN_C);
            match(TipoDeToken.OP_FLECHA);
            cuerpoLambda(Set.of());
            return;
        }

        Candidato primero = leerCandidatoAParametro();
        if (hayLogro(Logro.LAMBDA) && estaEn(Primeros.RESTO_DE_PARENTESIS_COMA)) {
            lambdaDeVariosParametros(primero);
        } else {
            match(TipoDeToken.PAREN_C);
            finDeParentizadaOLambda(primero);
        }
    }

    // CuerpoLambda -> Expresion   [+lambda]
    //
    // Voraz: la expresión consume todo lo que puede, así que `x -> a + b` es
    // `x -> (a + b)`. Es una de las siete ambigüedades declaradas.
    public void cuerpoLambda(Set<String> parametros) throws ExcepcionLexica, ExcepcionSintactica {
        RestriccionesDeLambda restricciones = sintaxis.restriccionesDeLambda();
        RestriccionesDeLambda.Alcance anterior = restricciones.entrar(parametros);
        try {
            sintaxis.expresiones().expresion();
        } finally {
            restricciones.salir(anterior);
        }
    }

    // RestoDeParentesis -> , ListaParametrosLambda ) -> CuerpoLambda   [+lambda]
    // ListaParametrosLambda -> idMetVar RestoListaParametrosLambda
    // RestoListaParametrosLambda -> , idMetVar RestoListaParametrosLambda | EPSILON
    private void lambdaDeVariosParametros(Candidato primero)
            throws ExcepcionLexica, ExcepcionSintactica {
        Set<String> parametros = new HashSet<>();
        exigirParametro(primero, parametros);
        while (estaEn(Primeros.RESTO_DE_PARENTESIS_COMA)) {
            match(TipoDeToken.COMA);
            Token siguiente = actual();
            match(TipoDeToken.ID_MV);
            exigirParametro(new Candidato(siguiente, true), parametros);
        }
        match(TipoDeToken.PAREN_C);
        match(TipoDeToken.OP_FLECHA);
        cuerpoLambda(parametros);
    }

    // FinDeParentizadaOLambda -> -> CuerpoLambda   [+lambda]
    // FinDeParentizadaOLambda -> EPSILON
    private void finDeParentizadaOLambda(Candidato primero)
            throws ExcepcionLexica, ExcepcionSintactica {
        if (!hayLogro(Logro.LAMBDA) || !estaEn(Primeros.FIN_DE_PARENTIZADA_OLAMBDA_FLECHA)) {
            return;
        }
        Set<String> parametros = new HashSet<>();
        exigirParametro(primero, parametros);
        match(TipoDeToken.OP_FLECHA);
        cuerpoLambda(parametros);
    }

    // Hasta ver el `->` no se sabe si lo que hay entre paréntesis es el primer
    // parámetro de una lambda o una expresión parentizada, así que se lee como
    // expresión y se recuerda si fue exactamente un idMetVar.
    private Candidato leerCandidatoAParametro() throws ExcepcionLexica, ExcepcionSintactica {
        Token primero = actual();
        int antes = marca();
        sintaxis.expresiones().expresion();
        boolean fueUnIdMetVarPelado =
                primero.tipo() == TipoDeToken.ID_MV && consumioUnSoloTokenDesde(antes);
        return new Candidato(primero, fueUnIdMetVarPelado);
    }

    // Un parámetro de lambda tiene que ser un identificador pelado: `(a) -> a` es
    // una lambda y `(a + 1) -> a` no. La gramática no puede distinguirlos —los dos
    // son una Expresion hasta ver el `->`—, así que lo verifica el analizador.
    private void exigirParametro(Candidato candidato, Set<String> parametros)
            throws ExcepcionSintactica {
        if (!candidato.esIdMetVarPelado()) {
            throw ExcepcionSintactica.porRestriccion(candidato.token(),
                    "un parametro de lambda tiene que ser un identificador");
        }
        if (!parametros.add(candidato.token().lexema())) {
            throw ExcepcionSintactica.porRestriccion(candidato.token(),
                    "el parametro " + candidato.token().lexema() + " esta repetido en la lambda");
        }
    }

    private record Candidato(Token token, boolean esIdMetVarPelado) {
    }
}
