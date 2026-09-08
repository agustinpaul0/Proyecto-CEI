package minijava.lexico.tokens;

import minijava.comun.io.ManejadorDeArchivo;
import minijava.comun.io.ManejadorDeArchivoEficiente;
import minijava.lexico.AnalizadorLexico;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// La tabla es la ÚNICA fuente de la correspondencia tipo <-> lexema para los 53
// tokens que la tienen fija. Estos dos tests la cierran por los dos lados: que no
// falte ninguno, y que el autómata emita para cada uno lo que la tabla promete.
class TablaDeLexemasTest {

    // Los 7 que leen su lexema del fuente y por eso NO están en la tabla.
    private static final Set<TipoDeToken> DE_LEXEMA_VARIABLE = EnumSet.of(
            TipoDeToken.ID_CLASE, TipoDeToken.ID_GEN, TipoDeToken.ID_MV,
            TipoDeToken.INT_LITERAL, TipoDeToken.CHAR_LITERAL,
            TipoDeToken.LIT_STRING, TipoDeToken.FLOAT_LITERAL);

    @Test
    void cadaUnoDeLos60TokensOTieneLexemaFijoOEsDeLexemaVariable() {
        for (TipoDeToken tipo : TipoDeToken.values()) {
            boolean fijo = TablaDeLexemas.lexemaFijoDe(tipo).isPresent();
            boolean variable = DE_LEXEMA_VARIABLE.contains(tipo);
            assertTrue(fijo ^ variable,
                    () -> tipo + (fijo ? " está en la tabla Y declarado como variable"
                            : " no está en la tabla ni declarado como de lexema variable"));
        }
    }

    @Test
    void laTablaCubreExactamenteLos53DeLexemaFijo() {
        assertEquals(60, TipoDeToken.values().length);
        assertEquals(53, TablaDeLexemas.lexemasFijos().size());
        assertEquals(23, TablaDeLexemas.palabrasClave().size());
    }

    // La inversa tiene que ser una biyección: dos tipos con el mismo lexema
    // harían que lexemaFijoDe() perdiera uno en silencio.
    @Test
    void laCorrespondenciaEntreTipoYLexemaFijoEsBiyectiva() {
        for (String lexema : TablaDeLexemas.lexemasFijos()) {
            TipoDeToken tipo = TablaDeLexemas.tipoDelLexemaFijo(lexema);
            assertEquals(lexema, TablaDeLexemas.lexemaFijoDe(tipo).orElseThrow(),
                    () -> "el lexema " + lexema + " no vuelve desde " + tipo);
        }
    }

    static Stream<String> lexemasFijos() {
        return TablaDeLexemas.lexemasFijos().stream();
    }

    // Ida y vuelta contra el autómata: cada lexema fijo, suelto en un fuente,
    // tiene que producir exactamente el tipo que la tabla le asigna. Cubre de una
    // los 10 símbolos de puntuación y los 17 operadores.
    @ParameterizedTest(name = "{0}")
    @MethodSource("lexemasFijos")
    void cadaLexemaFijoSueltoProduceSuPropioTipo(String lexema, @TempDir Path dir) throws Exception {
        TipoDeToken esperado = TablaDeLexemas.tipoDelLexemaFijo(lexema);
        if (esperado == TipoDeToken.EOF) {
            // El '$' no se lee del fuente: escrito en el fuente es un símbolo
            // inválido (Aspectos Léxicos §3.3), y eso lo fija dolarSuelto.java.
            return;
        }

        var token = analizar(dir, lexema).proximoToken();

        assertEquals(esperado, token.tipo(), lexema);
        assertEquals(lexema, token.lexema());
        assertEquals(1, token.linea());
    }

    // Y el fuente vacío da el EOF con el lexema que declara Aspectos Léxicos §2.5.
    @Test
    void elFuenteVacioProduceElEofConElLexemaDeLaTabla(@TempDir Path dir) throws Exception {
        var token = analizar(dir, "").proximoToken();

        assertEquals(TipoDeToken.EOF, token.tipo());
        assertEquals(TablaDeLexemas.lexemaFijoDe(TipoDeToken.EOF).orElseThrow(), token.lexema());
        assertEquals("$", token.lexema());
    }

    private AnalizadorLexico analizar(Path dir, String fuente) throws IOException {
        Path archivo = dir.resolve("fuente.mjava");
        Files.writeString(archivo, fuente);
        ManejadorDeArchivo manejador = new ManejadorDeArchivoEficiente();
        manejador.open(archivo.toString());
        return new AnalizadorLexico(manejador);
    }
}
