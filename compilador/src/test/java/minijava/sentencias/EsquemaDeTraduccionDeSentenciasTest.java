package minijava.sentencias;

import minijava.semantico.entidades.Clase;
import minijava.semantico.tabla.TablaDeSimbolos;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

// La EDT que arma los AST (Clase 09, slides 40-41): se parsea un cuerpo y se
// compara su árbol, impreso como expresión S, con el que se espera. Hay al menos
// un caso por cada producción de sentencias y expresiones.
@Tag("sentencias")
class EsquemaDeTraduccionDeSentenciasTest extends BaseDeSentencias {

    private String arbol(String cuerpo) throws Exception {
        parsear("class Aa { void m() { " + cuerpo + " } }");
        return metodo("Aa", "m", 0).cuerpo().toString();
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiterString = " => ", quoteCharacter = '#', value = {
            "; => {;}",
            "{ } => {{}}",
            "{ ; { } } => {{; {}}}",
            "var x = 1; => {(var x 1)}",
            "return; => {(return)}",
            "return 1 + 2; => {(return (+ 1 2))}",
            "if (a) x = 1; => {(if a (sentencia (= x 1)))}",
            "if (a) ; else { } => {(if a ; {})}",
            "if (a) if (b) ; else x = 2; => {(if a (if b ; (sentencia (= x 2))))}",
            "while (a) { x = 1; } => {(while a {(sentencia (= x 1))})}",
            "x = y; => {(sentencia (= x y))}",
            "x = (y = 3); => {(sentencia (= x (paren (= y 3))))}",
            "x = a - b - c; => {(sentencia (= x (- (- a b) c)))}",
            "x = a + b * c; => {(sentencia (= x (* (+ a b) c)))}",
            "x = a * (b + c); => {(sentencia (= x (* a (paren (+ b c)))))}",
            "x = a < b == c && d || e; => {(sentencia (= x (|| (&& (== (< a b) c) d) e)))}",
            "x = -a + !b; => {(sentencia (= x (+ (- a) (! b))))}",
            "x = 'c'; => {(sentencia (= x 'c'))}",
            "x = \"s\"; => {(sentencia (= x \"s\"))}",
            "x = null; => {(sentencia (= x null))}",
            "x = true; => {(sentencia (= x true))}",
            "x = 1.5; => {(sentencia (= x 1.5))}",
            "x = this; => {(sentencia (= x this))}",
            "this.x = 1; => {(sentencia (= (atributo this x) 1))}",
            "m(1, b); => {(sentencia (llamar m 1 b))}",
            "m(); => {(sentencia (llamar m))}",
            "a.b.c(1).d = 2; => {(sentencia (= (atributo (llamar (atributo a b).c 1) d) 2))}",
            "Aa.m(1); => {(sentencia (llamar Aa.m 1))}",
            "Aa.m().x = 1; => {(sentencia (= (atributo (llamar Aa.m) x) 1))}",
            "x = Aa.m().n(); => {(sentencia (= x (llamar (llamar Aa.m).n)))}",
            "x = v[1][2]; => {(sentencia (= x ([] ([] v 1) 2)))}",
            "v[0].x[1] = 3; => {(sentencia (= ([] (atributo ([] v 0) x) 1) 3))}",
            "new Aa(1); => {(sentencia (new Aa 1))}",
            "x = new Caja<String>(); => {(sentencia (= x (new Caja<String>)))}",
            "x = new int[3][n]; => {(sentencia (= x (new int[][] 3 n)))}",
            "x = new Aa[2]; => {(sentencia (= x (new Aa[] 2)))}",
            "x = new Caja<String>[2]; => {(sentencia (= x (new Caja<String>[] 2)))}",
            "x = new Aa().m(); => {(sentencia (= x (llamar (new Aa).m)))}",
            "(x = 3); => {(sentencia (paren (= x 3)))}",
            "(this).m(); => {(sentencia (llamar (paren this).m))}",
            "-x; => {(sentencia (- x))}",
            "1 + 2; => {(sentencia (+ 1 2))}",
            "\"s\".toString(); => {(sentencia (llamar \"s\".toString))}",
            "for (var i = 0; i < 3; i = 1) ; => {(no-controlada for)}",
            "int x = 1; => {(no-controlada int)}",
            "Aa x; => {(no-controlada Aa)}",
            "x = a ? b : c; => {(sentencia (= x (no-controlada ?)))}",
            "x = a++; => {(sentencia (= x (no-controlada ++)))}",
            "x = a--; => {(sentencia (= x (no-controlada --)))}",
            "x = new int[]{1, 2}; => {(sentencia (= x (no-controlada new)))}",
            "x = new Caja<>(); => {(sentencia (= x (no-controlada new)))}",
    })
    void cadaCuerpoArmaSuArbol(String cuerpo, String esperado) throws Exception {
        assertEquals(esperado, arbol(cuerpo));
    }

    @Test
    void unCuerpoVacioEsUnBloqueSinSentencias() throws Exception {
        assertEquals("{}", arbol(""));
    }

    @Test
    void elCuerpoDeUnConstructorTambienSeCuelga() throws Exception {
        parsear("class Aa { Aa() { return; } }");
        var clase = (Clase) TablaDeSimbolos.tipo("Aa").orElseThrow();
        assertEquals("{(return)}", clase.constructor(0).orElseThrow().cuerpo().toString());
        assertSame(clase, clase.constructor(0).orElseThrow().declarante());
    }

    @Test
    void elConstructorPorDefectoYLosMetodosDeInterfazNoTienenCuerpo() throws Exception {
        parsear("class Aa { } interface Ix { void f(); }");
        var clase = (Clase) TablaDeSimbolos.tipo("Aa").orElseThrow();
        assertNull(clase.constructor(0).orElseThrow().cuerpo());
        assertNull(metodo("Ix", "f", 0).cuerpo());
    }

    // Documento §14: la copia heredada comparte el cuerpo y recuerda su declarante.
    @Test
    void laCopiaHeredadaConservaElCuerpoYElDeclarante() throws Exception {
        parsear("class Base<T> { T f(T x) { return x; } } class Hija extends Base<String> { }");
        TablaDeSimbolos.chequearDeclaraciones();
        TablaDeSimbolos.consolidar();
        var original = metodo("Base", "f", 1);
        var heredado = metodo("Hija", "f", 1);

        assertSame(original.cuerpo(), heredado.cuerpo());
        assertSame(original.declarante(), heredado.declarante());
        assertEquals("String", heredado.retorno().nombre());
    }
}
