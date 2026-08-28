package minijava.lexico.contexto;

// Dónde cae el '^' del reporte, que es lo único que distingue entre sí a las
// formas de construir una ExcepcionLexica. El criterio es el de javac, verificado
// ejecutándolo: si el mensaje habla de un carácter ofensivo puntual el '^' va a
// ese carácter; si habla de la construcción entera, a donde ésta empieza.
// Tabla completa de qué error usa cuál: agents/manejo-de-errores.md.
public enum AnclaDeError {

    // El ofensivo es el lookahead y todavía no se consumió.
    LOOKAHEAD,

    // El ofensivo ya se consumió y ES el problema.
    ULTIMO_CONSUMIDO,

    // El mensaje habla de la construcción entera: va a su primer carácter.
    INICIO_DEL_LEXEMA
}
