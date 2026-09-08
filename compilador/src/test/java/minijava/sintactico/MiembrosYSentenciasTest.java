package minijava.sintactico;

import minijava.comun.config.Logro;
import minijava.logros.RequiereLogro;
import minijava.logros.RequiereLogroApagado;
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
    @RequiereLogro(Logro.VISIBILIDAD)
    @ValueSource(strings = {
            "private int x;", "public int x;", "int x;",
            "private void m1(){}", "public void m1(){}",
            "private C1(){}", "public C1(){}", "C1(){}",
    })
    void conVisibilidadMejoradaTodoMiembroPuedeLlevarlaOOmitirla(String miembro, @TempDir Path dir) {
        aceptar(dir, "class C1 { " + miembro + " }");
    }

    @Test
    @RequiereLogroApagado(Logro.VISIBILIDAD)
    void sinVisibilidadMejoradaPrivateNoEsUnModificador(@TempDir Path dir) {
        assertEquals("private", rechazar(dir, "class C1 { private int x; }").getLexemaOfensivo());
    }

    // Sin el logro, `public` sólo aparece en el constructor: es lo único que lo
    // distingue del resto de los miembros en la gramática original.
    @Test
    @RequiereLogroApagado(Logro.VISIBILIDAD)
    void sinVisibilidadMejoradaElPublicSoloValeEnElConstructor(@TempDir Path dir) {
        aceptar(dir, "class C1 { public C1(){} }");
        assertEquals("int", rechazar(dir, "class C1 { public int x; }").getLexemaOfensivo());
    }

    @Test
    @RequiereLogro(Logro.VISIBILIDAD)
    void unMetodoDeInterfazAdmitePublicPeroNuncaPrivate(@TempDir Path dir) {
        aceptar(dir, "interface I1 { public void m1(); }");
        assertEquals("private", rechazar(dir, "interface I1 { private void m1(); }")
                .getLexemaOfensivo());
    }

    // El orden es fiel a la gramática original: `static public` se rechaza.
    @Test
    @RequiereLogro(Logro.VISIBILIDAD)
    void laVisibilidadVaAntesDelStaticYNoAlReves(@TempDir Path dir) {
        aceptar(dir, "class C1 { public static int m1(){} }");
        assertEquals("public", rechazar(dir, "class C1 { static public int m1(){} }")
                .getLexemaOfensivo());
    }

    // ------------------------------------------------ atributos inicializados ---

    @Test
    @RequiereLogro(Logro.ATRINIT)
    void conAtributosInicializadosUnAtributoPuedeTraerValor(@TempDir Path dir) {
        aceptar(dir, "class C1 { int x = 5; }");
        aceptar(dir, "class C1 { Persona p = new Persona(); }");
    }

    @Test
    @RequiereLogroApagado(Logro.ATRINIT)
    void sinAtributosInicializadosElIgualSeRechaza(@TempDir Path dir) {
        assertEquals("=", rechazar(dir, "class C1 { int x = 5; }").getLexemaOfensivo());
    }

    // `a = b = c` no es válido: el lado derecho es una ExpresionCondicional y no
    // una Expresion, igual que en la gramática original.
    @Test
    @RequiereLogro(Logro.ATRINIT)
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
    @RequiereLogro(Logro.VARCLASICA)
    @ValueSource(strings = {
            "int x;", "int x = 1;", "int x, y;", "int x, y, z = 10;",
            "int[] a;", "int[][] m;", "Persona p;", "Persona p = new Persona();",
            "Lista<Persona> l;", "T t;",
    })
    void conVariablesClasicasSeDeclaraSinVar(String sentencia, @TempDir Path dir) {
        aceptar(dir, envolverEnMetodo(sentencia));
    }

    @Test
    @RequiereLogroApagado(Logro.VARCLASICA)
    void sinVariablesClasicasSoloSeDeclaraConVar(@TempDir Path dir) {
        aceptar(dir, envolverEnMetodo("var x = 1;"));
        assertEquals("int", rechazar(dir, envolverEnMetodo("int x;")).getLexemaOfensivo());
    }

    // `C x;` y `C.m();` compiten por el idClase, y el desempate es el token
    // siguiente: `.` es expresión, cualquier otro es declaración.
    @Test
    @RequiereLogro(Logro.VARCLASICA)
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
    @RequiereLogro(Logro.FOR)
    @ValueSource(strings = {
            "for(;;) ;",
            "for(; i < 10; ) ;",
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

    // El declarador tipado del encabezado es del logro de variables clásicas.
    @ParameterizedTest
    @RequiereLogro({Logro.FOR, Logro.VARCLASICA})
    @ValueSource(strings = {
            "for(int i = 0; i < 10; ) ;",
            "for(int i = 0; ; ) ;",
            "for(Persona p = null; ; ) ;",
    })
    void elForConDeclaradorTipadoNecesitaTambienLasVariablesClasicas(String sentencia,
                                                                    @TempDir Path dir) {
        aceptar(dir, envolverEnMetodo(sentencia));
    }

    // Y el `i++` del incremento es del logro de posfijos.
    @Test
    @RequiereLogro({Logro.FOR, Logro.POSFIJO})
    void elForConIncrementoPosfijoNecesitaTambienLosPosfijos(@TempDir Path dir) {
        aceptar(dir, envolverEnMetodo("for(; ; i++) ;"));
    }

    @Test
    @RequiereLogroApagado(Logro.FOR)
    void sinForsLaPalabraForNoEsUnaSentencia(@TempDir Path dir) {
        assertEquals(";", rechazar(dir, envolverEnMetodo("for(;;) ;")).getLexemaOfensivo());
    }

    // El logro restringe a una sola sentencia o expresión por separador.
    @Test
    @RequiereLogro(Logro.FOR)
    void elForNoAdmiteVariasExpresionesPorSeparador(@TempDir Path dir) {
        assertEquals(",", rechazar(dir, envolverEnMetodo("for(int i=0, j=0; ; ) ;"))
                .getLexemaOfensivo());
    }

    @Test
    @RequiereLogro(Logro.FOR)
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
            "m1(a,)                   | )",
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
