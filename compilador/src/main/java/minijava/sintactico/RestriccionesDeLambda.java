package minijava.sintactico;

import minijava.lexico.tokens.Token;
import minijava.sintactico.errores.ExcepcionSintactica;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
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
    private final Deque<List<Token>> capturasEnDuda = new ArrayDeque<>();

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
    //
    // El indicador dice si la RAÍZ de la última expresión es una lambda, no si se
    // vio una lambda en algún lado: `(a = x -> x)` es una asignación y
    // `(a ? b : x -> x)` un ternario, y las dos pueden llevar cola. Por eso las
    // producciones que ponen un `=` o un `?` en la raíz lo apagan al terminar.
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
    //
    // Salvo mientras se lee el contenido de un paréntesis que todavía puede ser
    // la lista de parámetros de otra lambda: ahí el nombre queda EN DUDA. En
    // `x -> (y) -> y` el `y` se lee como expresión —no hay forma de saber que es
    // un parámetro hasta ver el `->` que viene después del `)`—, y tomarlo por
    // una captura rechazaría un programa que la misma lambda escrita sin
    // paréntesis acepta.
    public void verificarQueNoCaptura(Token nombre) throws ExcepcionSintactica {
        if (!dentroDeLambda || parametros.contains(nombre.lexema())) {
            return;
        }
        if (!capturasEnDuda.isEmpty()) {
            capturasEnDuda.peek().add(nombre);
            return;
        }
        throw capturaProhibida(nombre);
    }

    // El paréntesis se abrió y todavía no se sabe qué es. Devuelve la profundidad
    // para poder restaurarla pase lo que pase, que es lo que necesita el Modo
    // Pánico: si el contenido falla, el marco tiene que cerrarse igual.
    public int abrirCandidatoAParametro() {
        capturasEnDuda.push(new ArrayList<>());
        return capturasEnDuda.size();
    }

    // Era una lista de parámetros: los nombres eran declaraciones, no capturas.
    public void descartarCandidatoAParametro() {
        capturasEnDuda.pop();
    }

    // Era una expresión parentizada: las dudas eran capturas de verdad, y se
    // reporta la primera en orden de aparición. No es necesariamente la que se
    // habría reportado sin diferir: en `x -> ((y) -> z)` el diferimiento descarta
    // el `y` —resultó ser un parámetro— y deja el `z`, que es la captura real.
    public void confirmarExpresionParentizada() throws ExcepcionSintactica {
        List<Token> enDuda = capturasEnDuda.pop();
        if (!enDuda.isEmpty()) {
            throw capturaProhibida(enDuda.getFirst());
        }
    }

    public void cerrarCandidatoAParametro(int profundidad) {
        while (capturasEnDuda.size() >= profundidad) {
            capturasEnDuda.pop();
        }
    }

    private ExcepcionSintactica capturaProhibida(Token nombre) {
        return ExcepcionSintactica.porRestriccion(nombre,
                "no se permite capturar la variable " + nombre.lexema() + " dentro de una lambda");
    }
}
