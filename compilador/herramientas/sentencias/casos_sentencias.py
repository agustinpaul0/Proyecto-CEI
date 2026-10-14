"""Sentencias: documento §12."""
from catalogo import bien, mal

bien("cuerposVacios", "metodos y constructores con cuerpo vacio, aunque retornen un valor (§12.5)", """
class Aa {
    Aa() { }
    void v() { }
    int i() { }
    Aa o() { }
    static boolean b() { }
}
""")

bien("sentenciasVaciasYBloques", "; y los bloques vacios son correctos (§12.1, §12.6)", """
class Aa {
    void m() {
        ;
        ;;
        { }
        {{ }}
        { ; { ; } }
    }
}
""")

bien("ifWhileYElse", "if con y sin else y while con condicion boolean (§12.4)", """
class Aa {
    void m(boolean b, int i) {
        if (b) i = 1;
        if (i < 3) i = 2; else i = 3;
        if (b && (i == 3)) { i = 4; } else { i = 5; }
        while (i > 0) i = i - 1;
        while (b) { b = false; }
        if (b) ; else ;
        while (false) ;
    }
}
""")

bien("elseColganteLigaAlIfMasCercano", "el else liga al if mas cercano y las dos ramas se chequean", """
class Aa {
    void m(boolean a, boolean b, int i) {
        if (a) if (b) i = 1; else i = 2;
        if (a) { if (b) i = 3; } else i = 4;
    }
}
""")

bien("condicionesDeCadaForma", "la condicion puede ser una llamada, un atributo o una asignacion boolean (§12.4)", """
class Aa {
    boolean bandera;
    boolean f() { return true; }
    void m(boolean b) {
        if (f()) ;
        if (bandera) ;
        if (this.bandera) ;
        if (b = false) ;
        while (b = f()) ;
        if (!f() || bandera && (b == true)) ;
        if ((b)) ;
    }
}
""")

bien("varDentroDeBloquesDeIfYWhile", "con llaves, el cuerpo de un if o un while puede declarar (§12.3)", """
class Aa {
    void m(boolean c) {
        if (c) { var x = 1; }
        if (c) { var x = 1; } else { var y = 2; }
        while (c) { var x = 1; c = false; }
    }
}
""")

bien("variantesDeReturn", "return sin valor en void y constructores, con valor conformante en los demas (§12.5)", """
class Aa { }
class Bb extends Aa { }
class Uso {
    Uso() { return; }
    void v() { return; }
    int i() { return 1; }
    boolean b() { return 1 < 2; }
    Aa a() { return new Bb(); }
    Aa n() { return null; }
    Object o() { return "s"; }
    int[] arr() { return new int[2]; }
    Object arrComoObject() { return new int[2]; }
    String s() { return "x"; }
    int asignacion(int y) { return y = 4; }
}
""")

bien("sinRetornoEnTodosLosCaminos", "no se exige retorno en todos los caminos ni se rechaza codigo inalcanzable (§12.5)", """
class Aa {
    int sinRetorno() { }
    int parcial(boolean elegir) {
        if (elegir) return 1;
    }
    int conCodigoInalcanzable() {
        return 1;
        var numero = 2;
    }
    void dosReturns() {
        return;
        return;
    }
}
""")

bien("whileConReturnAdentro", "un return dentro de un while", """
class Aa {
    int m(int n) {
        while (n > 0) {
            if (n == 5) return n;
            n = n - 1;
        }
        return 0;
    }
}
""")

bien("anidamientoProfundo", "if y while anidados con bloques y locales a varios niveles", """
class Aa {
    int m(int n) {
        var total = 0;
        while (n > 0) {
            var i = n;
            if (i > 10) {
                while (i > 10) {
                    var j = i;
                    if (j == 15) {
                        {
                            total = total + j;
                        }
                    } else {
                        total = total - 1;
                    }
                    i = i - 1;
                }
            }
            n = n - 1;
        }
        return total;
    }
}
""")

for nombre, condicion, lexema, descripcion in [
    ("condicionIfEntera", "if (x) ;", "x", "la condicion de un if es boolean (§12.4)"),
    ("condicionIfNull", "if (null) ;", "null", "null no es boolean"),
    ("condicionIfString", 'if ("true") ;', '"true"', "String no es boolean"),
    ("condicionIfAsignacionEntera", "if (x = 1) ;", "=", "una asignacion int no es condicion (§12.4)"),
    ("condicionIfSuma", "if (x + 1) ;", "+", "una suma no es condicion"),
    ("condicionIfLlamadaVoid", "if (v()) ;", "(", "una llamada void no es condicion"),
    ("condicionIfLlamadaEntera", "if (n()) ;", "(", "una llamada int no es condicion"),
    ("condicionIfParentizada", "if ((x)) ;", "x", "el parentesis conserva el tipo int; el error va adentro, como en javac"),
    ("condicionWhileEntera", "while (x) ;", "x", "la condicion de un while es boolean (§12.4)"),
    ("condicionWhileReferencia", "while (this) ;", "this", "una referencia no se convierte a boolean (§12.4)"),
    ("condicionWhileChar", "while ('a') ;", "'a'", "char no es boolean"),
    ("condicionDeIfAnidado", "if (true) if (x) ;", "x", "la condicion del if de adentro"),
    ("condicionDeWhileDentroDeIf", "if (true) { while (x) ; }", "x", "la condicion del while de adentro"),
    ("condicionIfLlamadaEstatica", "if (Aa.e()) ;", "(", "una llamada estatica int no es condicion; el error va en el (, como en javac"),
    ("condicionIfCreacion", "if (new Aa()) ;", "new", "una referencia no es condicion; el error va en el new, como en javac"),
]:
    mal(nombre, lexema, descripcion, f"""
class Aa {{
    void v() {{ }}
    int n() {{ return 1; }}
    static int e() {{ return 1; }}
    void m(int x) {{
        {condicion} // <-
    }}
}}
""")

mal("errorEnLaRamaThen", "+", "la rama then se chequea", """
class Aa {
    void m(boolean b) {
        if (b) b = b + 1; // <-
    }
}
""")

mal("errorEnLaRamaElse", "zz", "la rama else se chequea aunque la condicion sea true (§12.4)", """
class Aa {
    void m() {
        if (true) ;
        else zz = 1; // <-
    }
}
""")

mal("errorEnElCuerpoDeUnWhileFalso", "zz", "el cuerpo se chequea aunque no se ejecute (§12.4)", """
class Aa {
    void m() {
        while (false) {
            zz = 1; // <-
        }
    }
}
""")

mal("varComoCuerpoDeIf", "x", "una declaracion no es el cuerpo directo de un if; va en el nombre, como en javac (§12.3)", """
class Aa {
    void m(boolean c) {
        if (c) var x = 1; // <-
    }
}
""")

mal("varComoCuerpoDeElse", "y", "tampoco del else (§12.3)", """
class Aa {
    void m(boolean c) {
        if (c) ;
        else var y = 1; // <-
    }
}
""")

mal("varComoCuerpoDeWhile", "x", "tampoco de un while (§12.3)", """
class Aa {
    void m(boolean c) {
        while (c) var x = 1; // <-
    }
}
""")

mal("varComoCuerpoDeIfAnidado", "x", "el if de adentro tambien exige bloque", """
class Aa {
    void m(boolean c) {
        if (c) {
            if (c) var x = 1; // <-
        }
    }
}
""")

mal("varCuerpoDeIfAntesQueSuInicializador", "x", "la forma se controla antes que el inicializador", """
class Aa {
    void m(boolean c) {
        if (c) var x = zz; // <-
    }
}
""")

mal("returnSinValorEnMetodoConRetorno", "return", "un metodo int exige return e (§12.5)", """
class Aa {
    int m() {
        return; // <-
    }
}
""")

mal("returnSinValorEnMetodoDeReferencia", "return", "un metodo que retorna una clase exige return e (§12.5)", """
class Aa {
    Aa m() {
        return; // <-
    }
}
""")

mal("returnConValorEnVoid", "1", "un metodo void solo admite return; el error va en el valor (§12.5)", """
class Aa {
    void m() {
        return 1; // <-
    }
}
""")

mal("returnConValorEnConstructor", "1", "un constructor no retorna un valor (§12.5)", """
class Aa {
    Aa() {
        return 1; // <-
    }
}
""")

mal("returnDeThisEnConstructor", "this", "tampoco this: new produce el objeto, no el constructor (§12.5)", """
class Aa {
    Aa() {
        return this; // <-
    }
}
""")

mal("returnDeLlamadaVoidEnVoid", "(", "un retorno con resultado void es incorrecto, incluso en un metodo void (§12.5)", """
class Aa {
    void v() { }
    void m() {
        return v(); // <-
    }
}
""")

mal("returnDeLlamadaVoidEnInt", "(", "void no conforma con int", """
class Aa {
    void v() { }
    int m() {
        return v(); // <-
    }
}
""")

mal("returnIncompatible", "true", "el valor no conforma con el retorno declarado (§12.5)", """
class Aa {
    int m() {
        return true; // <-
    }
}
""")

mal("returnNullEnInt", "null", "null no conforma con int", """
class Aa {
    int m() {
        return null; // <-
    }
}
""")

mal("returnSuperclaseEnSubclase", "a", "Aa no conforma con Bb", """
class Aa { }
class Bb extends Aa {
    Bb m(Aa a) {
        return a; // <-
    }
}
""")

mal("returnDeBinariaIncompatible", "+", "el valor de una binaria va en su operador", """
class Aa {
    boolean m(int x) {
        return x + 1; // <-
    }
}
""")

mal("returnIncompatibleEnCodigoInalcanzable", "'c'", "el codigo inalcanzable se chequea igual (§12.5)", """
class Aa {
    int m() {
        return 1;
        return 'c'; // <-
    }
}
""")

mal("errorDespuesDeUnReturn", "zz", "las sentencias despues de un return se chequean (§12.6)", """
class Aa {
    void m() {
        return;
        zz = 1; // <-
    }
}
""")

for nombre, sentencia, lexema in [
    ("sentenciaBinaria", "x + 1;", "+"),
    ("sentenciaComparacion", "x == 1;", "=="),
    ("sentenciaUnaria", "-x;", "-"),
    ("sentenciaNegacion", "!b;", "!"),
    ("sentenciaVariable", "x;", "x"),
    ("sentenciaLiteralEntero", "1;", "1"),
    ("sentenciaLiteralString", '"s";', '"s"'),
    ("sentenciaNull", "null;", "null"),
    ("sentenciaThis", "this;", "this"),
    ("sentenciaAccesoAAtributo", "this.x;", "."),
    ("sentenciaAccesoEncadenadoAAtributo", "yo().x;", "."),
    ("sentenciaIndexacion", "v[0];", "["),
    ("sentenciaLength", "v.length;", "."),
    ("sentenciaCreacionDeArreglo", "new int[3];", "new"),
    ("sentenciaAsignacionParentizada", "(x = 3);", "("),
    ("sentenciaLlamadaParentizada", "(yo());", "("),
    ("sentenciaLlamadaSobreParentizada", "(this).yo();", "("),
    ("sentenciaLlamadaSobreParentizadaEncadenada", "(yo()).yo().yo();", "("),
]:
    mal(nombre, lexema, "como sentencia solo vale una asignacion o una llamada (§12.2, §11.4)", f"""
class Aa {{
    int x;
    int[] v;
    Aa yo() {{ return this; }}
    void m(boolean b) {{
        {sentencia} // <-
    }}
}}
""")

mal("sentenciaChequeaSusTiposAntesQueSuForma", "zz", "la expresion se chequea antes que su forma (§11, 1.er parrafo)", """
class Aa {
    void m() {
        true + zz; // <-
    }
}
""")
