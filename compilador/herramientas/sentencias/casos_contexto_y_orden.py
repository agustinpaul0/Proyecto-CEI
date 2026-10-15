"""Contexto estático, this, orden de los chequeos y construcciones de logros."""
from catalogo import bien, mal

bien("estaticoUsaMiembrosDeInstanciaConReceptor", "en un estatico se accede a lo de instancia con un receptor explicito (§10.4)", """
class Persona {
    int edad;
    int obtenerEdad() { return edad; }
    static int consultar(Persona persona) {
        var otra = new Persona();
        otra.edad = persona.obtenerEdad();
        return persona.obtenerEdad() + new Persona().edad + crear().edad;
    }
    static Persona crear() { return new Persona(); }
}
""")

bien("thisEnMetodosDeInstanciaYConstructores", "this vale en constructores y metodos de instancia (§11.3)", """
class Aa {
    Aa otro;
    Aa() { otro = this; }
    Aa yo() { return this; }
    void m() {
        var a = this;
        a = (this);
        this.otro = this.yo();
    }
}
""")

bien("metodoHeredadoSeChequeaUnaSolaVez", "el cuerpo heredado se chequea en su clase de origen (§14, Clase 10 slide 51)", """
class Base {
    int x;
    int f() { return x; }
}
class Hija extends Base { }
class Nieta extends Hija {
    int g() { return f(); }
}
""")

bien("cuerpoDeUnaClaseSinErrorEntreClasesCorrectas", "varias clases correctas con cuerpos que se usan entre si", """
class Nodo {
    int valor;
    Nodo siguiente;
    Nodo(int v) { valor = v; }
}
class Lista {
    Nodo primero;
    int largo;
    void agregar(int v) {
        var nuevo = new Nodo(v);
        nuevo.siguiente = primero;
        primero = nuevo;
        largo = largo + 1;
    }
    int suma() {
        var total = 0;
        var actual = primero;
        while (actual != null) {
            total = total + actual.valor;
            actual = actual.siguiente;
        }
        return total;
    }
    static void main() {
        var l = new Lista();
        l.agregar(1);
        l.agregar(2);
        System.printIln(l.suma());
    }
}
""")

mal("thisEnEstatico", "this", "this no vale en un metodo estatico (§10.4)", """
class Aa {
    static void m() {
        var a = this; // <-
    }
}
""")

mal("thisParentizadoEnEstatico", "this", "los parentesis no habilitan this en un estatico (§11.4)", """
class Aa {
    static void m() {
        var a = (this); // <-
    }
}
""")

mal("thisComoArgumentoEnEstatico", "this", "this tampoco como argumento", """
class Aa {
    static void f(Aa a) { }
    static void m() {
        f(this); // <-
    }
}
""")

mal("atributoImplicitoEnEstatico", "x", "en un estatico un atributo no se usa sin receptor (§10.1, §10.4)", """
class Aa {
    int x;
    static void m() {
        x = 1; // <-
    }
}
""")

mal("atributoHeredadoImplicitoEnEstatico", "x", "tampoco uno heredado", """
class Aa { int x; }
class Bb extends Aa {
    static void m() {
        var y = x; // <-
    }
}
""")

mal("atributoImplicitoEnEstaticoParentizado", "x", "ni dentro de parentesis (§10.4)", """
class Aa {
    int x;
    static int m() {
        return (x); // <-
    }
}
""")

mal("llamadaDeInstanciaImplicitaEnEstaticoComoArgumento", "n", "una llamada implicita a un metodo de instancia en un estatico (§10.4)", """
class Aa {
    int n() { return 1; }
    static void m() {
        System.printI(n()); // <-
    }
}
""")

mal("errorEnElPrimerMetodoSeReportaPrimero", "zz", "el primer error corta el analisis: el del primer metodo", """
class Aa {
    void m() {
        zz = 1; // <-
    }
    void n() {
        yy = 2;
    }
}
""")

mal("errorEnLaSegundaClase", "yy", "la primera clase esta bien y el error esta en la segunda", """
class Aa {
    void m() {
        var x = 1;
    }
}
class Bb {
    void n() {
        yy = 2; // <-
    }
}
""")

mal("errorEnElOrdenDelFuenteEntreMetodoYConstructor", "yy", "las unidades se chequean en el orden del fuente, como javac", """
class Aa {
    void m() {
        yy = 1; // <-
    }
    Aa() {
        zz = 2;
    }
}
""")

mal("errorEnElConstructor", "zz", "los constructores tambien se chequean", """
class Aa {
    Aa() {
        zz = 1; // <-
    }
}
""")

mal("errorEnUnMetodoEstatico", "zz", "los metodos estaticos tambien se chequean", """
class Aa {
    static void m() {
        zz = 1; // <-
    }
}
""")

mal("errorEnUnMetodoHeredadoSeReportaEnSuClase", "zz", "un cuerpo heredado se chequea una sola vez, en su clase (§14)", """
class Base {
    void f() {
        zz = 1; // <-
    }
}
class Hija extends Base { }
""")

mal("errorDeDeclaracionAntesQueDeSentencia", "Nada", "las sentencias se chequean solo si las declaraciones estan bien (Pautas slide 22)", """
class Aa {
    void m() {
        zz = 1;
    }
}
class Bb {
    Nada x; // <-
}
""")

mal("errorDeConsolidacionAntesQueDeSentencia", "f", "una redefinicion incorrecta se reporta antes que un cuerpo (Clase 10 slide 50)", """
class Aa {
    int f() { return true; }
}
class Bb extends Aa {
    boolean f() { return true; } // <-
}
""")

mal("errorEnElUltimoMetodoDeLaUltimaClase", "yy", "el error esta al final del archivo", """
class Aa { void m() { } }
class Bb { void m() { } }
class Cc {
    void m() { }
    void n() {
        var x = yy; // <-
    }
}
""")

mal("noControladoFor", "for", "el for es de un logro que no se controla", """
class Aa {
    void m() {
        for (var i = 0; i < 3; i = i + 1) ; // <-
    }
}
""")

mal("noControladoTernario", "?", "el ternario es de un logro que no se controla", """
class Aa {
    void m(boolean b) {
        var x = b ? 1 : 2; // <-
    }
}
""")

mal("noControladoPosfijo", "++", "el ++ posfijo es de un logro que no se controla", """
class Aa {
    int a1;
    int a2;
    void m1() {
        a2 = a1++ + 10; // <-
    }
}
""")

mal("noControladoDeclaracionConTipo", "int", "la declaracion local con tipo es de un logro que no se controla", """
class Aa {
    void m() {
        int x = 1; // <-
    }
}
""")

mal("noControladoInicializadorDeArreglo", "new", "el inicializador de arreglo es de un logro que no se controla", """
class Aa {
    void m() {
        var v = new int[]{1, 2}; // <-
    }
}
""")
