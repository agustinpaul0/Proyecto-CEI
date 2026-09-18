package minijava.sintactico;

import minijava.lexico.AnalizadorLexico;
import minijava.lexico.errores.ExcepcionLexica;
import minijava.lexico.tokens.TablaDeLexemas;
import minijava.lexico.tokens.TipoDeToken;
import minijava.lexico.tokens.Token;
import minijava.sintactico.errores.ExcepcionSintactica;
import minijava.sintactico.primeros.ConjuntoDePrimeros;

// El lookahead del analizador y el único punto que consume entrada. Separarlo de
// las reglas de la gramática deja a cada analizador de sección sin más estado
// que el que necesita para decidir.
public final class EntradaDeTokens {

    private final AnalizadorLexico analizadorLexico;

    private Token actual;
    private int consumidos;

    public EntradaDeTokens(AnalizadorLexico analizadorLexico) {
        this.analizadorLexico = analizadorLexico;
    }

    public void cargarPrimerToken() throws ExcepcionLexica {
        actual = analizadorLexico.proximoToken();
    }

    public Token actual() {
        return actual;
    }

    // Una marca opaca de posición en la entrada, para poder preguntar después
    // cuánto consumió lo que se acaba de analizar.
    public int marca() {
        return consumidos;
    }

    public boolean estaEn(ConjuntoDePrimeros primeros) {
        return primeros.contiene(actual);
    }

    public void match(TipoDeToken esperado) throws ExcepcionLexica, ExcepcionSintactica {
        if (actual.tipo() != esperado) {
            throw new ExcepcionSintactica(actual, nombreDe(esperado));
        }
        avanzar();
    }

    public ExcepcionSintactica errorEsperando(ConjuntoDePrimeros... alternativas) {
        ConjuntoDePrimeros esperados = ConjuntoDePrimeros.de();
        for (ConjuntoDePrimeros alternativa : alternativas) {
            esperados = esperados.unido(alternativa);
        }
        return new ExcepcionSintactica(actual, esperados.describir());
    }

    // Consume el token actual sin verificar nada. Lo usa el Modo Pánico, que
    // descarta entrada justamente porque no coincide con lo que se esperaba.
    public void descartar() throws ExcepcionLexica {
        avanzar();
    }

    private void avanzar() throws ExcepcionLexica {
        actual = analizadorLexico.proximoToken();
        consumidos++;
    }

    private static String nombreDe(TipoDeToken tipo) {
        return TablaDeLexemas.lexemaFijoDe(tipo).orElseGet(tipo::getNombre);
    }
}
