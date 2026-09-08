package minijava.sintactico.partes;

import minijava.comun.config.Logro;
import minijava.lexico.errores.ExcepcionLexica;
import minijava.lexico.tokens.TipoDeToken;
import minijava.lexico.tokens.Token;
import minijava.sintactico.Sintaxis;
import minijava.sintactico.errores.ExcepcionSintactica;
import minijava.sintactico.primeros.ConjuntoDePrimeros;

// Lo que toda sección de la gramática necesita: el token actual, el match, los
// logros y el acceso a las demás secciones. No aporta ninguna regla propia.
//
// Los métodos de entrada se delegan uno a uno en vez de exponer la
// EntradaDeTokens, para que cada método de no terminal se lea como el esquema de
// Clase 5 (slide 57): `match(x)` para los terminales y una llamada para los no
// terminales. Quién consume la entrada sigue siendo EntradaDeTokens y nadie más.
public abstract class ParteDelAnalizador {

    protected final Sintaxis sintaxis;

    protected ParteDelAnalizador(Sintaxis sintaxis) {
        this.sintaxis = sintaxis;
    }

    protected final void match(TipoDeToken esperado) throws ExcepcionLexica, ExcepcionSintactica {
        sintaxis.entrada().match(esperado);
    }

    protected final void matchDelActual() throws ExcepcionLexica, ExcepcionSintactica {
        sintaxis.entrada().matchDelActual();
    }

    protected final ExcepcionSintactica errorEsperando(ConjuntoDePrimeros... alternativas) {
        return sintaxis.entrada().errorEsperando(alternativas);
    }

    protected final Token actual() {
        return sintaxis.entrada().actual();
    }

    protected final int marca() {
        return sintaxis.entrada().marca();
    }

    protected final boolean consumioUnSoloTokenDesde(int marca) {
        return sintaxis.entrada().consumioUnSoloTokenDesde(marca);
    }

    protected final boolean estaEn(ConjuntoDePrimeros primeros) {
        return sintaxis.entrada().estaEn(primeros);
    }

    protected final boolean hayLogro(Logro logro) {
        return sintaxis.configuracion().tiene(logro);
    }
}
