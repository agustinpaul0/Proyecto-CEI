package minijava.sintactico;

import minijava.lexico.tokens.Token;
import minijava.sintactico.errores.ExcepcionSintactica;

import java.util.Set;

// Las restricciones del logro MiniLambdas que una gramática libre de contexto no
// puede expresar: ni `this` —explícito o implícito— ni la captura de variables
// dentro de una lambda, que un parámetro sea un identificador pelado, y que una
// lambda parentizada no lleve cola de expresión. El razonamiento de por qué se verifican acá y no en el semántico está
// en documentacion/sintactico/decisiones-de-deteccion-de-errores.md.
public final class RestriccionesDeLambda {

    private boolean dentroDeLambda;
    private Set<String> parametros = Set.of();
    private boolean ultimaExpresionFueLambda;

    // Las lambdas anidadas no necesitan una pila de alcances: usar el parámetro
    // de la lambda de afuera también está prohibido, así que alcanza con el
    // conjunto de la más interna. Igual hay que restaurar al salir, porque
    // después de la lambda el código vuelve a poder usar `this`.
    public Alcance entrar(Set<String> parametrosDeLaLambda) {
        Alcance anterior = new Alcance(dentroDeLambda, parametros);
        dentroDeLambda = true;
        parametros = parametrosDeLaLambda;
        return anterior;
    }

    public void salir(Alcance anterior) {
        dentroDeLambda = anterior.dentroDeLambda();
        parametros = anterior.parametros();
    }

    public record Alcance(boolean dentroDeLambda, Set<String> parametros) {
    }

    // Una lambda parentizada es válida cuando ella misma es toda la expresión
    // —`f((x -> x))`— y no cuando lleva cola: `(x -> x).m()` y `(x -> x) + 1` son
    // «lambda expression not expected here» en Java. Distinguirlos en la
    // gramática exigiría clonar la cadena de colas en su variante «el contenido
    // del paréntesis fue una lambda», así que lo verifica el analizador con lo
    // que acaba de leer.
    public void anotarQueEsLambda() {
        ultimaExpresionFueLambda = true;
    }

    public void anotarQueNoEsLambda() {
        ultimaExpresionFueLambda = false;
    }

    public boolean ultimaExpresionFueLambda() {
        return ultimaExpresionFueLambda;
    }

    public void verificarThisExplicito(Token thisEncontrado) throws ExcepcionSintactica {
        if (dentroDeLambda) {
            throw ExcepcionSintactica.porRestriccion(thisEncontrado,
                    "no se permite this dentro de una lambda");
        }
    }

    // El cuerpo de una lambda es UNA expresión, así que adentro no se declara
    // nada: los únicos nombres válidos son sus propios parámetros.
    public void verificarQueNoCaptura(Token nombre) throws ExcepcionSintactica {
        if (dentroDeLambda && !parametros.contains(nombre.lexema())) {
            throw ExcepcionSintactica.porRestriccion(nombre,
                    "no se permite capturar la variable " + nombre.lexema() + " dentro de una lambda");
        }
    }
}
