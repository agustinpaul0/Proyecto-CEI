package minijava.lexico;

import minijava.errores.ExcepcionLexica;

// Un componente por estado del AF: la técnica "Método por Estado" que exige la cátedra.
public interface EstadoLexico {

    ResultadoDeTransicion procesar(ContextoLexico contexto) throws ExcepcionLexica;
}
