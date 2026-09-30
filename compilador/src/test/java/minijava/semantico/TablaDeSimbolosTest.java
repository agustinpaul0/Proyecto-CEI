package minijava.semantico;

import minijava.semantico.entidades.Metodo;
import minijava.semantico.tabla.TablaDeSimbolos;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("semantico")
class TablaDeSimbolosTest extends BaseDeTablaDeSimbolos {

    @Test
    void recienInicializadaTieneSoloLasTresPredefinidasEnOrden() {
        TablaDeSimbolos.inicializar();

        assertEquals(List.of("Object", "String", "System"),
                TablaDeSimbolos.tipos().stream().map(t -> t.nombre()).toList());
    }

    // Documento de semántica §7.
    @Test
    void objectDeclaraDebugPrintEstaticoYToStringDeInstancia() {
        TablaDeSimbolos.inicializar();

        Metodo debugPrint = metodo("Object", "debugPrint", 1);
        assertTrue(debugPrint.esEstatico());
        assertEquals("void", debugPrint.retorno().nombre());
        assertEquals("int", debugPrint.parametros().getFirst().tipo().nombre());
        assertEquals("i", debugPrint.parametros().getFirst().nombre());
        Metodo toString = metodo("Object", "toString", 0);
        assertFalse(toString.esEstatico());
        assertEquals("String", toString.retorno().nombre());
        assertTrue(clase("Object").padre().isEmpty());
    }

    @Test
    void stringNoTieneMetodosPropiosYHeredaDeObject() throws Exception {
        TablaDeSimbolos.inicializar();
        assertEquals("Object", clase("String").padre().orElseThrow().nombre());
        assertEquals(List.of(), clase("String").metodos());

        TablaDeSimbolos.chequearDeclaraciones();
        TablaDeSimbolos.consolidar();

        assertEquals(List.of("debugPrint", "toString"), nombres(clase("String").metodos()));
    }

    @Test
    void systemConsolidadoSumaLosDosDeObject() throws Exception {
        TablaDeSimbolos.inicializar();
        assertEquals(10, clase("System").metodos().size());

        TablaDeSimbolos.chequearDeclaraciones();
        TablaDeSimbolos.consolidar();

        assertEquals(12, clase("System").metodos().size());
    }

    @ParameterizedTest
    @CsvSource({
            "read,0,int,", "printB,1,void,boolean", "printC,1,void,char", "printI,1,void,int",
            "printS,1,void,String", "println,0,void,", "printBln,1,void,boolean",
            "printCln,1,void,char", "printIln,1,void,int", "printSln,1,void,String",
    })
    void systemDeclaraSusDiezMetodosEstaticos(String nombre, int aridad, String retorno, String parametro) {
        TablaDeSimbolos.inicializar();

        Metodo metodo = metodo("System", nombre, aridad);
        assertTrue(metodo.esEstatico());
        assertEquals(retorno, metodo.retorno().nombre());
        if (aridad == 1) {
            assertEquals(parametro, metodo.parametros().getFirst().tipo().nombre());
        }
    }

    @Test
    void cadaPredefinidaTieneElConstructorPorDefecto() {
        TablaDeSimbolos.inicializar();

        for (String nombre : List.of("Object", "String", "System")) {
            assertEquals(0, clase(nombre).constructores().getFirst().aridad(), nombre);
        }
    }

    // Los testers de la cátedra llaman a main() muchas veces en la misma JVM.
    @Test
    void inicializarDescartaLoDeLaCompilacionAnterior(@TempDir Path dir) throws Exception {
        compilar(dir, "class Aa { }\n");
        assertTrue(TablaDeSimbolos.tipo("Aa").isPresent());

        TablaDeSimbolos.inicializar();

        assertTrue(TablaDeSimbolos.tipo("Aa").isEmpty());
        assertEquals(3, TablaDeSimbolos.tipos().size());
    }

    @Test
    void inicializarDescartaElRepetidoDeLaCompilacionAnterior(@TempDir Path dir) throws Exception {
        parsear(dir, "class Aa { }\nclass Aa { }\n");

        TablaDeSimbolos.inicializar();
        TablaDeSimbolos.chequearDeclaraciones();
    }

    // El primer repetido es el que se reporta, aunque haya otros después.
    @Test
    void seReportaElPrimerRepetidoDelFuente(@TempDir Path dir) {
        var error = rechazar(dir, "class Aa { int x; int x; }\nclass Aa { }\n");

        assertEquals("x", error.getLexemaOfensivo());
        assertEquals(1, error.getLinea());
    }

    @Test
    void elMensajeTieneLaFormaDeLasPautas(@TempDir Path dir) {
        var error = rechazar(dir, "class A1 { }\nclass A1 { }\n");

        assertEquals("Error Semantico en linea 2: La clase A1 ya esta declarada", error.getMessage());
    }

    @Test
    void elMensajeNombraElMetodoConSuAridad(@TempDir Path dir) {
        var error = rechazar(dir, "interface Ix { void m(int a); }\nclass Aa implements Ix { }\n");

        assertEquals("Error Semantico en linea 2: La clase Aa no implementa el metodo m/1 de la interfaz Ix",
                error.getMessage());
    }

    private List<String> nombres(List<Metodo> metodos) {
        return metodos.stream().map(Metodo::nombre).toList();
    }
}
