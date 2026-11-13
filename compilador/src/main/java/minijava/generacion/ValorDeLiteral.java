package minijava.generacion;

import java.util.ArrayList;
import java.util.List;

// El valor de un literal char o String a partir de su lexema, con las comillas.
// Aspectos Léxicos §2.3.2 acepta `'\x'` con cualquier x sin decir su valor. Un
// escape de Java vale lo que en Java (JLS §3.10.7 y §3.3, verificado con javac
// 25); uno que Java no tiene vale el carácter mismo, porque javac lo rechaza y no
// hay nada que imitar (CONVENCIONES-CATEDRA §3).
public final class ValorDeLiteral {

    private ValorDeLiteral() {
    }

    public static int caracter(String lexema) {
        return decodificar(lexema.substring(1, lexema.length() - 1)).getFirst();
    }

    public static List<Integer> cadena(String lexema) {
        return decodificar(lexema.substring(1, lexema.length() - 1));
    }

    private static List<Integer> decodificar(String contenido) {
        List<Integer> codigos = new ArrayList<>();
        int i = 0;
        while (i < contenido.length()) {
            char actual = contenido.charAt(i);
            if (actual != '\\' || i + 1 == contenido.length()) {
                codigos.add((int) actual);
                i++;
            } else {
                i = escape(contenido, i + 1, codigos);
            }
        }
        return codigos;
    }

    // Devuelve la posición siguiente al escape que empieza en `inicio`, después de la barra.
    private static int escape(String contenido, int inicio, List<Integer> codigos) {
        char marca = contenido.charAt(inicio);
        if (marca == 'u') {
            int i = inicio;
            while (contenido.charAt(i) == 'u') {
                i++;
            }
            codigos.add(Integer.parseInt(contenido.substring(i, i + 4), 16));
            return i + 4;
        }
        if (marca >= '0' && marca <= '7') {
            return octal(contenido, inicio, codigos);
        }
        codigos.add(switch (marca) {
            case 'b' -> 8;
            case 't' -> 9;
            case 'n' -> 10;
            case 'f' -> 12;
            case 'r' -> 13;
            case 's' -> 32;
            default -> (int) marca;
        });
        return inicio + 1;
    }

    // JLS §3.10.7: hasta tres dígitos octales si el primero es 0-3, y hasta dos si no.
    private static int octal(String contenido, int inicio, List<Integer> codigos) {
        int maximo = contenido.charAt(inicio) <= '3' ? 3 : 2;
        int fin = inicio;
        while (fin < contenido.length() && fin - inicio < maximo
                && contenido.charAt(fin) >= '0' && contenido.charAt(fin) <= '7') {
            fin++;
        }
        codigos.add(Integer.parseInt(contenido.substring(inicio, fin), 8));
        return fin;
    }
}
