package minijava.sintactico.partes;

import minijava.comun.config.Logro;
import minijava.sintactico.EntradaDeTokens;
import minijava.sintactico.Sintaxis;
import minijava.sintactico.primeros.ConjuntoDePrimeros;

// Lo que toda sección de la gramática necesita: la entrada, los logros y el
// acceso a las demás secciones. No aporta ninguna regla propia.
public abstract class ParteDelAnalizador {

    protected final Sintaxis sintaxis;

    protected ParteDelAnalizador(Sintaxis sintaxis) {
        this.sintaxis = sintaxis;
    }

    protected EntradaDeTokens entrada() {
        return sintaxis.entrada();
    }

    protected boolean estaEn(ConjuntoDePrimeros primeros) {
        return sintaxis.entrada().estaEn(primeros);
    }

    protected boolean hayLogro(Logro logro) {
        return sintaxis.configuracion().tiene(logro);
    }
}
