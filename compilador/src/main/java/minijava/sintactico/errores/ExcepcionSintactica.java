package minijava.sintactico.errores;

import minijava.lexico.tokens.Token;

import java.util.List;

// El único error que produce el analizador, en los dos únicos puntos donde el
// Esquema Simple puede fallar (Clase 5, slides 21 y 30): un match que no
// coincide, y un no terminal NO anulable cuyo token actual no está en los
// Primeros de ninguna de sus producciones.
//
// Lleva el token encontrado —de donde salen el lexema y la línea que exige el
// reporte— y la descripción de lo que se esperaba en su lugar.
public final class ExcepcionSintactica extends Exception {

    private final Token encontrado;
    private final String esperado;

    public ExcepcionSintactica(Token encontrado, String esperado) {
        super("Error Sintactico en linea " + encontrado.linea()
                + ": se esperaba " + esperado
                + " se encontro \"" + encontrado.lexema() + "\"");
        this.encontrado = encontrado;
        this.esperado = esperado;
    }

    public ExcepcionSintactica(Token encontrado, List<String> alternativas) {
        this(encontrado, enumerar(alternativas));
    }

    public Token getEncontrado() {
        return encontrado;
    }

    public String getEsperado() {
        return esperado;
    }

    public String getLexemaOfensivo() {
        return encontrado.lexema();
    }

    public int getLinea() {
        return encontrado.linea();
    }

    // "a", "a o b", "a, b o c": el ejemplo de la cátedra escribe las alternativas
    // así, en prosa (Clase 5, slide 30: "un (, id o num").
    private static String enumerar(List<String> alternativas) {
        if (alternativas.isEmpty()) {
            throw new IllegalArgumentException("Un error sintactico siempre espera algo");
        }
        if (alternativas.size() == 1) {
            return alternativas.getFirst();
        }
        String ultimo = alternativas.getLast();
        String previos = String.join(", ", alternativas.subList(0, alternativas.size() - 1));
        return previos + " o " + ultimo;
    }
}
