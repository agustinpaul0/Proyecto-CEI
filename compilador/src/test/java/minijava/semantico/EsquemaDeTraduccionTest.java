package minijava.semantico;

import minijava.semantico.entidades.Atributo;
import minijava.semantico.entidades.Metodo;
import minijava.semantico.entidades.Parametro;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Lo que las acciones de la EDT dejan en la tabla al terminar de parsear, antes
// de chequear nada.
@Tag("semantico")
class EsquemaDeTraduccionTest extends BaseDeTablaDeSimbolos {

    @Test
    void unaClaseSinExtendsHeredaDeObject(@TempDir Path dir) throws Exception {
        parsear(dir, "class Aa { }\n");

        assertEquals("Object", clase("Aa").padre().orElseThrow().nombre());
        assertTrue(clase("Aa").interfaz().isEmpty());
    }

    @Test
    void unaClaseQueImplementaTambienHeredaDeObject(@TempDir Path dir) throws Exception {
        parsear(dir, "interface Ix { }\nclass Aa implements Ix { }\n");

        assertEquals("Object", clase("Aa").padre().orElseThrow().nombre());
        assertEquals("Ix", clase("Aa").interfaz().orElseThrow().nombre());
    }

    @Test
    void laHerenciaGuardaElArgumentoGenerico(@TempDir Path dir) throws Exception {
        parsear(dir, "class Caja<T> { }\nclass Aa extends Caja<String> { }\ninterface Ib extends Ix<T> { }\n");

        assertEquals("Caja<String>", clase("Aa").padre().orElseThrow().nombre());
        assertEquals("T", clase("Caja").parametroDeTipo().orElseThrow().lexema());
        assertEquals("Ix<T>", interfaz("Ib").extendida().orElseThrow().nombre());
        assertTrue(clase("Aa").parametroDeTipo().isEmpty());
    }

    @Test
    void cadaAtributoGuardaSuTipoCompleto(@TempDir Path dir) throws Exception {
        parsear(dir, """
                class Aa<T> {
                  int a;
                  float[][] b;
                  T c;
                  T[] d;
                  Caja<String>[] e;
                  public Aa f;
                  private Caja<T> g = null;
                }
                """);

        assertEquals(List.of("int", "float[][]", "T", "T[]", "Caja<String>[]", "Aa", "Caja<T>"),
                clase("Aa").atributos().stream().map(a -> a.tipo().nombre()).toList());
        Atributo primero = clase("Aa").atributos().getFirst();
        assertEquals("a", primero.nombre());
        assertEquals(2, primero.token().linea());
    }

    @Test
    void cadaFormaDeMetodoGuardaRetornoYModificador(@TempDir Path dir) throws Exception {
        parsear(dir, """
                class Aa {
                  void a() { }
                  static int b() { }
                  public static char c() { }
                  static public Aa d() { }
                  private boolean e() { }
                  T[] f() { }
                  Caja<String> g() { }
                }
                """);

        assertEquals(List.of("void", "int", "char", "Aa", "boolean", "T[]", "Caja<String>"),
                clase("Aa").metodos().stream().map(m -> m.retorno().nombre()).toList());
        assertEquals(List.of(false, true, true, true, false, false, false),
                clase("Aa").metodos().stream().map(Metodo::esEstatico).toList());
    }

    @Test
    void losParametrosQuedanEnOrdenConSuPosicion(@TempDir Path dir) throws Exception {
        parsear(dir, "class Aa {\n  void m(int a, Aa b, char[] c) { }\n}\n");

        List<Parametro> parametros = metodo("Aa", "m", 3).parametros();
        assertEquals(List.of("a", "b", "c"), parametros.stream().map(Parametro::nombre).toList());
        assertEquals(List.of("int", "Aa", "char[]"),
                parametros.stream().map(p -> p.tipo().nombre()).toList());
        assertEquals(List.of(0, 1, 2), parametros.stream().map(Parametro::posicion).toList());
    }

    @Test
    void losMetodosDeInterfazVanALaInterfaz(@TempDir Path dir) throws Exception {
        parsear(dir, "interface Ix {\n  int m(char c);\n  public void n();\n}\n");

        assertEquals(List.of("m", "n"), interfaz("Ix").metodos().stream().map(Metodo::nombre).toList());
        assertEquals("char", metodo("Ix", "m", 1).parametros().getFirst().tipo().nombre());
        assertFalse(metodo("Ix", "n", 0).esEstatico());
    }

    // Documento §6.3.
    @Test
    void sinConstructoresSeAgregaElPorDefecto(@TempDir Path dir) throws Exception {
        parsear(dir, "class Aa { }\n");

        assertEquals(1, clase("Aa").constructores().size());
        assertEquals(0, clase("Aa").constructores().getFirst().aridad());
    }

    @Test
    void conUnConstructorNoSeAgregaElPorDefecto(@TempDir Path dir) throws Exception {
        parsear(dir, "class Aa {\n  Aa(int x) { }\n}\n");

        assertEquals(List.of(1), clase("Aa").constructores().stream().map(c -> c.aridad()).toList());
    }

    @Test
    void losCuerposYLasInicializacionesNoAgreganNadaALaTabla(@TempDir Path dir) throws Exception {
        parsear(dir, "class Aa {\n  int x = y;\n  void m(int a) { int b; var c = new Bb(); }\n}\n");

        assertEquals(1, clase("Aa").atributos().size());
        assertEquals(1, metodo("Aa", "m", 1).aridad());
    }
}
