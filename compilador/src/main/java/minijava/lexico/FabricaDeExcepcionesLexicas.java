package minijava.lexico;

import minijava.errores.ExcepcionLexica;
import minijava.errores.TipoDeErrorLexico;
import minijava.io.ManejadorDeArchivo;

// Punto único de construcción de ExcepcionLexica, para que lexema, línea, columna
// y línea fuente sean consistentes. Qué variante usa cada error: agents/manejo-de-errores.md.
public final class FabricaDeExcepcionesLexicas {

    private final BuilderDeLexema lexema;
    private final ManejadorDeArchivo manejador;

    public FabricaDeExcepcionesLexicas(BuilderDeLexema lexema, ManejadorDeArchivo manejador) {
        this.lexema = lexema;
        this.manejador = manejador;
    }

    // Un lookahead EOF o '\n' no aparece en getCurrentLineText(), y reportar su
    // columna dejaría el '^' colgando después del final de la línea impresa.
    public ExcepcionLexica crearExcepcionLexica(TipoDeErrorLexico tipo) {
        boolean lookaheadFueraDeLaLineaImpresa =
                manejador.estaEnFinDeArchivo() || lexema.getCaracterActual() == '\n';
        if (lookaheadFueraDeLaLineaImpresa && lexema.posicionUltimoConsumido() != null) {
            return crearExcepcionLexica(tipo, lexema.getLexema(), lexema.posicionUltimoConsumido());
        }
        return new ExcepcionLexica(tipo, escaparNoImprimibles(lexema.getLexema()),
                lexema.getLinea(), manejador.getColumnNumber(), manejador.getCurrentLineText());
    }

    // Tras el avanzar() getColumnNumber() ya apunta al siguiente, no al ofensivo.
    public ExcepcionLexica crearExcepcionLexicaSobreElUltimoConsumido(TipoDeErrorLexico tipo) {
        return crearExcepcionLexica(tipo, lexema.getLexema(), lexema.posicionUltimoConsumido());
    }

    // Ídem, nombrando el fragmento que está mal. Se escapa: también se imprime.
    public ExcepcionLexica crearExcepcionLexicaSobreElUltimoConsumido(TipoDeErrorLexico tipo,
                                                                     String fragmentoOfensivo) {
        PosicionLexica posicion = lexema.posicionUltimoConsumido();
        return new ExcepcionLexica(tipo, escaparNoImprimibles(lexema.getLexema()),
                posicion.linea(), posicion.columna(), posicion.lineaFuente(),
                escaparNoImprimibles(fragmentoOfensivo));
    }

    // Para los errores que hablan de la construcción entera: el '^' va donde empieza.
    public ExcepcionLexica crearExcepcionLexicaSobreElInicioDelLexema(TipoDeErrorLexico tipo) {
        if (lexema.posicionPrimerConsumido() == null) {
            return crearExcepcionLexica(tipo);
        }
        return crearExcepcionLexica(tipo, lexema.getLexema(), lexema.posicionPrimerConsumido());
    }

    // Para una posición irreconstruible en el throw: la apertura de un comentario
    // multilínea, cuyo lexema ya se descartó.
    public ExcepcionLexica crearExcepcionLexica(TipoDeErrorLexico tipo, String lexemaOfensivo,
                                                PosicionLexica posicion) {
        return new ExcepcionLexica(tipo, escaparNoImprimibles(lexemaOfensivo),
                posicion.linea(), posicion.columna(), posicion.lineaFuente());
    }

    // Un invisible en el lexema deja el mensaje ilegible —o, si es un salto, parte
    // [Error:Lexema|NroLinea] en dos—. Sólo para errores: el de un token va crudo.
    private static String escaparNoImprimibles(String texto) {
        StringBuilder escapado = new StringBuilder(texto.length());
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            switch (c) {
                case '\t' -> escapado.append("\\t");
                case '\n' -> escapado.append("\\n");
                case '\r' -> escapado.append("\\r");
                case '\f' -> escapado.append("\\f");
                case '\b' -> escapado.append("\\b");
                default -> {
                    if (esInvisible(texto, i)) {
                        escapado.append("\\u").append(String.format("%04x", (int) c));
                    } else {
                        escapado.append(c);
                    }
                }
            }
        }
        return escapado.toString();
    }

    // Controles, formato (el BOM), espacios que no son el común, y surrogados
    // SUELTOS. Un par bien formado —un emoji— se deja crudo: se ve.
    private static boolean esInvisible(String texto, int posicion) {
        char c = texto.charAt(posicion);
        if (Character.isISOControl(c)) {
            return true;
        }
        if (Character.isHighSurrogate(c)) {
            return posicion + 1 >= texto.length() || !Character.isLowSurrogate(texto.charAt(posicion + 1));
        }
        if (Character.isLowSurrogate(c)) {
            return posicion == 0 || !Character.isHighSurrogate(texto.charAt(posicion - 1));
        }
        if (Character.isSpaceChar(c)) {
            return c != ' ';
        }
        return Character.getType(c) == Character.FORMAT;
    }
}
