package minijava.sintactico.partes;

import minijava.lexico.errores.ExcepcionLexica;
import minijava.lexico.tokens.TipoDeToken;
import minijava.semantico.ast.expresiones.NodoExpresion;
import minijava.sintactico.Sintaxis;
import minijava.sintactico.errores.ExcepcionSintactica;
import minijava.sintactico.primeros.PrimerosDeArgumentos;

import java.util.ArrayList;
import java.util.List;

// La lista de argumentos actuales de una llamada. Sección propia y no parte de
// la creación: la usan por igual el `new`, el encadenado, la llamada estática,
// la llamada sin punto y la sentencia que empieza con idClase.
public final class AnalizadorDeArgumentos extends ParteDelAnalizador {

    public AnalizadorDeArgumentos(Sintaxis sintaxis) {
        super(sintaxis);
    }

    // ArgsActuales -> ( ListaExpsOpcional )
    public List<NodoExpresion> argsActuales() throws ExcepcionLexica, ExcepcionSintactica {
        match(TipoDeToken.PAREN_A);
        List<NodoExpresion> argumentos = new ArrayList<>();
        listaExpsOpcional(argumentos);
        match(TipoDeToken.PAREN_C);
        return argumentos;
    }

    // ListaExpsOpcional -> ListaExps | EPSILON
    //
    // `argumentos` acumula los nodos en orden.
    private void listaExpsOpcional(List<NodoExpresion> argumentos) throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeArgumentos.LISTA_EXPS_OPCIONAL_LISTA_EXPS)) {
            listaExps(argumentos);
        } else {
            // ListaExpsOpcional -> EPSILON
        }
    }

    // ListaExps -> Expresion RestoListaExps
    private void listaExps(List<NodoExpresion> argumentos) throws ExcepcionLexica, ExcepcionSintactica {
        argumentos.add(sintaxis.expresiones().expresion());
        restoListaExps(argumentos);
    }

    // RestoListaExps -> , ListaExps | EPSILON
    private void restoListaExps(List<NodoExpresion> argumentos) throws ExcepcionLexica, ExcepcionSintactica {
        if (estaEn(PrimerosDeArgumentos.RESTO_LISTA_EXPS_COMA)) {
            match(TipoDeToken.COMA);
            listaExps(argumentos);
        } else {
            // RestoListaExps -> EPSILON
        }
    }
}
