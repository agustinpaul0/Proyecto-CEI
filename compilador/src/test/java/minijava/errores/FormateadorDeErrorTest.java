package minijava.errores;

import minijava.config.ConfiguracionDelCompilador;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FormateadorDeErrorTest {

    private ExcepcionLexica excepcionDeEjemplo() {
        return new ExcepcionLexica(TipoDeErrorLexico.CARACTER_DESCONOCIDO, "#", 2, 5, "v1 + # chau");
    }

    @Test
    void conConfiguracionPorDefectoIncluyeLasCuatroLineas() {
        List<String> lineas = new FormateadorDeError(ConfiguracionDelCompilador.DEFAULT)
                .formatear(excepcionDeEjemplo());

        assertEquals(4, lineas.size());
        assertEquals("[Error:#|2]", lineas.get(3));
    }

    // El encabezado sale de TipoDeErrorLexico. Para CARACTER_DESCONOCIDO el
    // texto es el de la slide 43 y no puede cambiar; el resto de los tipos
    // estrena razón propia, que es lo que permite decir "sin cerrar".
    @Test
    void elEncabezadoDescribeElTipoDeErrorYNoUnaFraseGenerica() {
        var formateador = new FormateadorDeError(ConfiguracionDelCompilador.DEFAULT);

        assertEquals("Error Léxico en línea 2, columna 5: # no es un símbolo válido",
                formateador.formatear(excepcionDeEjemplo()).get(0));
        assertEquals("Error Léxico en línea 3, columna 2: charLiteral sin cerrar: 'a",
                formateador.formatear(
                        new ExcepcionLexica(TipoDeErrorLexico.CHAR_SIN_CERRAR, "'a", 3, 2, "'a")).get(0));
        assertEquals("Error Léxico en línea 1, columna 3: "
                        + "charLiteral mal formado, contiene más de un carácter: 'a",
                formateador.formatear(
                        new ExcepcionLexica(TipoDeErrorLexico.CHAR_MAL_FORMADO, "'a", 1, 3, "'ab'")).get(0));
    }

    // El '^' se posiciona contando CARACTERES de la linea fuente, uno por
    // columna. Con un tabulador o con caracteres CJK (que la terminal dibuja
    // con dos celdas) el caret sigue apuntando al caracter correcto en
    // terminos de indice, pero se ve corrido en pantalla. Es una limitacion
    // conocida y aceptada: corregirla exigiria conocer el ancho de
    // renderizado, que depende de la terminal. Lo que NO puede pasar es que
    // el indice se vaya del largo de la linea, y eso es lo que se fija aca y
    // en la propiedad de PropiedadesDelReporteTest.
    @Test
    void elCaretSeUbicaPorIndiceDeCaracterAunConTabsOCaracteresAnchos() {
        var formateador = new FormateadorDeError(ConfiguracionDelCompilador.DEFAULT);

        List<String> conTab = formateador.formatear(
                new ExcepcionLexica(TipoDeErrorLexico.CARACTER_DESCONOCIDO, "#", 1, 3, "a\t#"));
        List<String> conCjk = formateador.formatear(
                new ExcepcionLexica(TipoDeErrorLexico.CARACTER_DESCONOCIDO, "#", 1, 3, "\u4E2D\u6587#"));

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
