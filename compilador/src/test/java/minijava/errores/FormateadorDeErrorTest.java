package minijava.errores;

import minijava.config.ConfiguracionDelCompilador;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class FormateadorDeErrorTest {

    private ExcepcionLexica excepcionDeEjemplo() {
        return new ExcepcionLexica(TipoDeErrorLexico.CARACTER_DESCONOCIDO, "#",
                new PosicionLexica(2, 5, "v1 + # chau"));
    }

    @Test
    void conConfiguracionPorDefectoIncluyeLasCuatroLineas() {
        List<String> lineas = new FormateadorDeError(ConfiguracionDelCompilador.DEFAULT)
                .formatear(excepcionDeEjemplo());

        assertEquals(4, lineas.size());
        assertEquals("[Error:#|2]", lineas.get(3));
    }

    // Para CARACTER_DESCONOCIDO el texto es el de la slide 43 y no puede cambiar;
    // el resto tiene razón propia.
    @Test
    void elEncabezadoDescribeElTipoDeErrorYNoUnaFraseGenerica() {
        var formateador = new FormateadorDeError(ConfiguracionDelCompilador.DEFAULT);

        assertEquals("Error Léxico en línea 2, columna 5: # no es un símbolo válido",
                formateador.formatear(excepcionDeEjemplo()).get(0));
        assertEquals("Error Léxico en línea 3, columna 2: charLiteral sin cerrar: 'a",
                formateador.formatear(
                        new ExcepcionLexica(TipoDeErrorLexico.CHAR_SIN_CERRAR, "'a",
                                new PosicionLexica(3, 2, "'a"))).get(0));
        assertEquals("Error Léxico en línea 1, columna 3: "
                        + "charLiteral mal formado, contiene más de un carácter: 'a",
                formateador.formatear(
                        new ExcepcionLexica(TipoDeErrorLexico.CHAR_MAL_FORMADO, "'a",
                                new PosicionLexica(1, 3, "'ab'"))).get(0));
    }

    // Pautas slide 24 obliga al "lexema armado hasta el error", que no ES la
    // secuencia: sin nombrarla aparte el mensaje se lee al revés. El encabezado
    // lleva las dos; el código de contrato, sólo el lexema.
    @Test
    void elEncabezadoDeUnEscapeInvalidoNombraLaSecuenciaYElLexemaArmado() {
        var formateador = new FormateadorDeError(ConfiguracionDelCompilador.DEFAULT);
        var excepcion = new ExcepcionLexica(TipoDeErrorLexico.STRING_ESCAPE_INVALIDO, "\"a\\q",
                new PosicionLexica(7, 4, "\"a\\q b\\z c\""), "\\q");

        List<String> lineas = formateador.formatear(excepcion);

        assertEquals("Error Léxico en línea 7, columna 4: "
                + "litString con secuencia de escape inválida \\q: \"a\\q", lineas.get(0));
        assertEquals("[Error:\"a\\q|7]", lineas.get(3));
    }

    // Los otros diez no tienen fragmento: el lexema entero ES lo ofensivo, y el
    // encabezado no puede degenerar en un "null".
    @Test
    void unErrorSinFragmentoConservaElEncabezadoDeUnSoloDato() {
        var formateador = new FormateadorDeError(ConfiguracionDelCompilador.DEFAULT);

        for (TipoDeErrorLexico tipo : TipoDeErrorLexico.values()) {
            String encabezado = formateador.formatear(
                    new ExcepcionLexica(tipo, "x", new PosicionLexica(1, 1, "x"))).get(0);
            assertFalse(encabezado.contains("null"), () -> tipo + ": " + encabezado);
        }
    }

    // El '^' cuenta CARACTERES, no celdas de terminal: con un tab o con CJK apunta
    // al índice correcto pero se ve corrido. Limitación aceptada; lo que se fija
    // acá es que el índice nunca se vaya del largo de la línea.
    @Test
    void elCaretSeUbicaPorIndiceDeCaracterAunConTabsOCaracteresAnchos() {
        var formateador = new FormateadorDeError(ConfiguracionDelCompilador.DEFAULT);

        List<String> conTab = formateador.formatear(
                new ExcepcionLexica(TipoDeErrorLexico.CARACTER_DESCONOCIDO, "#",
                        new PosicionLexica(1, 3, "a\t#")));
        List<String> conCjk = formateador.formatear(
                new ExcepcionLexica(TipoDeErrorLexico.CARACTER_DESCONOCIDO, "#",
                        new PosicionLexica(1, 3, "\u4E2D\u6587#")));

        for (List<String> lineas : List.of(conTab, conCjk)) {
            String detalle = lineas.get(1);
            String caret = lineas.get(2);
            int indice = caret.indexOf('^') - "Detalle: ".length();
            assertEquals(2, indice, () -> "el caret deberia caer en el indice 2: " + lineas);
            assertEquals('#', detalle.charAt("Detalle: ".length() + indice));
        }
    }

    @Test
    void conReporteEleganteDeshabilitadoSoloImprimeElCodigoDeContrato() {
        var configuracion = new ConfiguracionDelCompilador(true, true, false, true);

        List<String> lineas = new FormateadorDeError(configuracion).formatear(excepcionDeEjemplo());

        assertEquals(List.of("[Error:#|2]"), lineas);
    }
}
