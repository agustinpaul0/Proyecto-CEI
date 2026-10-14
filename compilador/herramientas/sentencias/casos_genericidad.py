"""Genericidad: documento §5, §5.1, §5.2 y §9.3."""
from catalogo import bien, mal

bien("miembrosVistosDesdeElReceptor", "desde Caja<String>, T se ve como String en atributos, parametros y retornos (§5.1)", """
class Caja<T> {
    T contenido;
    T[] varios;
    T obtener() { return contenido; }
    void guardar(T valor) { contenido = valor; }
    Caja<T> mismo() { return this; }
}
class Uso {
    void m(Caja<String> c, String s) {
        s = c.contenido;
        s = c.obtener();
        c.guardar("x");
        c.guardar(s);
        c.guardar(null);
        s = c.varios[0];
        c.varios = new String[3];
        c = c.mismo();
        s = c.mismo().mismo().obtener();
        Object o = null;
    }
}
""".replace("        Object o = null;\n", "        var o = c.obtener().toString();\n"))

bien("cuerpoGenericoChequeadoParametricamente", "dentro de Caja<T> los valores de tipo T conforman solo con T (§9.3)", """
class Caja<T> {
    T contenido;
    T obtener() { return contenido; }
    void guardar(T valor) { contenido = valor; }
    void copiar(Caja<T> otra) {
        contenido = otra.obtener();
        otra.guardar(contenido);
        var t = this.obtener();
        t = null;
        guardar(t);
        var yo = this;
        yo = otra;
    }
}
""")

bien("sustitucionEncadenadaPorLaHerencia", "en una cadena de herencia se componen las sustituciones (§5.1)", """
class Base<T> {
    T dato;
    T obtener() { return dato; }
}
class Intermedia<U> extends Base<U> {
    U deIntermedia() { return obtener(); }
}
class Final extends Intermedia<String> {
    void m(Final f) {
        String s = null;
    }
}
""".replace("        String s = null;\n", "        var s = f.dato;\n        s = f.obtener();\n        s = obtener();\n        s = dato;\n        s = f.deIntermedia();\n"))

bien("herenciaSinArgumentoConservaT", "con extends Base sin argumento, lo heredado conserva T (§5)", """
class Base<T> {
    T dato;
    T obtener() { return dato; }
}
class Derivada extends Base {
    void m(Derivada d) {
        var t = d.obtener();
        t = d.dato;
        d.dato = t;
        t = null;
    }
}
""")

bien("implementaInterfazGenericaConArgumento", "una clase que implementa Contenedor<String> conforma con Contenedor<String> (§9.3)", """
interface Contenedor<T> { T sacar(); }
class Cajon implements Contenedor<String> {
    String sacar() { return "x"; }
}
class Uso {
    void m(Contenedor<String> c) {
        c = new Cajon();
        var s = c.sacar();
        s = new Cajon().sacar();
    }
}
""")

bien("genericoInstanciadoConformaConsigoMismo", "Caja<String> conforma con Caja<String> y con Object", """
class Caja<T> { }
class Uso {
    void m(Caja<String> a, Caja<String> b, Object o, boolean r) {
        a = b;
        o = a;
        r = a == b;
        r = a == null;
    }
}
""")

bien("thisEnUnaClaseGenerica", "this tiene tipo Caja<T> dentro de Caja<T> (§11.3)", """
class Caja<T> {
    Caja<T> yo() { return this; }
    void m() {
        var c = this;
        c = yo();
    }
}
""")

bien("estaticoConGenericosInstanciados", "un estatico puede usar Caja<String> aunque no T (§10.4)", """
class Caja<T> {
    static void valido() {
        var datos = new Caja<String>[10];
        var una = new Caja<String>();
        datos[0] = una;
    }
}
""")

bien("argumentoDeTipoInterfazYPredefinida", "el argumento puede ser una interfaz o una predefinida", """
interface Ix { }
class Caja<T> { T dato; }
class Uso {
    void m(Caja<Ix> a, Caja<Object> b, Caja<String> c) {
        a.dato = null;
        b.dato = c;
        b.dato = "s";
    }
}
""")

mal("TNoConformaConObject", "contenido", "T no conforma con Object: no tiene cotas (§9.3)", """
class Caja<T> {
    T contenido;
    Object comoObjeto() {
        return contenido; // <-
    }
}
""")

mal("ObjectNoConformaConT", "=", "Object no conforma con T (§9.3)", """
class Caja<T> {
    T contenido;
    void guardarObjeto(Object objeto) {
        contenido = objeto; // <-
    }
}
""")

mal("StringNoConformaConT", "=", "un valor String no conforma con un destino T (§9.3)", """
class Caja<T> {
    T contenido;
    void m() {
        contenido = "s"; // <-
    }
}
""")

mal("TNoConformaConString", "=", "T no conforma con String (§9.3)", """
class Caja<T> {
    T contenido;
    void m(String s) {
        s = contenido; // <-
    }
}
""")

mal("metodoSobreReceptorT", ".", "un T no instanciado no garantiza los metodos de Object (§10.2)", """
class Caja<T> {
    T contenido;
    void m() {
        var s = contenido.toString(); // <-
    }
}
""")

mal("atributoSobreReceptorT", ".", "un T no permite seleccionar atributos (§10.1)", """
class Caja<T> {
    T contenido;
    void m() {
        var x = contenido.dato; // <-
    }
}
""")

mal("arregloDeTNoConformaConObjectArreglo", "=", "T[] no conforma con Object[] (§9.4)", """
class Caja<T> {
    void m(T[] ts, Object[] os) {
        os = ts; // <-
    }
}
""")

mal("sustitucionHaceIncompatibleUnArgumento", "guardar", "desde Caja<String> el parametro T se chequea como String (§5.1)", """
class Caja<T> {
    void guardar(T valor) { }
}
class Uso {
    void m(Caja<String> c) {
        c.guardar(3); // <-
    }
}
""")

mal("sustitucionHaceIncompatibleUnAtributo", "=", "desde Caja<String>, el atributo T es String", """
class Caja<T> {
    T dato;
}
class Uso {
    void m(Caja<String> c) {
        c.dato = 3; // <-
    }
}
""")

mal("sustitucionHaceIncompatibleUnRetorno", "=", "desde Caja<Uso>, obtener() es Uso y no String", """
class Caja<T> {
    T obtener() { return null; }
}
class Uso {
    void m(Caja<Uso> c, String s) {
        s = c.obtener(); // <-
    }
}
""")

mal("sustitucionEnArreglosDeT", "=", "desde Caja<String>, T[] es String[]", """
class Caja<T> {
    T[] varios;
}
class Uso {
    void m(Caja<String> c) {
        c.varios = new int[3]; // <-
    }
}
""")

mal("herenciaSinArgumentoNoDaObject", "=", "el T conservado por extends Base no es Object (§5.1)", """
class Base<T> {
    T obtener() { return null; }
}
class Derivada extends Base { }
class Uso {
    void m(Derivada d, Object o) {
        o = d.obtener(); // <-
    }
}
""")

mal("herenciaSinArgumentoNoDaString", "=", "el T conservado no se reemplaza por un tipo concreto (§5)", """
class Base<T> {
    T dato;
}
class Derivada extends Base { }
class Uso {
    void m(Derivada d) {
        d.dato = "s"; // <-
    }
}
""")

mal("TConservadaNoEsLaDeLaHija", "=", "Der<T> extends Base: la T heredada es la de Base, no la de Der (§5.1, duda 4)", """
class Base<T> {
    T dato;
}
class Der<T> extends Base {
    T propio;
    void m() {
        propio = dato; // <-
    }
}
""")

mal("cadenaDeHerenciaInstanciaMal", "=", "Final extends Intermedia<String>: el dato heredado es String", """
class Base<T> { T dato; }
class Intermedia<U> extends Base<U> { }
class Final extends Intermedia<String> {
    void m() {
        dato = 3; // <-
    }
}
""")

mal("genericoNoConformaPorElArgumento", "=", "Caja<Bb> no conforma con Caja<Aa> aunque Bb extienda Aa (§9.3)", """
class Aa { }
class Bb extends Aa { }
class Caja<T> { }
class Uso {
    void m(Caja<Aa> a, Caja<Bb> b) {
        a = b; // <-
    }
}
""")

mal("implementaConArgumentoNoConformaConOtroArgumento", "=", "Cajon implementa Contenedor<String> y no Contenedor<Object> (§9.3)", """
interface Contenedor<T> { }
class Cajon implements Contenedor<String> { }
class Uso {
    void m(Contenedor<Object> c) {
        c = new Cajon(); // <-
    }
}
""")

mal("TEnUnMetodoEstaticoDelCuerpo", "T", "T no se usa en el cuerpo de un estatico (§10.4)", """
class Caja<T> {
    static void crear() {
        var datos = new T[10]; // <-
    }
}
""")

mal("TComoArgumentoEnUnEstatico", "T", "la prohibicion alcanza a los argumentos genericos (§10.4)", """
class Caja<T> {
    static void crear() {
        var c = new Caja<T>(); // <-
    }
}
""")

mal("TComoArgumentoDeArregloEnUnEstatico", "T", "y a los tipos base de arreglos genericos (§10.4)", """
class Caja<T> {
    static void crear() {
        var c = new Caja<T>[2]; // <-
    }
}
""")

mal("atributoRawEnLaDeclaracion", "Caja", "fuera de la herencia, un generico sin argumento es error (§5.2, duda 3)", """
class Caja<T> { }
class Uso {
    Caja dato; // <-
}
""")

mal("parametroRawEnLaDeclaracion", "Caja", "un parametro tampoco puede ser raw (§5.2, duda 3)", """
class Caja<T> { }
class Uso {
    void m(Caja c) { } // <-
}
""")

mal("retornoRawEnLaDeclaracion", "Caja", "un retorno tampoco puede ser raw (§5.2, duda 3)", """
class Caja<T> { }
class Uso {
    Caja m() { return null; } // <-
}
""")

mal("arregloRawEnLaDeclaracion", "Caja", "ni el tipo base de un arreglo (§5.2, duda 3)", """
class Caja<T> { }
class Uso {
    Caja[] datos; // <-
}
""")

mal("varConCreacionRaw", "Caja", "var caja = new Caja() es error (§5.2)", """
class Caja<T> {
    T contenido;
}
class Uso {
    Caja<String> texto;
    void ejemplo() {
        var caja = new Caja(); // <-
        var otra = new Caja<String>();
    }
}
""")
