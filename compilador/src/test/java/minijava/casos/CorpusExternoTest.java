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
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
// este proyecto: acá sólo se corre el compilador contra ellos y se clasifica cada
// divergencia.
//
// Los casos escritos a mano de ese corpus (73, los `ext*` de resources/) ya están
// importados y se verifican con el resto. Acá van los generados, que NO se copian
// ni se versionan.
//
// El corpus original traía 30.000 casos y se podó. Se sacaron los que divergían
// por la lectura de la barra invertida en un literal —Aspectos Léxicos §2.3.3 da
// una regla POSICIONAL y no de apareamiento, así que acá el literal no cierra—, y
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
    private static final List<String> GENERADOS = List.of("base", "lambdas", "localvar");

    // La única familia en que las dos gramáticas difieren, verificada a mano. Un
    // caso que cae en ella no se compara: lo que este test prueba es que NO
    // aparezca una segunda.
    private enum Divergencia {

        // `(x -> y).m()`, `(x -> y) + 1` y `(x -> y)[0]`: acá se aceptan y su
        // gramática los rechaza. MiniJava base define que toda expresión
        // parentizada es un <Primario> y que un <Primario> arrastra cadena, así
        // que salen solos. Java los rechaza; se eligió respetar el molde de la
        // base. Ver documentacion/sintactico/decisiones-de-logica.md §2.2.
        LAMBDA_PARENTIZADA
    }

    // Se evalúa contra UN renglón —aquel donde la divergencia se manifiesta, ver
    // `renglonDeLaDivergencia`— y no contra el fuente entero. Con el fuente entero
    // un `new Xx<Y>()` perdido en la línea 30 se llevaba puesta la clasificación
    // de un error de la línea 17, y la familia real quedaba tapada.
    private static final Pattern LAMBDA_ENTRE_PARENTESIS =
            Pattern.compile("\\(\\s*[\\w\\s,()]*->");
    private static final Pattern MARCA_DE_ERROR = Pattern.compile("\\[Error:.*\\|(\\d+)]");

    @Test
    void elCorpusExternoNoRevelaNingunaDivergenciaSinDeclarar() throws IOException {
        assumeTrue(Files.isDirectory(CORPORA),
                "el corpus externo no está presente: " + CORPORA.toAbsolutePath().normalize());

        List<String> sinDeclarar = new ArrayList<>();
        Map<Divergencia, Integer> porFamilia = new EnumMap<>(Divergencia.class);
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
                        if (coincide(fuente, salida, valido)) {
                            continue;
                        }
                        Divergencia divergencia = divergenciaDe(fuente, salida);
                        if (divergencia != null) {
                            porFamilia.merge(divergencia, 1, Integer::sum);
                            continue;
                        }
                        sinDeclarar.add(corpus + "/" + caso.getFileName() + ": " + primerRenglon(salida));
                    }
                }
            }
        }

        int declarados = porFamilia.values().stream().mapToInt(Integer::intValue).sum();
        System.out.printf("corpus externo: %d casos, %d coinciden, %d divergencias declaradas, "
                + "%d sin declarar%n", comparados, comparados - declarados - sinDeclarar.size(),
                declarados, sinDeclarar.size());
        porFamilia.forEach((familia, cuantos) ->
                System.out.printf("    %6d  %s%n", cuantos, familia));
        assertEquals(List.of(), sinDeclarar.size() > 20 ? sinDeclarar.subList(0, 20) : sinDeclarar,
                "aparecieron divergencias que no están en el enum Divergencia");
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

    private static Divergencia divergenciaDe(String fuente, String salida) {
        boolean yoRechazo = salida.contains("[Error:");
        String renglon = renglonDeLaDivergencia(fuente, salida, yoRechazo);
        return !yoRechazo && LAMBDA_ENTRE_PARENTESIS.matcher(renglon).find()
                ? Divergencia.LAMBDA_PARENTIZADA : null;
    }

    // El renglón donde la divergencia se manifiesta: el de MI primer error si
    // rechacé, y si no el que el caso declara. Es lo que se clasifica, porque el
    // resto del fuente no tiene nada que ver con por qué las dos difieren.
    private static String renglonDeLaDivergencia(String fuente, String salida, boolean yoRechazo) {
        String[] renglones = fuente.split("\n", -1);
        int linea = lineaDe(yoRechazo ? salida : veredictoDeclarado(fuente));
        return linea >= 1 && linea <= renglones.length ? renglones[linea - 1] : "";
    }

    private static int lineaDe(String conMarcaDeError) {
        Matcher marca = MARCA_DE_ERROR.matcher(conMarcaDeError);
        return marca.find() ? Integer.parseInt(marca.group(1)) : -1;
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
