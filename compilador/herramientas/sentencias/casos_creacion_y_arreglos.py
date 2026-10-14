"""Creación de objetos y arreglos: documento §10.3, §11.7 y §11.8."""
from catalogo import bien, mal

bien("creacionConConstructorPorDefecto", "sin constructores declarados hay uno sin parametros (§6.3)", """
class Aa { }
class Uso {
    void m() {
        var a = new Aa();
        var o = new Object();
        var s = new String();
        a = new Aa();
    }
}
""")

bien("creacionConSobrecargaDeConstructores", "el constructor se elige por aridad (§10.3)", """
class Aa {
    Aa() { }
    Aa(int x) { }
    Aa(int x, String s) { }
}
class Uso {
    void m() {
        var a = new Aa();
        a = new Aa(1);
        a = new Aa(1, "s");
    }
}
""")

bien("creacionComoSentenciaYArgumento", "una creacion vale como sentencia y como argumento (§12.2)", """
class Aa {
    Aa(Aa otro) { }
    Aa() { }
    void m() {
        new Aa();
        new Aa(new Aa());
        new Aa(null);
    }
}
""")

bien("creacionGenericaTieneElTipoCompleto", "new Caja<String>() tiene tipo Caja<String> (§11.7)", """
class Caja<T> {
    T dato;
    Caja(T inicial) { dato = inicial; }
}
class Uso {
    void m(Caja<String> c) {
        c = new Caja<String>("s");
        var d = new Caja<Uso>(this);
        d = new Caja<Uso>(null);
    }
}
""")

bien("creacionConParametroDeTipoComoArgumento", "dentro de Caja<T>, new Caja<T>() es valido", """
class Caja<T> {
    Caja<T> copia() { return new Caja<T>(); }
}
""")

bien("constructorNoInvocaAlDeLaSuperclase", "no hay llamada implicita al constructor de la superclase (§6.3)", """
class Base {
    Base(int valor) { }
}
class Derivada extends Base {
    Derivada() { }
}
class Uso {
    void m() {
        var d = new Derivada();
    }
}
""")

bien("constructorConCuerpo", "el cuerpo de un constructor se chequea como el de un metodo de instancia", """
class Aa {
    int x;
    Aa(int inicial) {
        x = inicial;
        this.x = x + 1;
        var y = this;
        if (x > 0) return;
        m();
    }
    void m() { }
}
""")

bien("creacionDeArreglos", "new con tamanios int y n dimensiones (§11.8)", """
interface Ix { }
class Caja<T> { }
class Aa {
    void m(int n) {
        var a = new int[3];
        var b = new boolean[n][n + 1];
        var c = new char[1][2][3];
        var d = new Aa[n];
        var e = new String[2];
        var f = new Ix[5];
        var g = new Caja<String>[10];
        var h = new Object[1];
        a = new int[a.length];
        c = new char[b.length][a[0]][n];
    }
}
""")

bien("creacionDeArreglosDeParametroDeTipo", "en una unidad de instancia de Caja<T> se crean T[] y Caja<T>[] (§11.8)", """
class Caja<T> {
    void m() {
        var e = new T[10];
        var c = new Caja<T>[3][4];
        e[0] = null;
    }
}
""")

bien("indexacionYLength", "cada indexacion quita una dimension; length es int (§11.8)", """
class Aa {
    void m(int[][] mat, String[] s, int i) {
        i = mat[0][1];
        i = mat.length;
        i = mat[0].length;
        var fila = mat[i];
        fila[0] = 1;
        mat[0] = fila;
        s[i] = "x";
        var t = s[mat[0][0]];
        t = "y";
    }
}
""")

bien("componentesEncadenados", "personas[0].edad usa Persona como receptor (§11.8)", """
class Persona {
    int edad;
    Persona[] hijos;
    int obtenerEdad() { return edad; }
}
class Uso {
    void m(Persona[] personas) {
        personas[0].edad = 3;
        var e = personas[1].hijos[2].obtenerEdad();
        e = personas[0].hijos.length;
        personas[0].hijos[0] = personas[1];
    }
}
""")

bien("arreglosConformanConObject", "un arreglo se pasa donde se espera Object (§9.4)", """
class Aa {
    void recibir(Object valor) { }
    void m() {
        recibir(new int[10]);
        recibir(new String[1][1]);
    }
}
""")

bien("arregloDeInterfazConElementosQueLaImplementan", "un componente de Figura[] recibe una clase que implementa Figura", """
interface Figura { }
class Cuadrado implements Figura { }
class Uso {
    void m() {
        var figuras = new Figura[2];
        figuras[0] = new Cuadrado();
        figuras[1] = null;
    }
}
""")

mal("creacionDeInterfaz", "new", "no se instancian interfaces (§11.7)", """
interface Figura { }
class Uso {
    void m() {
        var f = new Figura(); // <-
    }
}
""")

mal("creacionDeClaseNoDeclarada", "Nada", "el tipo de un new tiene que existir (§10.3)", """
class Aa {
    void m() {
        var x = new Nada(); // <-
    }
}
""")

mal("creacionRawDeGenerica", "Caja", "una clase generica no se instancia sin argumento (§5.2)", """
class Caja<T> { }
class Uso {
    void m() {
        var caja = new Caja(); // <-
    }
}
""")

mal("creacionConArgumentoSobreNoGenerica", "Aa", "una clase no generica no admite argumento (§3)", """
class Aa { }
class Uso {
    void m() {
        var a = new Aa<String>(); // <-
    }
}
""")

mal("creacionConArgumentoNoDeclarado", "Nada", "el argumento de tipo tiene que existir (§3)", """
class Caja<T> { }
class Uso {
    void m() {
        var c = new Caja<Nada>(); // <-
    }
}
""")

mal("creacionConArgumentoRaw", "Caja", "el argumento no puede ser una generica sin su argumento (§3)", """
class Caja<T> { }
class Uso {
    void m() {
        var c = new Caja<Caja>(); // <-
    }
}
""")

mal("creacionConArgumentoTFueraDeSuClase", "T", "una T vale solo en la clase que la declara (§3)", """
class Caja<T> { }
class Uso {
    void m() {
        var c = new Caja<T>(); // <-
    }
}
""")

mal("creacionConAridadSinConstructor", "new", "no hay constructor con esa aridad (§10.3)", """
class Aa { }
class Uso {
    void m() {
        var a = new Aa(1); // <-
    }
}
""")

mal("creacionConAridadDeOtroConstructor", "new", "con un constructor declarado no existe el de por defecto (§6.3)", """
class Aa { Aa(int x) { } }
class Uso {
    void m() {
        var a = new Aa(); // <-
    }
}
""")

mal("constructorNoSeHereda", "new", "los constructores no se heredan (§6.3)", """
class Aa { Aa(int x) { } }
class Bb extends Aa { }
class Uso {
    void m() {
        var b = new Bb(1); // <-
    }
}
""")

mal("argumentoIncompatibleConConstructor", "true", "en un constructor el error va en el argumento, como en javac", """
class Aa { Aa(int x) { } }
class Uso {
    void m() {
        var a = new Aa(true); // <-
    }
}
""")

mal("argumentoGenericoIncompatibleConConstructor", "3", "el parametro T se ve como String desde Caja<String> (§11.7)", """
class Caja<T> { Caja(T inicial) { } }
class Uso {
    void m() {
        var c = new Caja<String>(3); // <-
    }
}
""")

mal("creacionAsignadaAOtroTipo", "=", "el tipo completo de la creacion no conforma con el destino", """
class Caja<T> { }
class Uso {
    void m(Caja<Object> c) {
        c = new Caja<String>(); // <-
    }
}
""")

for nombre, tamanio, lexema in [("tamanioBoolean", "true", "true"), ("tamanioChar", "'c'", "'c'"),
                                ("tamanioNull", "null", "null"), ("tamanioString", '"3"', '"3"')]:
    mal(nombre, lexema, "cada tamanio es int; el error va en el tamanio, como en javac (§11.8)", f"""
class Aa {{
    void m() {{
        var v = new int[{tamanio}]; // <-
    }}
}}
""")

mal("segundoTamanioNoEntero", "b", "el segundo tamanio tambien es int (§11.8)", """
class Aa {
    void m(boolean b) {
        var v = new int[2][b]; // <-
    }
}
""")

mal("tamanioConOperadorNoEntero", "<", "el tamanio x < y es boolean; el error va en el operador", """
class Aa {
    void m(int x, int y) {
        var v = new int[x < y]; // <-
    }
}
""")

mal("arregloDeClaseNoDeclarada", "Nada", "el tipo base de un arreglo tiene que existir", """
class Aa {
    void m() {
        var v = new Nada[3]; // <-
    }
}
""")

mal("arregloDeGenericaRaw", "Caja", "el tipo base no puede ser una generica sin argumento (§11.8)", """
class Caja<T> { }
class Uso {
    void m() {
        var v = new Caja[3]; // <-
    }
}
""")

mal("arregloDeParametroDeTipoFueraDeSuClase", "T", "T[] solo dentro de la clase que declara T", """
class Aa {
    void m() {
        var v = new T[3]; // <-
    }
}
""")

for nombre, indice, lexema in [("indiceBoolean", "true", "true"), ("indiceChar", "'a'", "'a'"),
                               ("indiceNull", "null", "null"), ("indiceString", '"0"', '"0"')]:
    mal(nombre, lexema, "el indice es int; el error va en el indice, como en javac (§11.8)", f"""
class Aa {{
    void m(int[] v) {{
        var x = v[{indice}]; // <-
    }}
}}
""")

mal("indexarUnEntero", "[", "solo se indexa un arreglo (§11.8)", """
class Aa {
    void m(int n) {
        var x = n[0]; // <-
    }
}
""")

mal("indexarUnObject", "[", "una referencia de tipo Object no se indexa (§9.4)", """
class Aa {
    void m(Object o) {
        var x = o[0]; // <-
    }
}
""")

mal("indexarUnString", "[", "String no es un arreglo", """
class Aa {
    void m(String s) {
        var x = s[0]; // <-
    }
}
""")

mal("indexarDeMas", "[", "un int[] admite una sola indexacion (§11.8)", """
class Aa {
    void m(int[] v) {
        var x = v[0][0]; // <-
    }
}
""")

mal("indexarUnaLlamadaVoid", "[", "void no se indexa", """
class Aa {
    void v() { }
    void m() {
        var x = v()[0]; // <-
    }
}
""")

mal("componenteDeTipoIncompatible", "=", "el componente de un int[] es int", """
class Aa {
    void m(int[] v) {
        v[0] = true; // <-
    }
}
""")

mal("filaDeTipoIncompatible", "=", "la fila de un int[][] es int[], no int", """
class Aa {
    void m(int[][] m) {
        m[0] = 3; // <-
    }
}
""")

mal("arregloDeOtroTipoBase", "=", "un char[] no conforma con int[] (§9.4)", """
class Aa {
    void m(int[] v) {
        v = new char[3]; // <-
    }
}
""")

mal("errorEnElIndiceDeUnaAsignacion", "zz", "el indice del destino se chequea con el destino", """
class Aa {
    void m(int[] v) {
        v[zz] = 1; // <-
    }
}
""")
