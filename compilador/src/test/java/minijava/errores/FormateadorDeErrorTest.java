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

        // Cuatro del reporte elegante más el renglón en blanco de la slide 25.
        assertEquals(5, lineas.size());
        assertEquals("", lineas.get(3));
        assertEquals("[Error:#|2]", lineas.get(4));
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

    // El lexema entero ES lo ofensivo en los diez tipos que quedan, y el
    // encabezado no puede degenerar en un "null".
    @Test
    void todoTipoDeErrorConservaElEncabezadoDeUnSoloDato() {
        var formateador = new FormateadorDeError(ConfiguracionDelCompilador.DEFAULT);

        for (TipoDeErrorLexico tipo : TipoDeErrorLexico.values()) {
            String encabezado = formateador.formatear(
                    new ExcepcionLexica(tipo, "x", new PosicionLexica(1, 1, "x"))).get(0);
            assertFalse(encabezado.contains("null"), () -> tipo + ": " + encabezado);
        }
    }

    // El detalle normaliza tabs y el '^' usa el mismo ancho visual que el detalle.
    @Test
    void elCaretSeAlineaConTabsOCaracteresAnchos() {
        var formateador = new FormateadorDeError(ConfiguracionDelCompilador.DEFAULT);

        List<String> conTab = formateador.formatear(
                new ExcepcionLexica(TipoDeErrorLexico.CARACTER_DESCONOCIDO, "#",
                        new PosicionLexica(1, 3, "a\t#")));
        List<String> conCjk = formateador.formatear(
                new ExcepcionLexica(TipoDeErrorLexico.CARACTER_DESCONOCIDO, "#",
                        new PosicionLexica(1, 3, "\u4E2D\u6587#")));

        assertEquals(8, columnaDelCaret(conTab));
        assertEquals(4, columnaDelCaret(conCjk));
        assertEquals("Detalle: a       #", conTab.get(1));
        assertEquals("Detalle: 中文#", conCjk.get(1));
    }

    @Test
    void unaMarcaCombinanteSeEscapaParaQueElCaretSeaVisible() {
        List<String> lineas = new FormateadorDeError(ConfiguracionDelCompilador.DEFAULT)
                .formatear(new ExcepcionLexica(TipoDeErrorLexico.CARACTER_DESCONOCIDO, "\\u20cc",
                        new PosicionLexica(1, 2, "(⃌>%")));

        assertEquals("Detalle: (\\u20cc>%", lineas.get(1));
        assertEquals("Detalle: ".length() + 1, lineas.get(2).indexOf('^'));
        assertEquals("[Error:\\u20cc|1]", lineas.get(4));
    }

    private int columnaDelCaret(List<String> lineas) {
        return lineas.get(2).indexOf('^') - "Detalle: ".length();
    }

    @Test
    void conReporteEleganteDeshabilitadoSoloImprimeElCodigoDeContrato() {
        var configuracion = new ConfiguracionDelCompilador(true, true, false, true);

        List<String> lineas = new FormateadorDeError(configuracion).formatear(excepcionDeEjemplo());

        assertEquals(List.of("[Error:#|2]"), lineas);
    }
}
