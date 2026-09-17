package minijava.sintactico;

import minijava.aplicacion.ModuloPrincipalSintactico;
import minijava.comun.salida.SalidaEnMemoria;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.Tag;
import net.jqwik.api.constraints.IntRange;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Invariantes de robustez que tienen que sostenerse para CUALQUIER entrada, no
// sólo para los casos del corpus. La entrada es una sopa de tokens de MiniJava
// al azar: casi siempre inválida, que es justo lo que se quiere ejercitar.
@Tag("sintactico")
class AnalizadorSintacticoPropiedadesTest {

    // Uno solo reescrito en cada intento: jqwik corre miles por propiedad e
    // inundaba /tmp con un archivo por intento.
    private static final Path ARCHIVO = crearArchivo();

    private static Path crearArchivo() {
        try {
            Path archivo = Files.createTempFile("minijava-sintactico-", ".mjava");
            archivo.toFile().deleteOnExit();
            return archivo;
        } catch (IOException excepcion) {
            throw new UncheckedIOException(excepcion);
        }
    }

    private static final List<String> VOCABULARIO = List.of(
            "class", "interface", "extends", "implements", "static", "public", "private",
            "void", "int", "char", "boolean", "float", "var", "if", "else", "while", "for",
            "return", "this", "new", "null", "true", "false",
            "{", "}", "(", ")", "[", "]", ";", ",", ".", ":", "?", "->",
            "=", "==", "!=", "<", ">", "<=", ">=", "&&", "||", "!", "+", "-", "*", "/", "%",
            "++", "--",
            "C1", "Persona", "T", "x", "metodo", "1", "42", "'a'", "\"hola\"", "1.5");

    @Provide
    Arbitrary<String> sopasDeTokens() {
        return Arbitraries.of(VOCABULARIO).list().ofMinSize(0).ofMaxSize(60)
                .map(tokens -> String.join(" ", tokens));
    }

    private List<String> analizar(String fuente) {
        try {
            Files.writeString(ARCHIVO, fuente);
            SalidaEnMemoria salida = new SalidaEnMemoria();
            new ModuloPrincipalSintactico(salida).ejecutar(ARCHIVO.toString());
            return salida.getLineas();
        } catch (IOException excepcion) {
            throw new UncheckedIOException(excepcion);
        }
    }

    private List<String> codigos(List<String> salida) {
        return salida.stream().filter(linea -> linea.startsWith("[")).toList();
    }

    // El módulo nunca deja escapar una excepción: o reporta y sigue, o termina.
    @Property(tries = 400)
    void elModuloSiempreTerminaYNuncaDejaEscaparUnaExcepcion(
            @ForAll("sopasDeTokens") String fuente) {
        assertFalse(analizar(fuente).isEmpty(), fuente);
    }

    // O se aceptó y el contrato de éxito es exacto, o hay al menos un código de
    // error; nunca las dos cosas, nunca ninguna.
    @Property(tries = 400)
    void oSeAceptaOSeReportaAlMenosUnError(@ForAll("sopasDeTokens") String fuente) {
        List<String> salida = analizar(fuente);
        List<String> codigos = codigos(salida);

        assertFalse(codigos.isEmpty(), fuente + " -> " + salida);
        if (codigos.contains("[SinErrores]")) {
            assertEquals(List.of("Compilacion Exitosa", "", "[SinErrores]"), salida, fuente);
        } else {
            assertTrue(codigos.stream().allMatch(c -> c.startsWith("[Error:")),
                    fuente + " -> " + salida);
        }
    }

    // El lexema que reporta el error salió del fuente, o es el `$` del EOF, y su
    // línea cae dentro del archivo.
    @Property(tries = 400)
    void elTokenDelErrorSiempreSalioDelFuente(@ForAll("sopasDeTokens") String fuente) {
        for (String codigo : codigos(analizar(fuente))) {
            if (codigo.equals("[SinErrores]")) {
                continue;
            }
            String cuerpo = codigo.substring("[Error:".length(), codigo.length() - 1);
            int separador = cuerpo.lastIndexOf('|');
            String lexema = cuerpo.substring(0, separador);
            int linea = Integer.parseInt(cuerpo.substring(separador + 1));

            assertTrue(lexema.equals("$") || fuente.contains(lexema),
                    fuente + " -> lexema ajeno al fuente: " + lexema);
            assertTrue(linea >= 1 && linea <= 2, fuente + " -> linea fuera de rango: " + linea);
        }
    }

    // Con Modo Pánico el análisis avanza: cada error cae en una línea que no
    // retrocede, y no puede haber más errores que tokens.
    @Property(tries = 400)
    void conModoPanicoLosErroresAvanzan(@ForAll("sopasDeTokens") String fuente) {
        List<String> codigos = codigos(analizar(fuente));
        int tokens = fuente.isBlank() ? 0 : fuente.split("\\s+").length;

        assertTrue(codigos.size() <= tokens + 1,
                fuente + " -> mas errores que tokens: " + codigos);
    }

    // Un fuente sin ningún token es siempre válido: cero clases es un programa.
    @Property(tries = 50)
    void unFuenteDeSoloBlancosSiempreSeAcepta(@ForAll @IntRange(min = 0, max = 40) int blancos) {
        assertEquals(List.of("Compilacion Exitosa", "", "[SinErrores]"),
                analizar(" \n\t".repeat(blancos)));
    }
}
