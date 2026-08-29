package minijava.lexico.estados.identificadores;

import minijava.lexico.automata.Continuar;
import minijava.lexico.automata.Emitir;
import minijava.lexico.automata.EstadoLexico;
import minijava.lexico.automata.ResultadoDeTransicion;
import minijava.lexico.contexto.ContextoLexico;
import minijava.tokens.TablaDeLexemas;
import minijava.tokens.TipoDeToken;
import minijava.tokens.Token;

// Terminal: idMV, o la palabra clave si el lexema figura en la tabla de lexemas.
// Consultarla entera es seguro: un lexema de identificador nunca puede ser "(" ni ">=".
public final class EstadoIdMV implements EstadoLexico {

    public static final EstadoIdMV INSTANCIA = new EstadoIdMV();

    private EstadoIdMV() {
    }

    @Override
    public ResultadoDeTransicion procesar(ContextoLexico contexto) {
        if (esAlfanumericoOGuion(contexto.getCaracterActual())) {
            contexto.avanzar();
            return new Continuar(INSTANCIA);
        }
        String lexema = contexto.getLexema();
        TipoDeToken tipo = TablaDeLexemas.buscar(lexema).orElse(TipoDeToken.ID_MV);
        return new Emitir(new Token(tipo, lexema, contexto.getLinea()));
    }

    static boolean esAlfanumericoOGuion(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }
}
