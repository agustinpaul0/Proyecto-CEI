package minijava.lexico.errores;

// Los tres datos viajan juntos a propósito: mezclar la línea de un carácter
// con la línea-fuente de otro deja el '^' señalando texto ajeno al error.
public record PosicionLexica(int linea, int columna, String lineaFuente) {
}
