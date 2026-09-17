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
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

// Verificación diferencial contra un corpus ESCRITO POR OTRA PERSONA: 30.000
// fuentes generados por fuzzing a partir de su propia gramática de MiniJava, en
// `vendor/tests/`. Es el mismo argumento que `comparar_con_gramatica.py` —dos
// implementaciones independientes tienen que coincidir— pero con una gramática
// que no salió de este proyecto, así que no comparte ni siquiera las decisiones.
//
// Los casos escritos a mano de ese corpus (73, los `ext*` de resources/) ya están
// importados y se verifican con el resto. Acá van los generados, que NO se
// copian: son 145 MB y viven donde los dejó su autor.
//
// El corpus es opcional: si la carpeta no está, la clase se saltea entera.
@Tag("sintactico")
class CorpusExternoTest {

    private static final Path CORPORA =
            Path.of("..", "vendor", "tests", "resources", "parser", "corpora");
    private static final List<String> GENERADOS = List.of("base", "lambdas", "localvar");

    // Las cuatro familias en que las dos gramáticas difieren, cada una verificada
    // a mano. Un caso que cae en una de ellas no se compara: lo que prueba es que
    // NO aparezca ninguna quinta familia.
    private enum Divergencia {

        // `(x -> y).m()`, `(x -> y) + 1`, `(x -> y)[0]`. Su gramática produce la
        // lambda en <Primario>, así que una lambda parentizada puede ser operando.
        // Acá vive en <Expresion>, como el JLS, y el analizador cierra el hueco
        // del paréntesis. Verificado con javac: «lambda expression not expected
        // here» en los tres casos.
        LAMBDA_COMO_OPERANDO,

        // `"a\\"`. Aspectos Léxicos §2.3.3 da una regla POSICIONAL —la comilla de
        // cierre nunca está antecedida por `\`— y no una de apareamiento, así que
        // una barra no cancela a la anterior y el literal no cierra. Su fuzzer
        // asume la semántica de Java. Está documentado en
        // documentacion/lexico/decisiones-de-logica.md §3.5.
        STRING_TERMINADO_EN_BARRA,

        // `new C<T>()`. La gramática de la cátedra lo genera
        // (`LlamadaConstructor -> new TipoReferencia ArgsActuales`, con
        // `TipoReferencia -> idClase TipoGenericoOpcional`), y su gramática lo
        // rechaza.
        CREACION_GENERICA_CON_ARGUMENTO,

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
    private static final Pattern CREACION_GENERICA =
            Pattern.compile("new\\s+[A-Z]\\w*\\s*<\\s*\\w+\\s*>\\s*\\(");
    // Las dos formas que el logro de Visibilidad Mejorada habilita y su gramática
    // no tiene: un miembro con modificador, y un constructor SIN `public`.
    private static final Pattern MODIFICADOR_O_CONSTRUCTOR_PELADO =
            Pattern.compile("^\\s*(public|private|static)\\b|^\\s*[A-Z]\\w*\\s*\\(");

    @Test
    void elCorpusExternoNoRevelaNingunaDivergenciaSinDeclarar() throws IOException {
        assumeTrue(Files.isDirectory(CORPORA),
                "el corpus externo no está presente: " + CORPORA.toAbsolutePath().normalize());

        List<String> sinDeclarar = new ArrayList<>();
        int comparados = 0;
        int declarados = 0;

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
                        if (divergenciaDe(fuente, salida) != null) {
                            declarados++;
                            continue;
                        }
                        sinDeclarar.add(corpus + "/" + caso.getFileName() + ": " + primerRenglon(salida));
                    }
                }
            }
        }

        System.out.printf("corpus externo: %d casos, %d divergencias declaradas, %d sin declarar%n",
                comparados, declarados, sinDeclarar.size());
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
        if (salida.contains("una lambda no puede ser operando")) {
            return Divergencia.LAMBDA_COMO_OPERANDO;
        }
        if (BARRA_ANTES_DE_CERRAR.matcher(fuente).find()) {
            return Divergencia.STRING_TERMINADO_EN_BARRA;
        }
        if (CREACION_GENERICA.matcher(fuente).find()) {
            return Divergencia.CREACION_GENERICA_CON_ARGUMENTO;
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
        return MODIFICADOR_O_CONSTRUCTOR_PELADO.matcher(renglones[linea - 1]).find();
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
