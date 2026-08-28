package minijava.tokens;

import minijava.tokens.TipoDeToken;

import java.util.Map;
import java.util.Optional;

// Las 21 reservadas se reconocen como identificador y se consultan acá antes de
// emitir: la técnica de la cátedra (Clase 02, slides 62-63).
public final class TablaDePalabrasClave {

    private static final Map<String, TipoDeToken> PALABRAS = Map.ofEntries(
            Map.entry("class", TipoDeToken.PR_CLASS),
            Map.entry("extends", TipoDeToken.PR_EXTENDS),
            Map.entry("interface", TipoDeToken.PR_INTERFACE),
            Map.entry("implements", TipoDeToken.PR_IMPLEMENTS),
            Map.entry("static", TipoDeToken.PR_STATIC),
            Map.entry("boolean", TipoDeToken.PR_BOOLEAN),
            Map.entry("char", TipoDeToken.PR_CHAR),
            Map.entry("int", TipoDeToken.PR_INT),
            Map.entry("void", TipoDeToken.PR_VOID),
            Map.entry("public", TipoDeToken.PR_PUBLIC),
            Map.entry("if", TipoDeToken.PR_IF),
            Map.entry("else", TipoDeToken.PR_ELSE),
            Map.entry("while", TipoDeToken.PR_WHILE),
            Map.entry("return", TipoDeToken.PR_RETURN),
            Map.entry("var", TipoDeToken.PR_VAR),
            Map.entry("this", TipoDeToken.PR_THIS),
            Map.entry("new", TipoDeToken.PR_NEW),
            Map.entry("null", TipoDeToken.PR_NULL),
            Map.entry("true", TipoDeToken.PR_TRUE),
            Map.entry("false", TipoDeToken.PR_FALSE),
            Map.entry("float", TipoDeToken.PR_FLOAT));

    private TablaDePalabrasClave() {
    }

    public static Optional<TipoDeToken> buscar(String lexema) {
        return Optional.ofNullable(PALABRAS.get(lexema));
    }
}
