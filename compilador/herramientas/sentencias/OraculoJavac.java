import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

// Compila con javac, en una sola JVM, cada fuente de una carpeta y escribe una
// línea por fuente: la ruta, un tab y ACEPTA o RECHAZA. Lo usa
// generar_corpus_sentencias.py para contrastar cada caso generado con Java.
//
//     java herramientas/sentencias/OraculoJavac.java <carpeta>
public class OraculoJavac {

    public static void main(String[] args) throws IOException {
        JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
        Path salida = Files.createTempDirectory("oraculo-javac");
        try (StandardJavaFileManager archivos = javac.getStandardFileManager(null, null, null);
             Stream<Path> fuentes = Files.walk(Path.of(args[0]))) {
            for (Path fuente : fuentes.filter(Files::isRegularFile).sorted().toList()) {
                DiagnosticCollector<JavaFileObject> diagnosticos = new DiagnosticCollector<>();
                JavaFileObject unidad = new Fuente(Files.readString(fuente));
                boolean acepta = javac.getTask(null, archivos, diagnosticos,
                        List.of("-d", salida.toString(), "-proc:none", "-Xlint:none"), null,
                        List.of(unidad)).call();
                String primero = diagnosticos.getDiagnostics().stream()
                        .filter(d -> d.getKind() == Diagnostic.Kind.ERROR)
                        .map(d -> d.getLineNumber() + ": " + d.getMessage(null).lines().findFirst().orElse(""))
                        .findFirst().orElse("");
                System.out.println(fuente + "\t" + (acepta ? "ACEPTA" : "RECHAZA") + "\t" + primero);
            }
        }
    }

    private static final class Fuente extends SimpleJavaFileObject {
        private final String codigo;

        Fuente(String codigo) {
            super(URI.create("string:///Caso.java"), Kind.SOURCE);
            this.codigo = codigo;
        }

        @Override
        public CharSequence getCharContent(boolean ignorarErrores) {
            return codigo;
        }
    }
}
