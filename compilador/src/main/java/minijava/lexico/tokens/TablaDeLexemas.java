package minijava.lexico.tokens;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

// 49 de los 56 tokens tienen lexema FIJO: el tipo y el lexema se determinan
// mutuamente. Los 7 restantes —los identificadores y los cuatro literales— leen
// su lexema del fuente y no entran acá.
//
// El reparto de trabajo que habilita esta tabla: el autómata delimita el lexema
// (maximal munch, lo único que la tabla no puede decidir: no distingue '>' de
// '>='), y la tabla lo nombra. Es la técnica que la cátedra enseña para las
// palabras clave (Clase 02, slides 62-63), generalizada a los 49.
public final class TablaDeLexemas {

    private static final Set<String> PALABRAS_CLAVE;
    private static final Map<String, TipoDeToken> POR_LEXEMA;
    private static final Map<TipoDeToken, String> POR_TIPO;

    static {
        Map<String, TipoDeToken> palabrasClave = new LinkedHashMap<>();
        palabrasClave.put("class", TipoDeToken.PR_CLASS);
        palabrasClave.put("extends", TipoDeToken.PR_EXTENDS);
        palabrasClave.put("interface", TipoDeToken.PR_INTERFACE);
        palabrasClave.put("implements", TipoDeToken.PR_IMPLEMENTS);
        palabrasClave.put("static", TipoDeToken.PR_STATIC);
        palabrasClave.put("boolean", TipoDeToken.PR_BOOLEAN);
        palabrasClave.put("char", TipoDeToken.PR_CHAR);
        palabrasClave.put("int", TipoDeToken.PR_INT);
        palabrasClave.put("void", TipoDeToken.PR_VOID);
        palabrasClave.put("public", TipoDeToken.PR_PUBLIC);
        palabrasClave.put("if", TipoDeToken.PR_IF);
        palabrasClave.put("else", TipoDeToken.PR_ELSE);
        palabrasClave.put("while", TipoDeToken.PR_WHILE);
        palabrasClave.put("return", TipoDeToken.PR_RETURN);
        palabrasClave.put("var", TipoDeToken.PR_VAR);
        palabrasClave.put("this", TipoDeToken.PR_THIS);
        palabrasClave.put("new", TipoDeToken.PR_NEW);
        palabrasClave.put("null", TipoDeToken.PR_NULL);
        palabrasClave.put("true", TipoDeToken.PR_TRUE);
        palabrasClave.put("false", TipoDeToken.PR_FALSE);
        // No figura en Aspectos Léxicos: se agrega por confirmación directa del
        // profesor. 'double' NO es reservada (decisiones.md §3.1).
        palabrasClave.put("float", TipoDeToken.PR_FLOAT);

        Map<String, TipoDeToken> porLexema = new LinkedHashMap<>(palabrasClave);

        porLexema.put("(", TipoDeToken.PAREN_A);
        porLexema.put(")", TipoDeToken.PAREN_C);
        porLexema.put("{", TipoDeToken.LLAVE_A);
        porLexema.put("}", TipoDeToken.LLAVE_C);
        porLexema.put("[", TipoDeToken.CORCHETE_A);
        porLexema.put("]", TipoDeToken.CORCHETE_C);
        porLexema.put(";", TipoDeToken.PUNTO_Y_COMA);
        porLexema.put(",", TipoDeToken.COMA);
        porLexema.put(".", TipoDeToken.PUNTO);
        porLexema.put(":", TipoDeToken.DOS_PUNTOS);

        porLexema.put(">", TipoDeToken.OP_MAYOR);
        porLexema.put("<", TipoDeToken.OP_MENOR);
        porLexema.put("!", TipoDeToken.OP_NEGACION);
        porLexema.put("=", TipoDeToken.OP_ASIGNACION);
        porLexema.put("==", TipoDeToken.OP_IGUAL);
        porLexema.put(">=", TipoDeToken.OP_MAYOR_IGUAL);
        porLexema.put("<=", TipoDeToken.OP_MENOR_IGUAL);
        porLexema.put("!=", TipoDeToken.OP_DISTINTO);
        porLexema.put("&&", TipoDeToken.OP_AND);
        porLexema.put("||", TipoDeToken.OP_OR);
        porLexema.put("%", TipoDeToken.OP_MODULO);
        porLexema.put("+", TipoDeToken.OP_SUMA);
        porLexema.put("-", TipoDeToken.OP_RESTA);
        porLexema.put("*", TipoDeToken.OP_MULT);
        porLexema.put("/", TipoDeToken.OP_DIV);
        porLexema.put("++", TipoDeToken.OP_INCREMENTO);
        porLexema.put("--", TipoDeToken.OP_DECREMENTO);

        // Aspectos Léxicos §2.5: "El End of File es un token mas, su y lexema es $".
        // No es un carácter del fuente ni el centinela de fin de archivo: un '$'
        // escrito en el fuente es un símbolo inválido (§3.3).
        porLexema.put("$", TipoDeToken.EOF);

        Map<TipoDeToken, String> porTipo = new LinkedHashMap<>();
        porLexema.forEach((lexema, tipo) -> porTipo.put(tipo, lexema));

        PALABRAS_CLAVE = Collections.unmodifiableSet(palabrasClave.keySet());
        POR_LEXEMA = Collections.unmodifiableMap(porLexema);
        POR_TIPO = Collections.unmodifiableMap(porTipo);
    }

    private TablaDeLexemas() {
    }

    // Vacío si el lexema no es de los fijos: así lo consulta el estado de los
    // identificadores, donde "no está" significa idMV.
    public static Optional<TipoDeToken> buscar(String lexema) {
        return Optional.ofNullable(POR_LEXEMA.get(lexema));
    }

    // Para los estados que YA saben que armaron un lexema fijo. Que falle no es
    // una entrada inválida, es un defecto del compilador.
    public static TipoDeToken tipoDelLexemaFijo(String lexema) {
        return buscar(lexema).orElseThrow(() -> new IllegalStateException(
                "No hay tipo de token para el lexema fijo: " + lexema));
    }

    public static Optional<String> lexemaFijoDe(TipoDeToken tipo) {
        return Optional.ofNullable(POR_TIPO.get(tipo));
    }

    public static Set<String> palabrasClave() {
        return PALABRAS_CLAVE;
    }

    public static Set<String> lexemasFijos() {
        return POR_LEXEMA.keySet();
    }
}
