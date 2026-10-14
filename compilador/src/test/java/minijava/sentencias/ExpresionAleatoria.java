package minijava.sentencias;

import java.util.List;
import java.util.Random;

// Una expresión int o boolean bien tipada, armada al azar, con todos sus binarios
// entre paréntesis: así su forma no depende de cómo se agrupen los operadores.
// `conUnaHojaMalTipada` cambia una sola hoja por una del otro tipo y dice qué
// token tiene que reportar el compilador: el del nodo que la contiene.
sealed interface ExpresionAleatoria {

    String texto();

    List<String> ENTERAS = List.of("a", "1", "42", "v.length", "v[0]", "f(a)");
    List<String> BOOLEANAS = List.of("b", "true", "false", "g(b)");

    record Hoja(String texto, boolean entera) implements ExpresionAleatoria {
    }

    record Binaria(ExpresionAleatoria izquierda, String operador, ExpresionAleatoria derecha)
            implements ExpresionAleatoria {
        public String texto() {
            return "(" + izquierda.texto() + " " + operador + " " + derecha.texto() + ")";
        }
    }

    record Unaria(String operador, ExpresionAleatoria operando) implements ExpresionAleatoria {
        public String texto() {
            return operador + "(" + operando.texto() + ")";
        }
    }

    record Llamada(String metodo, ExpresionAleatoria argumento) implements ExpresionAleatoria {
        public String texto() {
            return metodo + "(" + argumento.texto() + ")";
        }
    }

    record Indice(ExpresionAleatoria indice) implements ExpresionAleatoria {
        public String texto() {
            return "v[" + indice.texto() + "]";
        }
    }

    static ExpresionAleatoria entera(Random azar, int profundidad) {
        int opcion = profundidad == 0 ? 0 : azar.nextInt(5);
        return switch (opcion) {
            case 0 -> new Hoja(elegir(azar, ENTERAS), true);
            case 1, 2 -> new Binaria(entera(azar, profundidad - 1),
                    elegir(azar, List.of("+", "-", "*", "/", "%")), entera(azar, profundidad - 1));
            case 3 -> azar.nextBoolean() ? new Unaria(elegir(azar, List.of("-", "+")), entera(azar, profundidad - 1))
                    : new Llamada("f", entera(azar, profundidad - 1));
            default -> new Indice(entera(azar, profundidad - 1));
        };
    }

    static ExpresionAleatoria booleana(Random azar, int profundidad) {
        int opcion = profundidad == 0 ? 0 : azar.nextInt(6);
        return switch (opcion) {
            case 0 -> new Hoja(elegir(azar, BOOLEANAS), false);
            case 1 -> new Binaria(entera(azar, profundidad - 1),
                    elegir(azar, List.of("<", ">", "<=", ">=", "==", "!=")), entera(azar, profundidad - 1));
            case 2, 3 -> new Binaria(booleana(azar, profundidad - 1),
                    elegir(azar, List.of("&&", "||", "==", "!=")), booleana(azar, profundidad - 1));
            case 4 -> new Unaria("!", booleana(azar, profundidad - 1));
            default -> new Llamada("g", booleana(azar, profundidad - 1));
        };
    }

    // El resultado: la expresión mutada y el lexema del error. Si la hoja cambiada
    // es la expresión entera, el error es el de la asignación que la recibe.
    record Mutacion(ExpresionAleatoria expresion, String lexema) {
    }

    static Mutacion conUnaHojaMalTipada(ExpresionAleatoria expresion, Random azar) {
        return mutar(expresion, azar, "=");
    }

    private static Mutacion mutar(ExpresionAleatoria expresion, Random azar, String tokenDelPadre) {
        return switch (expresion) {
            case Hoja hoja -> new Mutacion(new Hoja(hoja.entera() ? "true" : "1", !hoja.entera()),
                    tokenDelPadre.equals("[") ? (hoja.entera() ? "true" : "1") : tokenDelPadre);
            case Binaria binaria -> azar.nextBoolean()
                    ? conIzquierda(binaria, mutar(binaria.izquierda(), azar, binaria.operador()))
                    : conDerecha(binaria, mutar(binaria.derecha(), azar, binaria.operador()));
            case Unaria unaria -> {
                Mutacion interna = mutar(unaria.operando(), azar, unaria.operador());
                yield new Mutacion(new Unaria(unaria.operador(), interna.expresion()), interna.lexema());
            }
            case Llamada llamada -> {
                Mutacion interna = mutar(llamada.argumento(), azar, llamada.metodo());
                yield new Mutacion(new Llamada(llamada.metodo(), interna.expresion()), interna.lexema());
            }
            case Indice indice -> {
                Mutacion interna = mutar(indice.indice(), azar, "[");
                yield new Mutacion(new Indice(interna.expresion()), interna.lexema());
            }
        };
    }

    private static Mutacion conIzquierda(Binaria binaria, Mutacion izquierda) {
        return new Mutacion(new Binaria(izquierda.expresion(), binaria.operador(), binaria.derecha()),
                izquierda.lexema());
    }

    private static Mutacion conDerecha(Binaria binaria, Mutacion derecha) {
        return new Mutacion(new Binaria(binaria.izquierda(), binaria.operador(), derecha.expresion()),
                derecha.lexema());
    }

    private static String elegir(Random azar, List<String> opciones) {
        return opciones.get(azar.nextInt(opciones.size()));
    }
}
