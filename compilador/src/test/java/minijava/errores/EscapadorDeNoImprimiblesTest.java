package minijava.errores;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

// Extraído de FabricaDeExcepcionesLexicas, donde sólo se ejercitaba de rebote a
// través de un error léxico completo. Lo que protege: un invisible crudo dentro
// del lexema deja el encabezado ilegible y, si es un salto, parte
// [Error:Lexema|NroLinea] en dos rompiendo el formato de un elemento por línea.
//
// Los invisibles se escriben acá con escapes y nunca crudos: un archivo de test
// con un NUL o un BOM adentro es justamente lo que este código existe para evitar.
class EscapadorDeNoImprimiblesTest {

    @Test
    void losCincoEscapesConNombreDeJavaSeEscribenConEl() {
        assertEquals("a\\tb", EscapadorDeNoImprimibles.escapar("a\tb"));
        assertEquals("a\\nb", EscapadorDeNoImprimibles.escapar("a\nb"));
        assertEquals("a\\rb", EscapadorDeNoImprimibles.escapar("a\rb"));
        assertEquals("a\\fb", EscapadorDeNoImprimibles.escapar("a\fb"));
        assertEquals("a\\bb", EscapadorDeNoImprimibles.escapar("a\bb"));
    }

    @Test
    void unControlSinNombreVaEnNotacionUnicode() {
        assertEquals("\\u0000", EscapadorDeNoImprimibles.escapar("\0"));
        // El centinela de EOF: un byte legítimo del fuente que igual hay que mostrar.
        assertEquals("\\u001a", EscapadorDeNoImprimibles.escapar("\032"));
    }

    // El BOM es categoría FORMAT, no ISO control: sin esa rama pasaría crudo.
    @Test
    void elBomYLosCaracteresDeFormatoTambienSeEscapan() {
        assertEquals("\\ufeff", EscapadorDeNoImprimibles.escapar("\uFEFF"));
        assertEquals("a\\u200bb", EscapadorDeNoImprimibles.escapar("a\u200Bb"));
    }

    // El espacio común se deja; cualquier otro se ve igual que él y por eso
    // confunde: el no-rompible es un error léxico frecuente de copiar y pegar.
    @Test
    void soloElEspacioComunSobreviveSinEscapar() {
        assertEquals("a b", EscapadorDeNoImprimibles.escapar("a b"));
        assertEquals("a\\u00a0b", EscapadorDeNoImprimibles.escapar("a\u00A0b"));
    }

    // Un par bien formado es un carácter que se ve —un emoji—: escaparlo empeora
    // el mensaje. Uno suelto no se ve, y hay que nombrarlo.
    @Test
    void elParSurrogadoBienFormadoQuedaCrudoYElSueltoNo() {
        String emoji = "😀";

        assertEquals(emoji, EscapadorDeNoImprimibles.escapar(emoji));
        assertEquals("\\ud83d", EscapadorDeNoImprimibles.escapar("\uD83D"));
        assertEquals("\\ude00", EscapadorDeNoImprimibles.escapar("\uDE00"));
        // Invertidos no forman par: los dos quedan sueltos.
        assertEquals("\\ude00\\ud83d", EscapadorDeNoImprimibles.escapar("\uDE00\uD83D"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "v1", "\"a\\q", "1234567890", "ñ", "中文"})
    void loQueYaEsImprimibleNoSeToca(String texto) {
        assertEquals(texto, EscapadorDeNoImprimibles.escapar(texto));
    }

    // Nada del compilador le pasa null hoy, pero el escapador es de uso general y
    // envolver un null en la cadena "null" ensuciaría el mensaje del error.
    @Test
    void unTextoNuloSePropagaSinEnvolverloEnLaCadenaNull() {
        assertNull(EscapadorDeNoImprimibles.escapar(null));
    }
}
