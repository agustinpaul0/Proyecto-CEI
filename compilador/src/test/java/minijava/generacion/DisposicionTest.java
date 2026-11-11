package minijava.generacion;

import minijava.semantico.entidades.Clase;
import minijava.semantico.entidades.ClaseOInterfaz;
import minijava.semantico.entidades.ClaveDeMetodo;
import minijava.semantico.entidades.Metodo;
import minijava.semantico.tabla.TablaDeSimbolos;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Los desplazamientos que fija el análisis semántico (Clase 16, slides 5 y 36):
// la VT y el CIR de cada clase, los parámetros, el resultado y `this`.
@Tag("generacion")
class DisposicionTest {

    private static final String JERARQUIA = """
            interface Ix {
                int i1();
                int i2();
            }
            interface Iy extends Ix {
                int i3();
                int i1();
            }
            class Aa {
                int x;
                int y;
                void a1() { }
                static void estatico() { }
                void a2() { }
            }
            class Bb extends Aa {
                int z;
                void b1() { }
                void a2() { }
                String toString() { return ""; }
            }
            class Cc extends Bb {
                void a1() { }
                void c1(int p, int q) { }
            }
            class Dd implements Iy {
                int extra() { return 0; }
                int i3() { return 0; }
                int i2() { return 0; }
                int i1() { return 0; }
            }
            class Ee extends Dd {
                int i2() { return 1; }
                int nueva() { return 1; }
            }
            class Init {
                static void main() { }
                static int f(int a, int b, int c) { return a; }
                int g(int a, int b) { return a; }
            }
            """;

    private static ClaseOInterfaz tipo(String nombre) {
        assertTrue(Compilacion.deTexto(JERARQUIA).exitosa());
        return TablaDeSimbolos.tipo(nombre).orElseThrow();
    }

    private static List<String> vt(String nombre) {
        return tipo(nombre).disposicionDeVT().stream().map(ClaveDeMetodo::toString).toList();
    }

    @Test
    void laVTDeObjectTieneSoloToString() {
        assertEquals(List.of("toString/0"), vt("Object"));
        assertEquals(List.of("toString/0"), vt("String"));
        assertEquals(List.of("toString/0"), vt("System"));
    }

    @Test
    void losMetodosNuevosVanDespuesDeLosHeredadosYLosEstaticosNoEntran() {
        assertEquals(List.of("toString/0", "a1/0", "a2/0"), vt("Aa"));
    }

    @Test
    void unaRedefinicionReusaLaEntradaHeredada() {
        assertEquals(List.of("toString/0", "a1/0", "a2/0", "b1/0"), vt("Bb"));
        assertEquals(List.of("toString/0", "a1/0", "a2/0", "b1/0", "c1/2"), vt("Cc"));
    }

    @Test
    void laVTDeUnaInterfazEmpiezaConToStringYLaDeLaExtendida() {
        assertEquals(List.of("toString/0", "i1/0", "i2/0"), vt("Ix"));
        assertEquals(List.of("toString/0", "i1/0", "i2/0", "i3/0"), vt("Iy"));
    }

    // Apunte §9.1: la interfaz es un prefijo de la VT de quien la implementa, aunque
    // la clase declare sus métodos en otro orden.
    @Test
    void laVTDeUnaClaseQueImplementaEmpiezaConLaDeSuInterfaz() {
        assertEquals(List.of("toString/0", "i1/0", "i2/0", "i3/0", "extra/0"), vt("Dd"));
        assertEquals(List.of("toString/0", "i1/0", "i2/0", "i3/0", "extra/0", "nueva/0"), vt("Ee"));
    }

    @Test
    void elOffsetEsElIndiceEnLaVT() {
        ClaseOInterfaz cc = tipo("Cc");
        assertEquals(0, cc.offsetEnVT(new ClaveDeMetodo("toString", 0)));
        assertEquals(4, cc.offsetEnVT(new ClaveDeMetodo("c1", 2)));
        assertThrows(IllegalStateException.class, () -> cc.offsetEnVT(new ClaveDeMetodo("estatico", 0)));
    }

    @Test
    void cadaEntradaDeLaVTApuntaALaImplementacionQueCorresponde() {
        Clase cc = (Clase) tipo("Cc");
        assertEquals("Cc", cc.metodo(new ClaveDeMetodo("a1", 0)).orElseThrow().declarante().nombre());
        assertEquals("Bb", cc.metodo(new ClaveDeMetodo("a2", 0)).orElseThrow().declarante().nombre());
        assertEquals("Bb", cc.metodo(new ClaveDeMetodo("toString", 0)).orElseThrow().declarante().nombre());
    }

    // Apunte §9: el 0 del CIR es la VT, y un atributo heredado conserva su offset.
    @Test
    void losAtributosHeredadosConservanSuOffset() {
        Clase aa = (Clase) tipo("Aa");
        Clase cc = (Clase) tipo("Cc");
        assertEquals(1, aa.offsetDeAtributo("x"));
        assertEquals(2, aa.offsetDeAtributo("y"));
        assertEquals(1, cc.offsetDeAtributo("x"));
        assertEquals(2, cc.offsetDeAtributo("y"));
        assertEquals(3, cc.offsetDeAtributo("z"));
        assertEquals(3, aa.tamanioDelCIR());
        assertEquals(4, cc.tamanioDelCIR());
        assertEquals(1, ((Clase) tipo("Init")).tamanioDelCIR());
    }

    // Apunte §10, tabla de la p. 19.
    @Test
    void losParametrosElResultadoYLasCeldasDeUnaUnidadEstatica() {
        Metodo f = ((Clase) tipo("Init")).metodo(new ClaveDeMetodo("f", 3)).orElseThrow();
        assertEquals(5, f.offsetDeParametro(0));
        assertEquals(4, f.offsetDeParametro(1));
        assertEquals(3, f.offsetDeParametro(2));
        assertEquals(6, f.offsetDeRetorno());
        assertEquals(3, f.celdasALiberar());
    }

    @Test
    void losParametrosElResultadoYLasCeldasDeUnaUnidadDeInstancia() {
        Metodo g = ((Clase) tipo("Init")).metodo(new ClaveDeMetodo("g", 2)).orElseThrow();
        assertEquals(5, g.offsetDeParametro(0));
        assertEquals(4, g.offsetDeParametro(1));
        assertEquals(6, g.offsetDeRetorno());
        assertEquals(3, g.celdasALiberar());
        var constructor = ((Clase) tipo("Aa")).constructor(0).orElseThrow();
        assertEquals(4, constructor.offsetDeRetorno());
        assertEquals(1, constructor.celdasALiberar());
    }

    @Test
    void lasEtiquetasDeUnidadesUsanAlDeclarante() {
        Clase cc = (Clase) tipo("Cc");
        assertEquals("metodo@$42$b@a2@0", cc.metodo(new ClaveDeMetodo("a2", 0)).orElseThrow().etiqueta());
        assertEquals("constructor@$43$c@0", cc.constructor(0).orElseThrow().etiqueta());
    }
}
