package minijava.lexico;

import minijava.errores.ExcepcionLexica;
import minijava.errores.TipoDeErrorLexico;
import minijava.io.ManejadorDeArchivo;

// Punto único de construcción de ExcepcionLexica: ningún estado arma una a
// mano, para que el lexema, la línea, la columna y la línea fuente sean
// siempre consistentes entre sí. Cuál de las cinco variantes corresponde a
// cada error está en agents/manejo-de-errores.md.
public final class FabricaDeExcepcionesLexicas {

    private final BuilderDeLexema lexema;
    private final ManejadorDeArchivo manejador;

    public FabricaDeExcepcionesLexicas(BuilderDeLexema lexema, ManejadorDeArchivo manejador) {
        this.lexema = lexema;
        this.manejador = manejador;
    }

    // Si el lookahead actual es EOF o un salto de línea real, no aparece en
    // getCurrentLineText() (que corta antes del '\n') — reportar su columna
    // dejaría el '^' apuntando después del último carácter imprimible de la
    // línea, sin nada debajo. En ese caso se usa la posición del último
    // carácter realmente consumido en su lugar.
    public ExcepcionLexica crearExcepcionLexica(TipoDeErrorLexico tipo) {
        boolean lookaheadFueraDeLaLineaImpresa =
                manejador.estaEnFinDeArchivo() || lexema.getCaracterActual() == '\n';
        if (lookaheadFueraDeLaLineaImpresa && lexema.posicionUltimoConsumido() != null) {
            return crearExcepcionLexica(tipo, lexema.getLexema(), lexema.posicionUltimoConsumido());
        }
        return new ExcepcionLexica(tipo, escaparNoImprimibles(lexema.getLexema()),
                lexema.getLinea(), manejador.getColumnNumber(), manejador.getCurrentLineText());
    }

    // Para los estados que SÍ consumen el carácter ofensivo antes de lanzar.
    // Tras el avanzar(), getColumnNumber() ya apunta al carácter siguiente,
    // así que la posición correcta es la del último consumido.
    public ExcepcionLexica crearExcepcionLexicaSobreElUltimoConsumido(TipoDeErrorLexico tipo) {
        return crearExcepcionLexica(tipo, lexema.getLexema(), lexema.posicionUltimoConsumido());
    }

    // Ídem, nombrando además el fragmento concreto que está mal dentro del
    // lexema armado. El fragmento se escapa igual que el lexema: también se
    // imprime.
    public ExcepcionLexica crearExcepcionLexicaSobreElUltimoConsumido(TipoDeErrorLexico tipo,
                                                                     String fragmentoOfensivo) {
        PosicionLexica posicion = lexema.posicionUltimoConsumido();
        return new ExcepcionLexica(tipo, escaparNoImprimibles(lexema.getLexema()),
                posicion.linea(), posicion.columna(), posicion.lineaFuente(),
                escaparNoImprimibles(fragmentoOfensivo));
    }

    // Para los errores que hablan de la CONSTRUCCIÓN ENTERA y no de un
    // carácter puntual. El '^' va donde la construcción empieza, que es lo que
    // hace javac — verificado ejecutándolo.
    public ExcepcionLexica crearExcepcionLexicaSobreElInicioDelLexema(TipoDeErrorLexico tipo) {
        if (lexema.posicionPrimerConsumido() == null) {
            return crearExcepcionLexica(tipo);
        }
        return crearExcepcionLexica(tipo, lexema.getLexema(), lexema.posicionPrimerConsumido());
    }

    // Para errores sobre una posición que ningún estado puede reconstruir en
    // el momento del throw: la apertura de una construcción que nunca cerró y
    // cuyo lexema ya fue descartado (comentario multilínea).
    public ExcepcionLexica crearExcepcionLexica(TipoDeErrorLexico tipo, String lexemaOfensivo,
                                                PosicionLexica posicion) {
        return new ExcepcionLexica(tipo, escaparNoImprimibles(lexemaOfensivo),
                posicion.linea(), posicion.columna(), posicion.lineaFuente());
    }

    // El lexema de un error se imprime literal en el encabezado legible y
    // dentro de [Error:Lexema|NroLinea]. Un carácter invisible ahí deja el
    // mensaje ilegible y, si es un salto de línea, además parte el código de
    // error en dos y rompe el contrato de la cátedra de un elemento por línea.
    // Se renderizan con su escape de Java, igual que hace javac, que para un
    // espacio no rompible reporta literalmente `illegal character: ' '`.
    //
    // Vale SÓLO para el lexema de un error. El de un token se imprime crudo,
    // porque el contrato de la cátedra es que sea el texto exacto del fuente.
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

    // Invisible = no se ve nada al imprimirlo, o directamente no se puede
    // imprimir. Cubre los controles C0/DEL, los caracteres de formato (el BOM
    // entre ellos), los separadores de espacio que no son el espacio común, y
    // los surrogados SUELTOS. Un par surrogado bien formado (un emoji) se deja
    // crudo: se ve perfecto y escaparlo sólo haría el mensaje más difícil de
    // leer.
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
