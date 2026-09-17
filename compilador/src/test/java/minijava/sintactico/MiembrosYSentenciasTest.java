package minijava.sintactico;

import minijava.sintactico.errores.ExcepcionSintactica;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("sintactico")
class MiembrosYSentenciasTest extends BaseDeAnalisis {

    // ------------------------------------------------------------- miembros ---

    @ParameterizedTest
    @ValueSource(strings = {
            "int x;",
            "boolean b;",
            "char c;",
            "boolean[] v;",
            "char m1(boolean a, char b){}",
            "int[] a;",
            "int[][] m;",
            "Persona p;",
            "T t;",
            "Lista<Persona> l;",
            "void m1(){}",
            "int m1(){}",
            "static int m1(){}",
            "static void m1(){}",
            "Persona m1(){}",
            "int m1(int a){}",
            "int m1(int a, char b, Persona c){}",
            "int m1(int[] a){}",
            "public C1(){}",
            "public C1(int a){}",
    })
    void seAceptaCadaFormaDeMiembro(String miembro, @TempDir Path dir) {
        aceptar(dir, "class C1 { " + miembro + " }");
    }

    @Test
    void seAceptanMuchosMiembrosSeguidos(@TempDir Path dir) {
        aceptar(dir, "class C1 { int x; void m1(){} char c; static int m2(){} Persona p; }");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "int m1();",
            "void m1();",
            "Persona m1(int a);",
            "T m1();",
    })
    void seAceptaCadaFormaDeMetodoDeInterfaz(String metodo, @TempDir Path dir) {
        aceptar(dir, "interface I1 { " + metodo + " }");
    }

    // Un método de interfaz es siempre abstracto: nunca lleva cuerpo.
    @Test
    void unMetodoDeInterfazConCuerpoSeRechaza(@TempDir Path dir) {
        assertEquals("{", rechazar(dir, "interface I1 { void m1(){} }").getLexemaOfensivo());
    }

    // ---------------------------------------------------------- visibilidad ---

    @ParameterizedTest
    @ValueSource(strings = {
            "private int x;", "public int x;", "int x;",
            "private void m1(){}", "public void m1(){}",
            "private C1(){}", "public C1(){}", "C1(){}",
    })
    void conVisibilidadMejoradaTodoMiembroPuedeLlevarlaOOmitirla(String miembro, @TempDir Path dir) {
        aceptar(dir, "class C1 { " + miembro + " }");
    }

    @Test
    void unMetodoDeInterfazAdmitePublicPeroNuncaPrivate(@TempDir Path dir) {
        aceptar(dir, "interface I1 { public void m1(); }");
        assertEquals("private", rechazar(dir, "interface I1 { private void m1(); }")
                .getLexemaOfensivo());
    }

    // Los dos órdenes valen, como en Java.
    @Test
    void laVisibilidadPuedeIrAntesODespuesDelStatic(@TempDir Path dir) {
        aceptar(dir, "class C1 { public static int m1(){} }");
        aceptar(dir, "class C1 { static public int m1(){} }");
        aceptar(dir, "class C1 { private static int m1(){} }");
        aceptar(dir, "class C1 { static private int m1(){} }");
    }

    // Que la visibilidad aparezca a UN SOLO lado del `static` es lo que impide
    // repetir el modificador, sin ningún chequeo fuera de la gramática.
    @Test
    void unModificadorNoSePuedeRepetir(@TempDir Path dir) {
        assertEquals("private", rechazar(dir, "class C1 { public private int m1(){} }")
                .getLexemaOfensivo());
        assertEquals("static", rechazar(dir, "class C1 { static static int m1(){} }")
                .getLexemaOfensivo());
        assertEquals("private", rechazar(dir, "class C1 { public static private int m1(){} }")
                .getLexemaOfensivo());
    }

    // ------------------------------------------------ atributos inicializados ---

    @Test
    void conAtributosInicializadosUnAtributoPuedeTraerValor(@TempDir Path dir) {
        aceptar(dir, "class C1 { int x = 5; }");
        aceptar(dir, "class C1 { Persona p = new Persona(); }");
    }

    // `a = b = c` no es válido: el lado derecho es una ExpresionCondicional y no
    // una Expresion, igual que en la gramática original.
    @Test
    void laInicializacionNoEncadenaAsignaciones(@TempDir Path dir) {
        assertEquals("=", rechazar(dir, "class C1 { int x = y = 5; }").getLexemaOfensivo());
    }

    // ------------------------------------------------------------ sentencias ---

    @ParameterizedTest
    @ValueSource(strings = {
            ";", "{}", "{;}", "{{{;}}}",
            "return;", "return 1;", "return a + b;",
            "var x = 1;", "var x = new Persona();",
            "x = 1;", "x.y = 1;", "a[0] = 1;",
            "m1();", "this.m1();", "this.x = 1;", "Clase.m1();", "a.b.c();", "a.b().c();",
            "if (a) ;", "if (a) ; else ;", "if (a) { } else { }",
            "while (a) ;", "while (a) { m1(); }",
    })
    void seAceptaCadaFormaDeSentencia(String sentencia, @TempDir Path dir) {
        aceptar(dir, envolverEnMetodo(sentencia));
    }

    // Dangling else: el `else` liga al `if` más cercano, que es la regla de Java.
    @Test
    void unElseColganteSeAceptaYLigaAlIfMasCercano(@TempDir Path dir) {
        aceptar(dir, envolverEnMetodo("if (a) if (b) m1(); else m2();"));
        aceptar(dir, envolverEnMetodo("if (a) { if (b) m1(); } else m2();"));
    }

    @Test
    void unElseSinIfSeRechaza(@TempDir Path dir) {
        assertEquals("else", rechazar(dir, envolverEnMetodo("else ;")).getLexemaOfensivo());
    }

    @Test
    void seAceptaUnAnidamientoProfundoDeBloques(@TempDir Path dir) {
        aceptar(dir, envolverEnMetodo("{".repeat(100) + ";" + "}".repeat(100)));
    }

    // ------------------------------------------ variables locales clásicas ---

    @ParameterizedTest
    @ValueSource(strings = {
            "int x;", "int x = 1;", "int x, y;", "int x, y, z = 10;",
            "int[] a;", "int[][] m;", "Persona p;", "Persona p = new Persona();",
            "Lista<Persona> l;", "T t;",
    })
    void conVariablesClasicasSeDeclaraSinVar(String sentencia, @TempDir Path dir) {
        aceptar(dir, envolverEnMetodo(sentencia));
    }

    // `C x;` y `C.m();` compiten por el idClase, y el desempate es el token
    // siguiente: `.` es expresión, cualquier otro es declaración.
    @Test
    void unIdClaseSeDesempataConElTokenSiguiente(@TempDir Path dir) {
        aceptar(dir, envolverEnMetodo("Persona p;"));
        aceptar(dir, envolverEnMetodo("Persona.crear();"));
    }

    // `var` siempre exige inicialización; la forma clásica no.
    @Test
    void varSiempreExigeInicializacion(@TempDir Path dir) {
        assertEquals(";", rechazar(dir, envolverEnMetodo("var x;")).getLexemaOfensivo());
    }

    // ------------------------------------------------------------------ for ---

    // Sólo las formas que el logro Fors! trae por sí solo.
    @ParameterizedTest
    @ValueSource(strings = {
            "for(;;) ;",
            "for(; i < 10; ) ;",
            "for(var i = 0; ; ) ;",
            "for(var i = 0; i < 10; i) ;",
            "for(Clase.crear(); ; ) ;",
            "for(Clase.crear().campo; ; ) ;",
            "for(var e : lista) ;",
            "for(Persona p : lista) ;",
            "for(int x : arreglo) ;",
            "for(T t : lista) ;",
            "for(i = 0; i < 10; ) ;",
            "for(;;) { m1(); }",
            "for(;;) for(;;) ;",
            "for(;;) if (a) ; else ;",
    })
    void conForsSeAceptaCadaFormaDelFor(String sentencia, @TempDir Path dir) {
        aceptar(dir, envolverEnMetodo(sentencia));
    }

    // El declarador tipado del encabezado es del logro de FORS, no del de variables
    // clásicas: el logro dice «for similar a como es en java», y en Java el for
    // clásico declara su propia variable. Por eso el encabezado tiene su propio
    // <InicializacionDeDeclaradorFor> en vez de reusar el de las variables locales.
    @ParameterizedTest
    @ValueSource(strings = {
            "for(int i = 0; i < 10; ) ;",
            "for(int i = 0; ; ) ;",
            "for(int[] v = null; ; ) ;",
            "for(Persona p = null; ; ) ;",
            "for(T t = null; ; ) ;",
            "for(Persona p : personas) ;",
    })
    void elDeclaradorTipadoDelForNoNecesitaLasVariablesClasicas(String sentencia,
                                                                @TempDir Path dir) {
        aceptar(dir, envolverEnMetodo(sentencia));
    }

    // El declarador del for es un tipo de MiniJava: las dimensiones van pegadas al
    // tipo (`int[] v`) y no al nombre, que es la forma que la gramática admite.
    @Test
    void elForNoAdmiteLasDimensionesAlEstiloC(@TempDir Path dir) {
        assertEquals("[", rechazar(dir, envolverEnMetodo("for(int v[] = null; ; ) ;"))
                .getLexemaOfensivo());
    }

    // Y el `i++` del incremento es del logro de posfijos.
    @Test
    void elForConIncrementoPosfijoNecesitaTambienLosPosfijos(@TempDir Path dir) {
        aceptar(dir, envolverEnMetodo("for(; ; i++) ;"));
    }

    // El logro restringe a una sola sentencia o expresión por separador.
    @Test
    void elForNoAdmiteVariasExpresionesPorSeparador(@TempDir Path dir) {
        assertEquals(",", rechazar(dir, envolverEnMetodo("for(int i=0, j=0; ; ) ;"))
                .getLexemaOfensivo());
    }

    @Test
    void elForDeIteradoresNoLlevaClausulasFinales(@TempDir Path dir) {
        assertEquals(";", rechazar(dir, envolverEnMetodo("for(var e : lista; ; ) ;"))
                .getLexemaOfensivo());
    }

    // --------------------------------------------------- errores por token ---

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "class C1 { int }              | }",
            "class C1 { int x }            | }",
            "class C1 { void m1() }        | }",
            "class C1 { void m1({) }       | {",
            "class C1 { void m1(int){} }   | )",
            "class C1 { static }           | }",
            "interface I1 { void m1() }    | }",
            "interface I1 { int x; }       | ;",
    })
    void unMiembroInvalidoFallaEnElTokenExacto(String fuente, String lexema, @TempDir Path dir) {
        assertEquals(lexema, rechazar(dir, fuente).getLexemaOfensivo());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "if (a) ; else            | }",
            "if a) ;                  | a",
            "while ( ) ;              | )",
            "return                   | }",
            "{ ;                      | $",
            "m1(                      | }",
            "m1(a)                   | }",
            "a[] = 1;                 | ]",
    })
    // El lexema esperado es el del token donde el análisis se detiene: como la
    // sentencia va envuelta en un método, muchas veces es la llave de cierre.
    void unaSentenciaInvalidaFallaEnElTokenExacto(String sentencia, String lexema,
                                                  @TempDir Path dir) {
        ExcepcionSintactica excepcion = rechazar(dir, envolverEnMetodo(sentencia));

        assertEquals(lexema, excepcion.getLexemaOfensivo());
    }
}
