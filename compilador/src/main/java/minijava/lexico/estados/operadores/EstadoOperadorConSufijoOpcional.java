package minijava.lexico.estados.operadores;

import minijava.lexico.automata.Emitir;
import minijava.lexico.automata.EstadoLexico;
import minijava.lexico.automata.ResultadoDeTransicion;
import minijava.lexico.contexto.ContextoLexico;
import minijava.lexico.tokens.TablaDeLexemas;
import minijava.lexico.tokens.TipoDeToken;

// Los seis pares con sufijo opcional: >/>=, </<=, !/!=, =/==, +/++, -/--.
// Terminal: si el sufijo no viene, emite el token corto sin consumir el lookahead.
//
// Tres instancias y no seis: lo único que distinguía a '>' de '<' era el tipo de
// token, y ahora ése lo da la tabla a partir del lexema armado.
//
// El '-' tiene un segundo sufijo posible, el '>' de la flecha de las lambdas.
// Sin el logro de MiniLambdas ese sufijo no se mira y "->" vuelve a leerse como
// op- seguido de op_mayor, que es lo que hacía la Etapa 1.
public final class EstadoOperadorConSufijoOpcional implements EstadoLexico {

    public static final EstadoOperadorConSufijoOpcional SUFIJO_IGUAL =
            new EstadoOperadorConSufijoOpcional('=');
    public static final EstadoOperadorConSufijoOpcional SUFIJO_MAS =
            new EstadoOperadorConSufijoOpcional('+');
    public static final EstadoOperadorConSufijoOpcional SUFIJO_MENOS =
            new EstadoOperadorConSufijoOpcional('-', TipoDeToken.OP_FLECHA);

    private final char sufijo;

    // El sufijo alternativo y el token que lo justifica, para que la habilitación
    // se consulte por token y no por un booleano suelto. Null si no hay segundo.
    private final TipoDeToken tokenDelSufijoAlternativo;

    private EstadoOperadorConSufijoOpcional(char sufijo) {
        this(sufijo, null);
    }

    private EstadoOperadorConSufijoOpcional(char sufijo, TipoDeToken tokenDelSufijoAlternativo) {
        this.sufijo = sufijo;
        this.tokenDelSufijoAlternativo = tokenDelSufijoAlternativo;
    }

    @Override
    public ResultadoDeTransicion procesar(ContextoLexico contexto) {
        int linea = contexto.getLinea();
        if (contexto.getCaracterActual() == sufijo || esSufijoAlternativo(contexto)) {
            contexto.avanzar();
        }
        return Emitir.porLexemaFijo(contexto.getLexema(), linea);
    }

    private boolean esSufijoAlternativo(ContextoLexico contexto) {
        if (tokenDelSufijoAlternativo == null) {
            return false;
        }
        String conElSufijo = contexto.getLexema() + contexto.getCaracterActual();
        return TablaDeLexemas.lexemaFijoDe(tokenDelSufijoAlternativo).orElseThrow().equals(conElSufijo);
    }
}
