package minijava.sintactico.primeros;

import minijava.comun.config.ConfiguracionDelCompilador;
import minijava.comun.config.Logro;
import minijava.lexico.tokens.TipoDeToken;
import minijava.lexico.tokens.Token;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("sintactico")
class ConjuntoDePrimerosTest {

    private Token token(TipoDeToken tipo, String lexema) {
        return new Token(tipo, lexema, 1);
    }

    @Test
    void contieneAlTokenQueEstaEnElConjunto() {
        var primeros = ConjuntoDePrimeros.de(TipoDeToken.PR_CLASS, TipoDeToken.PR_INTERFACE);

        assertTrue(primeros.contiene(token(TipoDeToken.PR_CLASS, "class")));
        assertTrue(primeros.contiene(token(TipoDeToken.PR_INTERFACE, "interface")));
        assertFalse(primeros.contiene(token(TipoDeToken.PR_STATIC, "static")));
    }

    // El conjunto vacío es el Primeros de una alternativa que todavía no se
    // acumuló: no contiene nada, ni siquiera el fin de archivo.
    @Test
    void elConjuntoVacioNoContieneNingunToken() {
        var vacio = ConjuntoDePrimeros.de();

        assertTrue(vacio.getTokens().isEmpty());
        assertFalse(vacio.contiene(token(TipoDeToken.EOF, "$")));
    }

    @Test
    void laUnionJuntaLosDosSinTocarLosOriginales() {
        var uno = ConjuntoDePrimeros.de(TipoDeToken.PR_CLASS);
        var otro = ConjuntoDePrimeros.de(TipoDeToken.PR_INTERFACE);

        var union = uno.unido(otro);

        assertEquals(Set.of(TipoDeToken.PR_CLASS, TipoDeToken.PR_INTERFACE), union.getTokens());
        assertEquals(Set.of(TipoDeToken.PR_CLASS), uno.getTokens());
        assertEquals(Set.of(TipoDeToken.PR_INTERFACE), otro.getTokens());
    }

    @Test
    void laUnionConElVacioNoCambiaNada() {
        var uno = ConjuntoDePrimeros.de(TipoDeToken.PR_CLASS);

        assertEquals(uno.getTokens(), uno.unido(ConjuntoDePrimeros.de()).getTokens());
    }

    @Test
    void unTokenRepetidoSeCuentaUnaSolaVez() {
        var primeros = ConjuntoDePrimeros.de(TipoDeToken.PR_CLASS, TipoDeToken.PR_CLASS);

        assertEquals(1, primeros.getTokens().size());
    }

    @Test
    void elConjuntoExpuestoNoSePuedeModificarDesdeAfuera() {
        var tokens = ConjuntoDePrimeros.de(TipoDeToken.PR_CLASS).getTokens();

        assertThrows(UnsupportedOperationException.class, () -> tokens.add(TipoDeToken.PR_VOID));
    }

    @Test
    void unLexemaFijoSeDescribePorSuLexemaYUnoVariablePorSuNombreDeToken() {
        var primeros = ConjuntoDePrimeros.de(TipoDeToken.LLAVE_A, TipoDeToken.ID_CLASE);

        List<String> descripcion = primeros.describir();

        assertTrue(descripcion.contains("{"), descripcion.toString());
        assertTrue(descripcion.contains("idClase"), descripcion.toString());
    }

    // Prometer en un mensaje de error un token que el logro apagado sacó del
    // lenguaje sería ofrecer una entrada que el compilador igual rechazaría.
    @Test
    void unTokenDeUnLogroApagadoNoAparecEnLaDescripcion() {
        var primeros = ConjuntoDePrimeros.de(TipoDeToken.PR_CLASS, TipoDeToken.PR_FOR);

        assertEquals(List.of("class"),
                primeros.habilitadosEn(ConfiguracionDelCompilador.ninguno()).describir());
        assertTrue(primeros.habilitadosEn(ConfiguracionDelCompilador.todos()).describir()
                .contains("for"));
    }

    @Test
    void filtrarPorLogrosNoTocaElConjuntoOriginal() {
        var primeros = ConjuntoDePrimeros.de(TipoDeToken.PR_FOR);

        primeros.habilitadosEn(ConfiguracionDelCompilador.ninguno());

        assertEquals(Set.of(TipoDeToken.PR_FOR), primeros.getTokens());
    }

    @Test
    void unConjuntoEnteroDeTokensDeshabilitadosQuedaVacio() {
        var primeros = ConjuntoDePrimeros.de(TipoDeToken.PR_FOR, TipoDeToken.SIGNO_PREGUNTA);

        assertTrue(primeros.habilitadosEn(ConfiguracionDelCompilador.ninguno()
                .con(Logro.LAMBDA)).getTokens().isEmpty());
    }

    // Las constantes tienen que coincidir con lo que calcula el verificador de la
    // gramática: si divergen, el código deja de leerse contra el informe.
    @Test
    void lasConstantesDelNucleoCoincidenConLaGramatica() {
        assertEquals(Set.of(TipoDeToken.PR_CLASS), PrimerosDelNucleo.CLASE.getTokens());
        assertEquals(Set.of(TipoDeToken.PR_INTERFACE), PrimerosDelNucleo.INTERFAZ.getTokens());
        assertEquals(Set.of(TipoDeToken.OP_MENOR), PrimerosDelNucleo.GENERICIDAD.getTokens());
        assertEquals(Set.of(TipoDeToken.OP_MENOR), PrimerosDelNucleo.TIPO_GENERICO.getTokens());
        assertEquals(Set.of(TipoDeToken.PR_EXTENDS), PrimerosDelNucleo.HERENCIA_POR_EXTENDS.getTokens());
        assertEquals(Set.of(TipoDeToken.PR_IMPLEMENTS),
                PrimerosDelNucleo.HERENCIA_POR_IMPLEMENTS.getTokens());
        assertEquals(Set.of(TipoDeToken.ID_GEN, TipoDeToken.ID_CLASE),
                PrimerosDelNucleo.ARGUMENTO_DE_TIPO.getTokens());
    }
}
