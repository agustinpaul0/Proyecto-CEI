package minijava.generacion;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

// El segundo oráculo: corre un programa MiniJava como Java, con javac y la JVM del
// propio test, para comparar su salida con la de la CeIVM. MiniJava es casi un
// subconjunto de Java; lo que falta lo pone este adaptador:
// - una base común con debugPrint, el estático que Object tiene en MiniJava y no en
//   Java, que hereda toda clase sin `extends`;
// - una clase System propia con los print de MiniJava, que oculta a java.lang.System;
// - `public` en los métodos con cuerpo, porque en MiniJava todos lo son y Java no
//   deja redefinir toString ni implementar una interfaz con menos acceso.
// Cada programa va en su propio paquete, así una tanda se compila con una sola
// llamada a javac.
public final class EjecutorJava {

    private static final String BASE = "BaseMiniJava";
    private static final Pattern CLASE_SIN_EXTENDS =
            Pattern.compile("(class\\s+\\w+(?:\\s*<\\s*\\w\\s*>)?)(\\s+implements\\b|\\s*\\{)");
    private static final Pattern METODO_CON_CUERPO = Pattern.compile(
            "^(\\s*)(?!public\\b|return\\b|else\\b|new\\b)((?:static\\s+)?[\\w<>\\[\\]]+\\s+\\w+\\s*\\([^;]*\\)\\s*\\{)");

    private EjecutorJava() {
    }

    public static Map<String, String> correr(Map<String, String> programas) {
        try {
            Path directorio = Files.createTempDirectory("minijava-java-");
            try {
                List<Path> fuentes = new ArrayList<>();
                List<String> nombres = new ArrayList<>(programas.keySet());
                for (int i = 0; i < nombres.size(); i++) {
                    Path archivo = directorio.resolve("p" + i).resolve("Programa.java");
                    Files.createDirectories(archivo.getParent());
                    Files.writeString(archivo, adaptar("p" + i, programas.get(nombres.get(i))),
                            StandardCharsets.UTF_8);
                    fuentes.add(archivo);
                }
                compilar(directorio, fuentes);
                Map<String, String> salidas = new LinkedHashMap<>();
                try (URLClassLoader cargador = new URLClassLoader(new URL[]{directorio.toUri().toURL()},
                        EjecutorJava.class.getClassLoader())) {
                    for (int i = 0; i < nombres.size(); i++) {
                        salidas.put(nombres.get(i), ejecutar(cargador, directorio.resolve("p" + i), "p" + i));
                    }
                }
                return salidas;
            } finally {
                try (Stream<Path> archivos = Files.walk(directorio)) {
                    for (Path archivo : archivos.sorted(Comparator.reverseOrder()).toList()) {
                        Files.delete(archivo);
                    }
                }
            }
        } catch (IOException excepcion) {
            throw new UncheckedIOException(excepcion);
        }
    }

    static String adaptar(String paquete, String fuente) {
        String conBase = CLASE_SIN_EXTENDS.matcher(fuente).replaceAll(resultado -> {
            String sufijo = resultado.group(2);
            String puente = sufijo.trim().startsWith("implements") ? " extends " + BASE : " extends " + BASE + " ";
            return Matcher.quoteReplacement(resultado.group(1) + puente + sufijo);
        });
        StringBuilder adaptado = new StringBuilder("package " + paquete + ";\n");
        for (String linea : conBase.split("\n", -1)) {
            adaptado.append(METODO_CON_CUERPO.matcher(linea).replaceFirst("$1public $2")).append('\n');
        }
        adaptado.append("""
                class BaseMiniJava {
                    public static void debugPrint(int i) { java.lang.System.out.println(i); }
                }
                class System {
                    static int read() {
                        try { return java.lang.System.in.read(); } catch (java.io.IOException e) { return -1; }
                    }
                    static void printB(boolean b) { java.lang.System.out.print(b); }
                    static void printC(char c) { java.lang.System.out.print(c); }
                    static void printI(int i) { java.lang.System.out.print(i); }
                    static void printS(String s) { java.lang.System.out.print(s); }
                    static void println() { java.lang.System.out.println(); }
                    static void printBln(boolean b) { java.lang.System.out.println(b); }
                    static void printCln(char c) { java.lang.System.out.println(c); }
                    static void printIln(int i) { java.lang.System.out.println(i); }
                    static void printSln(String s) { java.lang.System.out.println(s); }
                }
                """);
        return adaptado.toString().replace("Object.debugPrint(", BASE + ".debugPrint(");
    }

    private static void compilar(Path directorio, List<Path> fuentes) {
        JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
        ByteArrayOutputStream errores = new ByteArrayOutputStream();
        List<String> argumentos = new ArrayList<>(List.of("-d", directorio.toString(), "-nowarn",
                "-Xlint:none", "-encoding", "UTF-8"));
        fuentes.forEach(fuente -> argumentos.add(fuente.toString()));
        int resultado = javac.run(null, null, errores, argumentos.toArray(String[]::new));
        if (resultado != 0) {
            throw new AssertionError("javac rechazo el programa adaptado:\n"
                    + errores.toString(StandardCharsets.UTF_8));
        }
    }

    // Busca la clase con `static void main()` y la invoca con la salida capturada.
    private static String ejecutar(ClassLoader cargador, Path paquete, String nombreDelPaquete) throws IOException {
        List<String> clases;
        try (Stream<Path> archivos = Files.list(paquete)) {
            clases = archivos.map(p -> p.getFileName().toString()).filter(n -> n.endsWith(".class"))
                    .map(n -> nombreDelPaquete + "." + n.substring(0, n.length() - ".class".length()))
                    .sorted().toList();
        }
        PrintStream original = java.lang.System.out;
        ByteArrayOutputStream capturada = new ByteArrayOutputStream();
        java.lang.System.setOut(new PrintStream(capturada, true, StandardCharsets.UTF_8));
        try {
            for (String clase : clases) {
                Method main = mainDe(cargador.loadClass(clase));
                if (main != null) {
                    main.invoke(null);
                    break;
                }
            }
        } catch (ClassNotFoundException | IllegalAccessException excepcion) {
            throw new IllegalStateException(excepcion);
        } catch (InvocationTargetException excepcion) {
            java.lang.System.out.flush();
            return capturada.toString(StandardCharsets.UTF_8) + "<excepcion " + excepcion.getCause() + ">";
        } finally {
            java.lang.System.setOut(original);
        }
        return capturada.toString(StandardCharsets.UTF_8);
    }

    private static Method mainDe(Class<?> clase) {
        for (Method metodo : clase.getDeclaredMethods()) {
            if (metodo.getName().equals("main") && metodo.getParameterCount() == 0
                    && Modifier.isStatic(metodo.getModifiers()) && metodo.getReturnType() == void.class) {
                metodo.setAccessible(true);
                return metodo;
            }
        }
        return null;
    }
}
