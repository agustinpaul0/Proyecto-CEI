package minijava.tokens;

// El lexema es SIEMPRE el texto crudo del fuente, nunca el valor decodificado:
// el oráculo de la cátedra espera //#'\n',3, los cuatro caracteres literales.
public record Token(TipoDeToken tipo, String lexema, int linea) {
}
