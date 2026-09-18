package minijava.sintactico;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

// El lenguaje reconocido, regla por regla: lo que define la gramática de la
// cátedra manda; lo que no define, se comporta como Java. Cada caso nombra la
// regla que decide su veredicto, y los que se apartan de Java dicen por qué.
@Tag("sintactico")
class LenguajeDeLaBaseYDeLosLogrosTest extends BaseDeAnalisis {

    private static String clase(String miembros) {
        return "class Ab { " + miembros + " }\n";
    }

    private static String metodo(String sentencias) {
        return "class Ab { void m() { " + sentencias + " } }\n";
    }

    static Stream<Arguments> validos() {
        return Stream.of(
                arguments("class Ab<T> { }\n", "base"),
                arguments(clase("int x = 1;"), "Atributos Inicializados"),
                arguments(clase("private int x = 1;"), "Visibilidad Mejorada"),
                arguments(clase("Ab() { }"), "Visibilidad Mejorada: visibilidad implícita"),
                arguments(clase("private Ab() { }"), "Visibilidad Mejorada"),
                arguments(clase("static public void m() { }"), "Visibilidad Mejorada, como Java"),
                arguments("interface Ab { public void m(); }\n", "Visibilidad Mejorada"),
                arguments(clase("Bc<Cd<De<T>>> x;"), "Genericidad Avanzada"),
                arguments(clase("float x;"), "Floats"),
                arguments(metodo("int x = 1, y, z = 2;"), "Variables Locales Clásicas"),
                arguments(metodo("int x = 10, y = 11, z = 12;"), "Variables Locales Clásicas, como Java"),
                arguments(metodo("Ab<T> x;"), "Variables Locales Clásicas"),
                arguments(metodo("T x;"), "Variables Locales Clásicas"),
                arguments(metodo("x;"), "base: Asignacion -> Expresion"),
                arguments(metodo("1 + 2;"), "base: Asignacion -> Expresion"),
                arguments(metodo("Ab.m() = 1;"), "base: el lado izquierdo es una ExpresionCompuesta"),
                arguments(metodo("for (;;) ;"), "Fors, como Java: las tres partes son opcionales"),
                arguments(metodo("for (var i = 0; i < n; i++) ;"), "Fors"),
                arguments(metodo("for (var x : y) ;"), "Fors"),
                arguments(metodo("for (;; 1 + 2) ;"), "Fors: una sentencia-expresión de la base"),
                arguments(metodo("a ? b : c;"), "base + Ternario"),
                arguments(metodo("(a ? b : c) = d;"), "base: un paréntesis es una ExpresionCompuesta"),
                arguments(metodo("x = a || b ? c : d;"), "Ternario"),
                arguments(metodo("x = a ? b : c + d;"), "Ternario"),
                arguments(metodo("(x)++;"), "Posfijos"),
                arguments(metodo("x = (a = b);"), "base"),
                arguments(metodo("m(a = b);"), "base"),
                arguments(metodo("x = new Ab<Cd>[3];"), "Genericidad Avanzada"),
                arguments(metodo("x = new Ab[]{a, b};"), "Inicialización de Arreglos"),
                arguments(metodo("x = new int[]{1}.length;"), "base: una creación arrastra cadena"),
                arguments(metodo("x = new int[]{1}[0];"), "base, como Java"));
    }

    static Stream<Arguments> invalidos() {
        return Stream.of(
                arguments("class Ab<T, U> { }\n", "Genericidad Avanzada: un parámetro de tipo"),
                arguments("class Ab extends Bc implements Cd { }\n", "base: extends o implements"),
                arguments("class Ab implements Bc, Cd { }\n", "base: un solo tipo"),
                arguments("public class Ab { }\n", "base: la clase no lleva modificadores"),
                arguments(clase("int x, y;"), "base: un atributo por declaración"),
                arguments(clase("static int x;"), "base: static sólo en métodos, confirmado por el profesor"),
                arguments(clase("static Ab() { }"), "base, como Java"),
                arguments(clase("int m()[] { }"), "base: las dimensiones van en el tipo"),
                arguments("interface Ab { private void m(); }\n", "Visibilidad Mejorada, como Java"),
                arguments("interface Ab { static void m(); }\n", "base"),
                arguments(clase("Bc<int> x;"), "base: el argumento de tipo es una clase o un idGen"),
                arguments(clase("Bc<?> x;"), "base: sin comodines"),
                arguments(clase("Bc<Cd, De> x;"), "Genericidad Avanzada: un parámetro de tipo"),
                arguments(metodo("int x[];"), "base: las dimensiones van en el tipo"),
                arguments(metodo("var x = 1, y = 2;"), "base: VarLocal declara una variable"),
                arguments(metodo("int x = y = 1;"), "base: el lado derecho de un `=` no lleva otro"),
                arguments(metodo("Ab.x = 1;"), "base: no hay acceso estático a un atributo"),
                arguments(metodo("this(1);"), "base"),
                arguments(metodo("for (;; i++, j++) ;"), "Fors: una expresión por separador"),
                arguments(metodo("for (var i; ;) ;"), "Fors, como Java"),
                arguments(metodo("for (int i = 0; ;) ;"), "Fors: la variable de control se declara con var"),
                arguments(metodo("for (Ab x : y) ;"), "Fors: la variable de control se declara con var"),
                arguments(metodo("++x;"), "base: los unarios son + - !"),
                arguments(metodo("x = Ab.m;"), "base: la llamada estática lleva argumentos"),
                arguments(metodo("var f = x -> x;"), "`->` no es un token de MiniJava"),
                arguments(metodo("var f = () -> 1;"), "`->` no es un token de MiniJava"),
                arguments(metodo("m(x -> x);"), "`->` no es un token de MiniJava"),
                arguments(metodo("x = new int[2][];"), "base: sin inicializador, toda dimensión con tamaño"),
                arguments(metodo("int[] x = {1, 2};"), "Inicialización de Arreglos: sólo con new"));
    }

    @ParameterizedTest(name = "[{index}] {1}: {0}")
    @MethodSource("validos")
    void seAcepta(String fuente, String regla, @TempDir Path dir) {
        aceptar(dir, fuente);
    }

    @ParameterizedTest(name = "[{index}] {1}: {0}")
    @MethodSource("invalidos")
    void seRechaza(String fuente, String regla, @TempDir Path dir) {
        rechazar(dir, fuente);
    }
}
