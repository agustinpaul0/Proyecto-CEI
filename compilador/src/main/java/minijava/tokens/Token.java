package minijava.tokens;

// El lexema es SIEMPRE el texto crudo del fuente, nunca el valor decodificado:
// para un charLiteral con un salto de línea escapado se reporta '\n' (cuatro
// caracteres literales), no el salto. Lo confirma la guía de uso de los testers
// de la cátedra, cuyo oráculo espera //#'\n',3.
//
// El valor decodificado todavía no se calcula ni se guarda: Etapa 1 no lo
// necesita. Cuando una etapa posterior lo pida, será un campo aparte — no
// reemplaza al lexema.
public record Token(TipoDeToken tipo, String lexema, int linea) {
}
