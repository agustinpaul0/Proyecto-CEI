package minijava.lexico.contexto;

import minijava.errores.PosicionLexica;
import minijava.io.ManejadorDeArchivo;

import java.io.IOException;
import java.io.UncheckedIOException;

// El lexema en curso, el lookahead y las dos fotos de posición: un solo objeto
// porque todo eso cambia junto al consumir un carácter.
public final class BuilderDeLexema {

    private final ManejadorDeArchivo manejador;
    private final StringBuilder lexema = new StringBuilder();
    private char caracterActual;
    // La usan los errores que hablan de la construcción entera.
    private PosicionLexica posicionPrimerConsumido;
    // No el lookahead: la usan los errores cuyo ofensivo ya se consumió.
    private PosicionLexica posicionUltimoConsumido;

    public BuilderDeLexema(ManejadorDeArchivo manejador) {
        this.manejador = manejador;
        this.caracterActual = leerSiguienteCaracter();
    }

    public char getCaracterActual() {
        return caracterActual;
    }

    public int getLinea() {
        return manejador.getLineNumber();
    }

    public void avanzar() {
        // Los tres del mismo origen y del mismo instante: describen el lookahead,
        // que es el carácter que el manejador devolvió último.
        PosicionLexica posicion = new PosicionLexica(manejador.getLineNumber(),
                manejador.getColumnNumber(), manejador.getCurrentLineText());
        if (lexema.isEmpty()) {
            posicionPrimerConsumido = posicion;
        }
        lexema.append(caracterActual);
        posicionUltimoConsumido = posicion;
        caracterActual = leerSiguienteCaracter();
    }

    public void descartar() {
        caracterActual = leerSiguienteCaracter();
    }

    // EstadoBarra la llama a mitad de token: por eso el reset vive acá.
    public void iniciarNuevoToken() {
        lexema.setLength(0);
        posicionPrimerConsumido = null;
    }

    public String getLexema() {
        return lexema.toString();
    }

    public PosicionLexica posicionPrimerConsumido() {
        return posicionPrimerConsumido;
    }

    // Resolver el ancla vive acá y no en la fábrica porque es acá donde están las
    // dos fotos y el lookahead. Las dos degradaciones son deliberadas: un lexema
    // todavía vacío no tiene inicio, y un lookahead que la línea impresa no
    // contiene dejaría el '^' colgando en el aire después de su último carácter.
    public PosicionLexica posicionDe(AnclaDeError ancla) {
        return switch (ancla) {
            case ULTIMO_CONSUMIDO -> posicionUltimoConsumido;
            case INICIO_DEL_LEXEMA -> posicionPrimerConsumido != null
                    ? posicionPrimerConsumido
                    : posicionDe(AnclaDeError.LOOKAHEAD);
            case LOOKAHEAD -> fueraDeLaLineaImpresa() && posicionUltimoConsumido != null
                    ? posicionUltimoConsumido
                    : new PosicionLexica(manejador.getLineNumber(), manejador.getColumnNumber(),
                            manejador.getCurrentLineText());
        };
    }

    // getCurrentLineText() corta antes del '\n', y en el EOF no hay carácter que
    // señalar: ninguno de los dos aparece en la línea que imprime el reporte.
    private boolean fueraDeLaLineaImpresa() {
        return manejador.estaEnFinDeArchivo() || caracterActual == '\n';
    }

    private char leerSiguienteCaracter() {
        try {
            return manejador.getNextChar();
        } catch (IOException excepcion) {
            throw new UncheckedIOException(excepcion);
        }
    }
}
