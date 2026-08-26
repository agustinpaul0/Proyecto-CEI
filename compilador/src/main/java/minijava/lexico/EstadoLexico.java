package minijava.lexico;

import minijava.errores.ExcepcionLexica;

// El contrato del patrón State: un componente por cada estado del AF, con un
// único método que decide la transición mirando el carácter actual. Es la
// técnica "Método por Estado" que exige la cátedra.
public interface EstadoLexico {

    ResultadoDeTransicion procesar(ContextoLexico contexto) throws ExcepcionLexica;
}
