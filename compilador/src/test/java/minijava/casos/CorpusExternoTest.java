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
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

// Verificación diferencial contra un corpus AJENO: 30.000 fuentes generados por
// fuzzing a partir de otra gramática de MiniJava, en `vendor/tests/`. Es el mismo
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
// ni se versionan: son 174 MB.
//
// El corpus es opcional: si la carpeta no está, la clase se saltea entera.
@Tag("sintactico")
class CorpusExternoTest {

    private static final Path CORPORA =
            Path.of("..", "vendor", "tests", "resources", "parser", "corpora");
    private static final List<String> GENERADOS = List.of("base", "lambdas", "localvar");

    // Las cuatro familias en que las dos gramáticas difieren, cada una verificada
    // a mano. Un caso que cae en una de ellas no se compara: lo que este test
    // prueba es que NO aparezca una quinta.
    private enum Divergencia {

        // `"a\\"`. Aspectos Léxicos §2.3.3 da una regla POSICIONAL —la comilla de
        // cierre nunca está antecedida por `\`— y no una de apareamiento, así que
        // una barra no cancela a la anterior y el literal no cierra. Su fuzzer
        // asume la semántica de Java. Está documentado en
        // documentacion/lexico/decisiones-de-logica.md §3.5.
        STRING_TERMINADO_EN_BARRA,

        // `new Clase<Arg>()` y `new Clase<>()`. El primero lo genera la gramática
        // de la cátedra (`LlamadaConstructor -> new TipoReferencia ArgsActuales`,
        // con `TipoReferencia -> idClase TipoGenericoOpcional`); el segundo lo
        // habilita el logro de genericidad, que sólo admite el diamante al
        // instanciar. La otra gramática rechaza los dos.
        //
        // Ojo con la notación: `C` de una sola letra es un idGen y no un idClase,
        // así que `new C<T>()` lo rechazan las dos implementaciones.
        CREACION_GENERICA,

        // Sus corpus `base` y `localvar` se generaron sin el logro de Visibilidad
        // Mejorada, así que para él `public` sólo puede encabezar un constructor:
        // `public void m(){}`, `public int f;` y `public static void m(){}` son
        // errores, y un constructor SIN `public` también. Acá los cuatro son
        // válidos.
        SIN_VISIBILIDAD_MEJORADA,

        // Lo que queda son casos que las dos implementaciones RECHAZAN, en un
        // token distinto porque su gramática cubre menos construcciones. Que las
        // dos rechacen es lo único comparable acá.
        RECHAZAN_LAS_DOS_EN_DISTINTO_TOKEN
    }

    private static final Pattern BARRA_ANTES_DE_CERRAR = Pattern.compile("\\\\\\\\\"");
    // `new Clase<Arg>(`. El nombre de la clase es un idClase, o sea mayúscula MÁS
    // de un carácter: `new C<T>(` con `C` de una sola letra es un idGen y no una
    // clase, así que no es esta familia —lo rechazan las dos gramáticas—. Adentro
    // de los ángulos vale idGen o idClase, que es lo que dice la regla original
    // (`InstanciadoOParametrico -> idGen | idClase`).
    // El nombre de la clase es un idClase: mayúscula MÁS de un carácter. Entre los
    // ángulos, un idGen, un idClase, o nada —el diamante—.
    private static final Pattern CREACION_GENERICA =
            Pattern.compile("new\\s+[A-Z]\\w+\\s*<\\s*(?:[A-Z]\\w*\\s*)?>\\s*\\(");
    // Las dos formas que el logro de Visibilidad Mejorada habilita y la otra
    // gramática no tiene: un miembro con modificador, y un constructor sin `public`.
    private static final Pattern MODIFICADOR_O_CONSTRUCTOR_SIN_PUBLIC =
            Pattern.compile("^\\s*(public|private|static)\\b|^\\s*[A-Z]\\w*\\s*\\(");

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
        String declarado = fuente.split("\n", 2)[0].replaceFirst("^/+", "").trim();
        return salida.contains(declarado);
    }

    private static Divergencia divergenciaDe(String fuente, String salida) {
        if (BARRA_ANTES_DE_CERRAR.matcher(fuente).find()) {
            return Divergencia.STRING_TERMINADO_EN_BARRA;
        }
        if (CREACION_GENERICA.matcher(fuente).find()) {
            return Divergencia.CREACION_GENERICA;
        }
        if (salida.contains("[Error:")) {
            return Divergencia.RECHAZAN_LAS_DOS_EN_DISTINTO_TOKEN;
        }
        // Acepté y él rechaza: sólo vale si el renglón que él señala lleva un
        // modificador de visibilidad, que es lo que su gramática no tiene.
        return señalaUnMiembroConVisibilidad(fuente) ? Divergencia.SIN_VISIBILIDAD_MEJORADA : null;
    }

    // El caso declara `[Error:<lexema>|<línea>]`; se mira ese renglón del fuente.
    private static boolean señalaUnMiembroConVisibilidad(String fuente) {
        String[] renglones = fuente.split("\n", -1);
        var marca = Pattern.compile("\\[Error:.*\\|(\\d+)]").matcher(renglones[0]);
        if (!marca.find()) {
            return false;
        }
        int linea = Integer.parseInt(marca.group(1));
        if (linea < 1 || linea > renglones.length) {
            return false;
        }
        return MODIFICADOR_O_CONSTRUCTOR_SIN_PUBLIC.matcher(renglones[linea - 1]).find();
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
