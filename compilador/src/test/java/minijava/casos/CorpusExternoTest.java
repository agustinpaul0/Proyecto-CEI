package minijava.casos;

import minijava.aplicacion.ModuloPrincipalSintactico;
import minijava.comun.salida.SalidaEnMemoria;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

// Verificación diferencial contra un corpus AJENO: fuentes generados por fuzzing
// a partir de otra gramática de MiniJava, en `vendor/tests/`. Es el mismo
// argumento que `comparar_con_gramatica.py` —dos implementaciones independientes
// tienen que coincidir— pero con una gramática que no salió de este proyecto, así
// que no comparte ni siquiera las decisiones.
//
// EL CORPUS Y SU FUZZER SON DE GIULIANO TAORMINA, compañero de la materia, que los
// compartió para que cada uno pudiera contrastar su analizador. No son material de
// este proyecto: acá sólo se corre el compilador contra ellos.
//
// Los casos escritos a mano de ese corpus (55, los `ext*` de resources/) ya están
// importados y se verifican con el resto. Acá van los generados, que NO se copian
// ni se versionan.
//
// El corpus original traía 30.000 casos y se podó. Se sacaron los que divergían
// por la lectura de la barra invertida en un literal —Aspectos Léxicos §2.3.3 da
// una regla POSICIONAL y no de apareamiento, así que acá el literal no cierra—,
// los que usan construcciones que la gramática de este proyecto no tiene, y
// después, ya revisados uno por uno, los que divergían por construcciones que su
// gramática no tiene: los genéricos al instanciar, el logro de Visibilidad
// Mejorada, y un puñado que las dos rechazan en distinto token. Ninguno escondía
// un defecto propio.
//
// El corpus es opcional: si la carpeta no está, la clase se saltea entera.
@Tag("sintactico")
class CorpusExternoTest {

    private static final Path CORPORA =
            Path.of("..", "vendor", "tests", "resources", "parser", "corpora");
    private static final List<String> GENERADOS = List.of("base", "localvar");

    @Test
    void elCorpusExternoNoRevelaNingunaDivergencia() throws IOException {
        assumeTrue(Files.isDirectory(CORPORA),
                "el corpus externo no está presente: " + CORPORA.toAbsolutePath().normalize());

        List<String> divergencias = new ArrayList<>();
        int comparados = 0;

        for (String corpus : GENERADOS) {
            for (boolean valido : new boolean[] { true, false }) {
                Path carpeta = CORPORA.resolve(corpus)
                        .resolve(valido ? "sinErrores" : "conErrores");
                if (!Files.isDirectory(carpeta)) {
                    continue;
                }
                try (Stream<Path> casos = Files.list(carpeta)) {
                    for (Path caso : casos.filter(CorpusExternoTest::esFuente).sorted().toList()) {
                        comparados++;
                        String fuente = leer(caso);
                        String salida = ejecutar(caso);
                        if (!coincide(fuente, salida, valido)) {
                            divergencias.add(corpus + "/" + caso.getFileName() + ": "
                                    + primerRenglon(salida));
                        }
                    }
                }
            }
        }

        System.out.printf("corpus externo: %d casos, %d coinciden, %d divergen%n",
                comparados, comparados - divergencias.size(), divergencias.size());
        assertEquals(List.of(), divergencias.size() > 20 ? divergencias.subList(0, 20) : divergencias,
                "el compilador y el corpus externo dan veredictos distintos");
    }

    private static boolean esFuente(Path caso) {
        return caso.getFileName().toString().endsWith(".java");
    }

    // El veredicto que el caso declara: `[SinErrores]` o el código de su primera
    // línea. El corpus generado usa `// [Error:…]` y el escrito a mano `///`, así
    // que se saca el prefijo y los espacios.
    private static boolean coincide(String fuente, String salida, boolean valido) {
        if (valido) {
            return salida.contains("[SinErrores]");
        }
        return salida.contains(veredictoDeclarado(fuente));
    }

    private static String veredictoDeclarado(String fuente) {
        return fuente.split("\n", 2)[0].replaceFirst("^/+", "").trim();
    }

    private static String leer(Path caso) throws IOException {
        return Files.readString(caso, StandardCharsets.UTF_8);
    }

    private static String ejecutar(Path caso) throws IOException {
        SalidaEnMemoria salida = new SalidaEnMemoria();
        new ModuloPrincipalSintactico(salida).ejecutar(caso.toString());
        return String.join("\n", salida.getLineas());
    }

    private static String primerRenglon(String salida) {
        return salida.split("\n", 2)[0];
    }
}
