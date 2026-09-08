package minijava.sintactico.partes;

import minijava.comun.config.Logro;
import minijava.lexico.errores.ExcepcionLexica;
import minijava.lexico.tokens.TipoDeToken;
import minijava.lexico.tokens.Token;
import minijava.sintactico.Sintaxis;
import minijava.sintactico.errores.ExcepcionSintactica;
import minijava.sintactico.primeros.ConjuntoDePrimeros;
import minijava.sintactico.recuperacion.ContextoDeSincronizacion;
import minijava.sintactico.recuperacion.RecuperadorDePanico;

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

    // Corre un elemento de una lista y, si falla, o deja subir el error (sin el
    // logro: cortar en el primero, Pautas slide 25) o lo registra y sincroniza
    // para seguir con el elemento siguiente. Devuelve si la lista puede seguir.
    protected final boolean reanudaTrasError(ContextoDeSincronizacion contexto,
                                             ElementoDeLista elemento)
            throws ExcepcionLexica, ExcepcionSintactica {
        RecuperadorDePanico recuperador = sintaxis.recuperador();
        int antes = marca();
        try {
            elemento.analizar();
            return true;
        } catch (ExcepcionSintactica error) {
            if (!recuperador.habilitado()) {
                throw error;
            }
            recuperador.registrar(error);
            boolean sigue = recuperador.sincronizar(contexto);
            // Invariante del bucle: cada vuelta consume al menos un token, o el
            // analizador no terminaría nunca.
            if (sigue && marca() == antes) {
                recuperador.forzarProgreso();
            }
            return sigue;
        }
    }

    @FunctionalInterface
    protected interface ElementoDeLista {
        void analizar() throws ExcepcionLexica, ExcepcionSintactica;
    }

    protected final boolean hayLogro(Logro logro) {
        return sintaxis.configuracion().tiene(logro);
    }
}
