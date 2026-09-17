package minijava.sintactico.partes;

import minijava.lexico.errores.ExcepcionLexica;
import minijava.lexico.tokens.TipoDeToken;
import minijava.sintactico.Sintaxis;
import minijava.sintactico.errores.ExcepcionSintactica;
import minijava.sintactico.primeros.PrimerosDeArgumentos;

// La lista de argumentos actuales de una llamada. Sección propia y no parte de
// la creación: la usan por igual el `new`, el encadenado, la llamada estática,
// la llamada sin punto y el encabezado del for.
public final class AnalizadorDeArgumentos extends ParteDelAnalizador {

    public AnalizadorDeArgumentos(Sintaxis sintaxis) {
        super(sintaxis);
    }

    // ArgsActuales -> ( ListaExpsOpcional )
    // ListaExpsOpcional -> ListaExps | EPSILON
    // ListaExps -> Expresion RestoListaExps
    // RestoListaExps -> , Expresion RestoListaExps | EPSILON
    public void argsActuales() throws ExcepcionLexica, ExcepcionSintactica {
        match(TipoDeToken.PAREN_A);
        if (estaEn(PrimerosDeArgumentos.LISTA_EXPS_EXPRESION)) {
            sintaxis.expresiones().expresion();
            restoListaExps();
        } else {
            // ListaExpsOpcional -> EPSILON
        }
        match(TipoDeToken.PAREN_C);
    }

    // RestoListaExps -> , Expresion RestoListaExps | EPSILON
    private void restoListaExps() throws ExcepcionLexica, ExcepcionSintactica {
        if (!estaEn(PrimerosDeArgumentos.RESTO_LISTA_EXPS_COMA)) {
            return;
        }
        match(TipoDeToken.COMA);
        sintaxis.expresiones().expresion();
        restoListaExps();
    }
}
