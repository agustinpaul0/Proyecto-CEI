package minijava.lexico.automata;

import minijava.errores.ExcepcionLexica;
import minijava.lexico.contexto.ContextoLexico;

// Un componente por estado del AF: la técnica "Método por Estado" que exige la cátedra.
public interface EstadoLexico {

    ResultadoDeTransicion procesar(ContextoLexico contexto) throws ExcepcionLexica;
}
