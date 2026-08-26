package minijava.lexico;

import minijava.config.ConfiguracionDelCompilador;
import minijava.errores.ExcepcionLexica;
import minijava.errores.TipoDeErrorLexico;
import minijava.io.ManejadorDeArchivo;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

public final class ContextoLexico {

    private final ManejadorDeArchivo manejador;
    private final ConfiguracionDelCompilador configuracion;
    private final StringBuilder lexema = new StringBuilder();
    private char caracterActual;
    // Línea propia, no delegada a manejador.getLineNumber(): esa cuenta se
    // adelanta en el momento en que un '\n' es DEVUELTO como lookahead, no
    // cuando efectivamente se lo consume. Acá se incrementa recién al
    // consumir (avanzar/descartar) el '\n', para que un estado que todavía
    // está MIRANDO el '\n' (sin haberlo consumido) reporte la línea que ese
    // salto termina, no la siguiente.
    private int linea = 1;
    // Posición del último carácter agregado al lexema (no del lookahead
    // actual, que puede ya haber avanzado más allá). Se actualiza dentro de
    // avanzar(), justo antes de consumir, cuando línea/columna/línea-fuente
    // todavía describen ese carácter. La usan los estados cuyo error no es
    // "este carácter que estoy mirando es inválido" sino "el lexema que ya
    // terminé de construir resultó inválido" (p. ej. un operador con
    // segundo carácter obligatorio ausente, o un entero de más de 9
    // dígitos): en esos casos el lookahead actual no tiene ninguna relación
    // con el motivo del error, así que reportarlo sería engañoso.
    private PosicionLexica posicionUltimoConsumido;
    // Errores que NO abortan el token en curso (ver registrarError).
    private final List<ExcepcionLexica> erroresNoFatales = new ArrayList<>();

    public ContextoLexico(ManejadorDeArchivo manejador, ConfiguracionDelCompilador configuracion) {
        this.manejador = manejador;
        this.configuracion = configuracion;
        this.caracterActual = leerSiguienteCaracter();
    }

    // Los estados la consultan para los dos logros que cambian el AF en sí
    // (Floats I y Unicodes); los otros dos flags viven fuera del léxico.
    public ConfiguracionDelCompilador getConfiguracion() {
        return configuracion;
    }

    // ManejadorDeArchivoEficiente nunca lanza IOException en la práctica;
    // se adapta acá para no propagar el checked exception de la interfaz.
    private char leerSiguienteCaracter() {
        try {
            return manejador.getNextChar();
        } catch (IOException excepcion) {
            throw new UncheckedIOException(excepcion);
        }
    }

    public char getCaracterActual() {
        return caracterActual;
    }

    // Por posición real, no comparando contra el centinela: un 0x1A
    // embebido en el fuente es un carácter común (y un error léxico), no el
    // fin del archivo. Ver ManejadorDeArchivo.estaEnFinDeArchivo().
    public boolean esFinDeArchivo() {
        return manejador.estaEnFinDeArchivo();
    }

    public void avanzar() {
        lexema.append(caracterActual);
        posicionUltimoConsumido = new PosicionLexica(getLinea(), getColumna(), getLineaFuente());
        consumirCaracterActual();
    }

    public PosicionLexica posicionUltimoConsumido() {
        return posicionUltimoConsumido;
    }

    public void descartar() {
        consumirCaracterActual();
    }

    private void consumirCaracterActual() {
        if (caracterActual == '\n') {
            linea++;
        }
        caracterActual = leerSiguienteCaracter();
    }

    // Un error no fatal se reporta pero no interrumpe el reconocimiento: el
    // token se termina de armar y se emite igual. Hoy lo usa un solo caso, el
    // escape inválido dentro de un litString, replicando a javac ("illegal
    // escape character" no aborta el literal — verificado ejecutándolo).
    public void registrarError(ExcepcionLexica error) {
        erroresNoFatales.add(error);
    }

    public List<ExcepcionLexica> tomarErroresNoFatales() {
        List<ExcepcionLexica> tomados = List.copyOf(erroresNoFatales);
        erroresNoFatales.clear();
        return tomados;
    }

    public void iniciarNuevoToken() {
        lexema.setLength(0);
    }

    public String getLexema() {
        return lexema.toString();
    }

    public int getLinea() {
        return linea;
    }

    public int getColumna() {
        return manejador.getColumnNumber();
    }

    public String getLineaFuente() {
        return manejador.getCurrentLineText();
    }

    // Si el lookahead actual es EOF o un salto de línea real, no aparece en
    // getLineaFuente() (que corta antes del '\n') — reportar su columna
    // dejaría el '^' apuntando después del último carácter imprimible de la
    // línea, sin nada debajo. En ese caso se usa la posición del último
    // carácter realmente consumido en su lugar.
    public ExcepcionLexica crearExcepcionLexica(TipoDeErrorLexico tipo) {
        if ((esFinDeArchivo() || caracterActual == '\n') && posicionUltimoConsumido != null) {
            return crearExcepcionLexica(tipo, getLexema(), posicionUltimoConsumido);
        }
        return new ExcepcionLexica(tipo, escaparNoImprimibles(getLexema()),
                getLinea(), getColumna(), getLineaFuente());
    }

    // Variante para los estados que SÍ consumen el carácter ofensivo antes de
    // lanzar (criterio de recuperación: se consume exactamente lo que se
    // reporta). Tras el avanzar(), getColumna() ya apunta al carácter
    // siguiente, así que la posición correcta es la del último consumido.
    public ExcepcionLexica crearExcepcionLexicaSobreElUltimoConsumido(TipoDeErrorLexico tipo) {
        return crearExcepcionLexica(tipo, getLexema(), posicionUltimoConsumido);
    }

    // Variante para errores sobre una posición distinta de la del lookahead
    // actual: el lexema ya construido resultó inválido como un todo (ver
    // posicionUltimoConsumido()), o el error es sobre la posición de
    // APERTURA de una construcción que nunca cerró (comentario multilínea).
    public ExcepcionLexica crearExcepcionLexica(TipoDeErrorLexico tipo, String lexema,
                                                PosicionLexica posicion) {
        return new ExcepcionLexica(tipo, escaparNoImprimibles(lexema),
                posicion.linea(), posicion.columna(), posicion.lineaFuente());
    }

    // El lexema de un error se imprime literal en el encabezado legible y
    // dentro de [Error:Lexema|NroLinea]. Un carácter invisible ahí deja el
    // mensaje ilegible ("  no es un símbolo válido") y, si es un salto de
    // línea, además parte el código de error en dos y rompe el contrato de la
    // cátedra de un elemento por línea.
    //
    // Se renderizan con su escape de Java, igual que hace javac, que para un
    // espacio no rompible reporta literalmente:
    //
    //     error: illegal character: '\u00a0'
    //
    // Esto vale SÓLO para el lexema de un error. El de un token se imprime
    // crudo, porque el contrato de la cátedra es que sea el texto exacto del
    // fuente (ver resources/sinErrores/stringConTabuladorInterior.java).
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
    // entre ellos), los separadores de espacio que no son el espacio común
    // (el no rompible entre ellos) y los surrogados SUELTOS. Un par surrogado
    // bien formado (un emoji) se deja crudo: se ve perfecto y escaparlo sólo
    // haría el mensaje más difícil de leer.
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
