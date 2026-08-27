package minijava.lexico.estados;

import minijava.lexico.ContextoLexico;
import minijava.lexico.Continuar;
import minijava.lexico.Emitir;
import minijava.lexico.EstadoLexico;
import minijava.lexico.ResultadoDeTransicion;
import minijava.lexico.TablaDePalabrasClave;
import minijava.tokens.Token;
import minijava.tokens.TipoDeToken;

// Terminal: idMV, o la palabra clave si el lexema está en la tabla de lookup.
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
        TipoDeToken tipo = TablaDePalabrasClave.buscar(lexema).orElse(TipoDeToken.ID_MV);
        return new Emitir(new Token(tipo, lexema, contexto.getLinea()));
    }

    static boolean esAlfanumericoOGuion(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }
}
