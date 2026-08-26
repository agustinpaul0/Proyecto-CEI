package minijava.lexico;

// Una foto de dónde estaba un carácter: línea, columna y el texto completo de
// su línea fuente. Los tres datos viajan juntos a propósito — mezclar la línea
// de un carácter con la línea-fuente de otro fue un bug real, y es lo que hace
// que el '^' termine señalando texto que no tiene nada que ver con el error.
//
// ContextoLexico captura estas fotos dentro de avanzar(), ANTES de consumir,
// que es el único instante en que los tres datos describen ese carácter.
public record PosicionLexica(int linea, int columna, String lineaFuente) {
}
