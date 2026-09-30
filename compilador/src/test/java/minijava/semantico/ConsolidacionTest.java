package minijava.semantico;

import minijava.semantico.entidades.Atributo;
import minijava.semantico.entidades.Metodo;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("semantico")
class ConsolidacionTest extends BaseDeTablaDeSimbolos {

    // El ejemplo de Clase 8, slides 21-22: cada clase ve la última versión de cada
    // método, y los de Object al principio.
    @Test
    void laTablaDeMetodosTieneLaUltimaVersionDeCadaUno(@TempDir Path dir) throws Exception {
        compilar(dir, """
                class Aa { void m1() { } void m2(int p) { } }
                class Bb extends Aa { void m2(int x) { } void m3() { } }
                class Cc extends Bb { void m3() { } void m4() { } }
                """);

        assertEquals(List.of("debugPrint", "toString", "m1", "m2", "m3", "m4"), nombres(clase("Cc").metodos()));
        assertEquals(1, metodo("Cc", "m1", 0).token().linea());
        assertEquals(2, metodo("Cc", "m2", 1).token().linea());
        assertEquals(3, metodo("Cc", "m3", 0).token().linea());
    }

    // Clase 8, slides 24-25.
    @Test
    void laTablaDeAtributosSumaLosDeTodosLosAncestros(@TempDir Path dir) throws Exception {
        compilar(dir, """
                class Aa { int v1; int v2; }
                class Bb extends Aa { String v3; boolean v4; }
                class Cc extends Bb { int v5; int v6; }
                """);

        assertEquals(List.of("v1", "v2", "v3", "v4", "v5", "v6"),
                clase("Cc").atributos().stream().map(Atributo::nombre).toList());
        assertEquals(List.of("v1", "v2", "v3", "v4"),
                clase("Bb").atributos().stream().map(Atributo::nombre).toList());
    }

    @Test
    void unaClaseDeclaradaAntesQueSuPadreSeConsolidaIgual(@TempDir Path dir) throws Exception {
        compilar(dir, "class Bb extends Aa { int y; }\nclass Aa { int x; void m() { } }\n");

        assertEquals(List.of("x", "y"), clase("Bb").atributos().stream().map(Atributo::nombre).toList());
        assertEquals(2, metodo("Bb", "m", 0).token().linea());
    }

    // Documento §5: con argumento se instancia, sin argumento se conserva la T.
    @Test
    void laHerenciaConArgumentoInstanciaLosTiposHeredados(@TempDir Path dir) throws Exception {
        compilar(dir, """
                class Caja<T> { T contenido; T[] varios; Caja<T> copia() { } void guardar(T x, int n) { } }
                class Texto extends Caja<String> { }
                """);

        assertEquals(List.of("String", "String[]"),
                clase("Texto").atributos().stream().map(a -> a.tipo().nombre()).toList());
        assertEquals("Caja<String>", metodo("Texto", "copia", 0).retorno().nombre());
        assertEquals("String", metodo("Texto", "guardar", 2).parametros().getFirst().tipo().nombre());
        assertEquals("Caja<T>", metodo("Caja", "copia", 0).retorno().nombre());
    }

    @Test
    void laHerenciaCrudaConservaElParametroDeTipo(@TempDir Path dir) throws Exception {
        compilar(dir, "class Caja<T> { T contenido; T obtener() { } }\nclass Cruda extends Caja { }\n");

        assertEquals("T", clase("Cruda").atributo("contenido").orElseThrow().tipo().nombre());
        assertEquals("T", metodo("Cruda", "obtener", 0).retorno().nombre());
    }

    @Test
    void laInstanciacionSeEncadenaPorVariosNiveles(@TempDir Path dir) throws Exception {
        compilar(dir, """
                class Caja<T> { T obtener() { } }
                class Otra<U> extends Caja<U> { }
                class Final extends Otra<Object> { }
                """);

        assertEquals("U", metodo("Otra", "obtener", 0).retorno().nombre());
        assertEquals("Object", metodo("Final", "obtener", 0).retorno().nombre());
    }

    @Test
    void unaInterfazHeredaLosMetodosDeLaQueExtiende(@TempDir Path dir) throws Exception {
        compilar(dir, "interface Ia<T> { T m(); }\ninterface Ib extends Ia<String> { void n(); }\n");

        assertEquals(List.of("m", "n"), nombres(interfaz("Ib").metodos()));
        assertEquals("String", metodo("Ib", "m", 0).retorno().nombre());
    }

    @Test
    void unaInterfazNoSumaLosMetodosDeObject(@TempDir Path dir) throws Exception {
        compilar(dir, "interface Ix { String toString(); }\n");

        assertEquals(List.of("toString"), nombres(interfaz("Ix").metodos()));
    }

    @Test
    void elMetodoPropioReemplazaAlHeredadoSoloEnLaHija(@TempDir Path dir) throws Exception {
        compilar(dir, "class Aa { void m() { } }\nclass Bb extends Aa { void m() { } }\n");

        assertEquals(2, metodo("Bb", "m", 0).token().linea());
        assertEquals(1, metodo("Aa", "m", 0).token().linea());
    }

    private List<String> nombres(List<Metodo> metodos) {
        return metodos.stream().map(Metodo::nombre).toList();
    }
}
