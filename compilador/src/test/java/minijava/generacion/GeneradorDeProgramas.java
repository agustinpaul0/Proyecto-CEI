package minijava.generacion;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Programas MiniJava válidos y deterministas, para correrlos en la CeIVM y en Java
// y comparar lo que imprimen. Cada semilla da siempre el mismo programa.
//
// Se evita, a propósito, todo lo que hace que MiniJava y Java difieran o que uno
// de los dos falle:
// - cada binario va entre paréntesis, porque MiniJava no tiene precedencia;
// - el lado izquierdo de una asignación no tiene efectos (CONVENCIONES-CATEDRA §4);
// - no se comparan Strings por identidad (CONVENCIONES-CATEDRA §5) ni se usa el toString de Object;
// - los divisores son siempre positivos y los índices, válidos;
// - sólo las clases sin descendientes declaran constructores: Java llama al
//   constructor del padre y MiniJava no (documento §6.3);
// - un método de índice k sólo llama a métodos de índice menor, así la recursión
//   termina, y la única recursión directa tiene un caso base;
// - nada queda después de un return, que javac rechazaría por inalcanzable.
final class GeneradorDeProgramas {

    private final Random azar;
    private final StringBuilder texto = new StringBuilder();
    private int ultimoNombre;

    private final List<List<String>> atributosPropios = new ArrayList<>();
    private final List<List<Integer>> aridadDeMetodos = new ArrayList<>();
    private int cantidadDeClases;

    GeneradorDeProgramas(long semilla) {
        azar = new Random(semilla);
    }

    String programa() {
        cantidadDeClases = 2 + azar.nextInt(3);
        int cantidadDeMetodos = 2 + azar.nextInt(3);
        for (int i = 0; i < cantidadDeMetodos; i++) {
            aridadDeMetodos.add(List.of(azar.nextInt(3)));
        }
        linea("interface Medible {");
        linea("    int medir(int x);");
        linea("}");
        linea("class Caja<T> {");
        linea("    T dato;");
        linea("    T obtener() {");
        linea("        return dato;");
        linea("    }");
        linea("}");
        for (int clase = 0; clase < cantidadDeClases; clase++) {
            clase(clase, cantidadDeMetodos);
        }
        principal(cantidadDeMetodos);
        return texto.toString();
    }

    private void clase(int clase, int cantidadDeMetodos) {
        List<String> propios = new ArrayList<>();
        int cantidadDeAtributos = azar.nextInt(3);
        for (int i = 0; i < cantidadDeAtributos; i++) {
            propios.add("a" + clase + "_" + i);
        }
        atributosPropios.add(propios);
        String encabezado = clase == 0 ? "class C0 implements Medible {" : "class C" + clase + " extends C" + (clase - 1) + " {";
        linea(encabezado);
        for (String atributo : propios) {
            linea("    int " + atributo + ";");
        }
        List<String> visibles = atributosVisibles(clase);
        if (clase == cantidadDeClases - 1 && azar.nextBoolean()) {
            linea("    C" + clase + "(int inicial) {");
            for (String atributo : visibles) {
                linea("        " + atributo + " = " + entera(1, List.of("inicial"), List.of()) + ";");
            }
            linea("    }");
        }
        if (clase == 0 || azar.nextBoolean()) {
            metodo("medir", List.of("x"), visibles, 0, "        ");
        }
        for (int m = 0; m < cantidadDeMetodos; m++) {
            if (clase == 0 || azar.nextInt(3) == 0) {
                List<String> parametros = new ArrayList<>();
                for (int p = 0; p < aridadDeMetodos.get(m).getFirst(); p++) {
                    parametros.add("p" + p);
                }
                metodo("m" + m, parametros, visibles, m, "        ");
            }
        }
        linea("    static int s" + clase + "(int n) {");
        linea("        return " + entera(2, List.of("n"), List.of()) + ";");
        linea("    }");
        linea("}");
    }

    private void metodo(String nombre, List<String> parametros, List<String> atributos, int indice, String sangria) {
        linea("    int " + nombre + "(" + String.join(", ", parametros.stream().map(p -> "int " + p).toList()) + ") {");
        List<String> enteras = new ArrayList<>(parametros);
        enteras.addAll(atributos);
        List<String> llamables = new ArrayList<>();
        for (int m = 0; m < indice; m++) {
            llamables.add("m" + m);
        }
        int sentencias = azar.nextInt(4);
        for (int i = 0; i < sentencias; i++) {
            sentencia(enteras, atributos, llamables, sangria);
        }
        linea(sangria + "return " + entera(2, enteras, llamables) + ";");
        linea("    }");
    }

    private void sentencia(List<String> enteras, List<String> atributos, List<String> llamables, String sangria) {
        switch (azar.nextInt(5)) {
            case 0 -> {
                String local = nombreNuevo("v");
                linea(sangria + "var " + local + " = " + entera(2, enteras, llamables) + ";");
                enteras.add(local);
            }
            case 1 -> {
                if (!atributos.isEmpty()) {
                    linea(sangria + elegir(atributos) + " = " + entera(2, enteras, llamables) + ";");
                }
            }
            case 2 -> {
                linea(sangria + "if (" + booleana(2, enteras) + ") {");
                linea(sangria + "    debugPrint(" + entera(1, enteras, llamables) + ");");
                linea(sangria + "} else {");
                linea(sangria + "    debugPrint(" + entera(1, enteras, llamables) + ");");
                linea(sangria + "}");
            }
            case 3 -> {
                String contador = nombreNuevo("i");
                String acumulado = nombreNuevo("t");
                linea(sangria + "var " + contador + " = 0;");
                linea(sangria + "var " + acumulado + " = 0;");
                linea(sangria + "while (" + contador + " < " + (1 + azar.nextInt(5)) + ") {");
                List<String> adentro = new ArrayList<>(enteras);
                adentro.add(contador);
                linea(sangria + "    " + acumulado + " = (" + acumulado + " + " + entera(1, adentro, List.of()) + ");");
                linea(sangria + "    " + contador + " = " + contador + " + 1;");
                linea(sangria + "}");
                enteras.add(contador);
                enteras.add(acumulado);
            }
            default -> {
                if (azar.nextBoolean()) {
                    linea(sangria + "if (" + booleana(1, enteras) + ") {");
                    linea(sangria + "    return " + entera(1, enteras, llamables) + ";");
                    linea(sangria + "}");
                } else {
                    linea(sangria + "System.printBln(" + booleana(2, enteras) + ");");
                }
            }
        }
    }

    private void principal(int cantidadDeMetodos) {
        linea("class Init {");
        linea("    static int recursiva(int n) {");
        linea("        if (n <= 0) {");
        linea("            return " + entera(1, List.of("n"), List.of()) + ";");
        linea("        }");
        linea("        return (recursiva(n - 1) + " + entera(1, List.of("n"), List.of()) + ");");
        linea("    }");
        linea("    static int probar(C0 c, int x) {");
        linea("        return (c.m0(" + argumentos(0, "x") + ") + c.medir(x));");
        linea("    }");
        linea("    static int porInterfaz(Medible m, int x) {");
        linea("        return m.medir(x);");
        linea("    }");
        linea("    static void main() {");
        List<String> objetos = new ArrayList<>();
        for (int clase = 0; clase < cantidadDeClases; clase++) {
            String objeto = "o" + clase;
            boolean conConstructor = texto.indexOf("C" + clase + "(int inicial)") >= 0;
            linea("        var " + objeto + " = new C" + clase + "(" + (conConstructor ? literal() : "") + ");");
            objetos.add(objeto);
        }
        List<String> enteras = new ArrayList<>();
        int pruebas = 6 + azar.nextInt(8);
        for (int i = 0; i < pruebas; i++) {
            String objeto = elegir(objetos);
            int clase = Integer.parseInt(objeto.substring(1));
            switch (azar.nextInt(9)) {
                case 0 -> linea("        debugPrint(probar(" + objeto + ", " + literal() + "));");
                case 1 -> linea("        debugPrint(porInterfaz(" + objeto + ", " + literal() + "));");
                case 2 -> {
                    int m = azar.nextInt(cantidadDeMetodos);
                    linea("        debugPrint(" + objeto + ".m" + m + "(" + argumentos(m, null) + "));");
                }
                case 3 -> {
                    List<String> atributos = atributosVisibles(clase);
                    if (!atributos.isEmpty()) {
                        String atributo = elegir(atributos);
                        linea("        " + objeto + "." + atributo + " = " + entera(2, enteras, List.of()) + ";");
                        linea("        debugPrint(" + objeto + "." + atributo + ");");
                    }
                }
                case 4 -> linea("        debugPrint(C" + clase + ".s" + clase + "(" + literal() + "));");
                case 5 -> linea("        debugPrint(recursiva(" + azar.nextInt(8) + "));");
                case 6 -> arreglo(enteras);
                case 7 -> {
                    String caja = nombreNuevo("caja");
                    linea("        var " + caja + " = new Caja<C0>();");
                    linea("        " + caja + ".dato = " + objeto + ";");
                    linea("        debugPrint(" + caja + ".obtener().medir(" + literal() + "));");
                }
                default -> {
                    String local = nombreNuevo("v");
                    linea("        var " + local + " = " + entera(3, enteras, List.of()) + ";");
                    enteras.add(local);
                    linea("        System.printIln(" + local + ");");
                    linea("        System.printBln(" + booleana(2, enteras) + ");");
                }
            }
        }
        linea("        System.printSln(\"fin\");");
        linea("    }");
        linea("}");
    }

    private void arreglo(List<String> enteras) {
        String arreglo = nombreNuevo("arr");
        String indice = nombreNuevo("k");
        int largo = 1 + azar.nextInt(6);
        linea("        var " + arreglo + " = new int[" + largo + "];");
        linea("        var " + indice + " = 0;");
        linea("        while (" + indice + " < " + arreglo + ".length) {");
        List<String> conIndice = new ArrayList<>(enteras);
        conIndice.add(indice);
        linea("            " + arreglo + "[" + indice + "] = " + entera(2, conIndice, List.of()) + ";");
        linea("            " + indice + " = " + indice + " + 1;");
        linea("        }");
        linea("        debugPrint(" + arreglo + "[" + azar.nextInt(largo) + "]);");
        String matriz = nombreNuevo("mat");
        linea("        var " + matriz + " = new int[2][" + largo + "];");
        linea("        " + matriz + "[1][" + (largo - 1) + "] = " + arreglo + "[0];");
        linea("        debugPrint((" + matriz + "[1][" + (largo - 1) + "] + " + matriz + "[0].length));");
        enteras.add(indice);
    }

    private String argumentos(int metodo, String primero) {
        List<String> valores = new ArrayList<>();
        for (int p = 0; p < aridadDeMetodos.get(metodo).getFirst(); p++) {
            valores.add(p == 0 && primero != null ? primero : literal());
        }
        return String.join(", ", valores);
    }

    private String entera(int profundidad, List<String> variables, List<String> llamables) {
        if (profundidad == 0 || azar.nextInt(4) == 0) {
            return variables.isEmpty() || azar.nextBoolean() ? literal() : elegir(variables);
        }
        int menor = profundidad - 1;
        return switch (azar.nextInt(8)) {
            case 0 -> "(" + entera(menor, variables, llamables) + " + " + entera(menor, variables, llamables) + ")";
            case 1 -> "(" + entera(menor, variables, llamables) + " - " + entera(menor, variables, llamables) + ")";
            case 2 -> "(" + entera(menor, variables, llamables) + " * " + entera(menor, variables, llamables) + ")";
            case 3 -> "(" + entera(menor, variables, llamables) + " / " + divisor(menor, variables) + ")";
            case 4 -> "(" + entera(menor, variables, llamables) + " % " + divisor(menor, variables) + ")";
            case 5 -> "-(" + entera(menor, variables, llamables) + ")";
            case 6 -> llamables.isEmpty() ? literal() : llamada(elegir(llamables), menor, variables, llamables);
            default -> "(" + entera(menor, variables, llamables) + " + " + literal() + ")";
        };
    }

    private String llamada(String metodo, int profundidad, List<String> variables, List<String> llamables) {
        int indice = Integer.parseInt(metodo.substring(1));
        List<String> valores = new ArrayList<>();
        for (int p = 0; p < aridadDeMetodos.get(indice).getFirst(); p++) {
            valores.add(entera(profundidad, variables, llamables.subList(0, Math.min(indice, llamables.size()))));
        }
        return (azar.nextBoolean() ? "this." : "") + metodo + "(" + String.join(", ", valores) + ")";
    }

    // Siempre entre 2 y 14: (e % 7) está entre -6 y 6.
    private String divisor(int profundidad, List<String> variables) {
        return "((" + entera(profundidad, variables, List.of()) + " % 7) + 8)";
    }

    private String booleana(int profundidad, List<String> variables) {
        if (profundidad == 0) {
            return azar.nextBoolean() ? "true" : "false";
        }
        int menor = profundidad - 1;
        String[] comparadores = {"<", "<=", ">", ">=", "==", "!="};
        return switch (azar.nextInt(4)) {
            case 0 -> "(" + booleana(menor, variables) + " && " + booleana(menor, variables) + ")";
            case 1 -> "(" + booleana(menor, variables) + " || " + booleana(menor, variables) + ")";
            case 2 -> "!(" + booleana(menor, variables) + ")";
            default -> "(" + entera(menor, variables, List.of()) + " "
                    + comparadores[azar.nextInt(comparadores.length)] + " " + entera(menor, variables, List.of()) + ")";
        };
    }

    private List<String> atributosVisibles(int clase) {
        List<String> visibles = new ArrayList<>();
        for (int i = 0; i <= clase; i++) {
            visibles.addAll(atributosPropios.get(i));
        }
        return visibles;
    }

    private String literal() {
        int valor = azar.nextInt(6) == 0 ? azar.nextInt(999999999) : azar.nextInt(100);
        return azar.nextInt(4) == 0 ? "-" + valor : String.valueOf(valor);
    }

    private String elegir(List<String> opciones) {
        return opciones.get(azar.nextInt(opciones.size()));
    }

    private String nombreNuevo(String prefijo) {
        ultimoNombre++;
        return prefijo + ultimoNombre;
    }

    private void linea(String linea) {
        texto.append(linea).append('\n');
    }
}
