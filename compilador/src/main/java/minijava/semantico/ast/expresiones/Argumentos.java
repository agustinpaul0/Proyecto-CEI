package minijava.semantico.ast.expresiones;

import minijava.lexico.tokens.Token;
import minijava.semantico.ast.Contexto;
import minijava.semantico.entidades.Parametro;
import minijava.semantico.errores.ExcepcionSemantica;
import minijava.semantico.tipos.Sustitucion;
import minijava.semantico.tipos.Tipo;

import java.util.List;
import java.util.stream.Collectors;

// Documento §11.6: con la unidad ya elegida por aridad, cada argumento conforma
// con el parámetro de su posición, visto desde el receptor.
final class Argumentos {

    private Argumentos() {
    }

    // En una llamada a método el error va en el nombre del método, como en el
    // caso de la cátedra semIIError04.
    static void chequearEnLaLlamada(List<NodoExpresion> argumentos, List<Parametro> formales,
                                    Sustitucion sustitucion, Token metodo, Contexto contexto)
            throws ExcepcionSemantica {
        chequear(argumentos, formales, sustitucion, metodo, contexto);
    }

    // En un constructor la cátedra no fija el token: va en el argumento, como en javac.
    static void chequearEnElArgumento(List<NodoExpresion> argumentos, List<Parametro> formales,
                                      Sustitucion sustitucion, Contexto contexto) throws ExcepcionSemantica {
        chequear(argumentos, formales, sustitucion, null, contexto);
    }

    private static void chequear(List<NodoExpresion> argumentos, List<Parametro> formales,
                                 Sustitucion sustitucion, Token metodo, Contexto contexto)
            throws ExcepcionSemantica {
        for (int i = 0; i < argumentos.size(); i++) {
            NodoExpresion argumento = argumentos.get(i);
            Tipo actual = argumento.chequear(contexto);
            Tipo formal = formales.get(i).tipo().instanciar(sustitucion);
            if (!actual.conformaCon(formal)) {
                Token culpable = metodo == null ? argumento.token() : metodo;
                throw new ExcepcionSemantica(culpable, "El argumento " + (i + 1) + " es de tipo "
                        + actual.nombre() + " y no conforma con el parametro, de tipo " + formal.nombre());
            }
        }
    }

    static String comoTexto(List<NodoExpresion> argumentos) {
        return argumentos.stream().map(argumento -> " " + argumento).collect(Collectors.joining());
    }
}
