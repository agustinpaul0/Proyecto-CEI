package minijava.lexico;

import minijava.errores.ExcepcionLexica;

public interface EstadoLexico {

    ResultadoDeTransicion procesar(ContextoLexico contexto) throws ExcepcionLexica;
}
