"""Llamadas, encadenados y entidades predefinidas: documento §7, §10.2, §11.5 y §11.6."""
from catalogo import bien, mal

bien("llamadasSinReceptor", "una llamada sin receptor busca en la clase actual y sus ancestros (§10.2)", """
class Aa {
    int heredado() { return 1; }
}
class Bb extends Aa {
    int propio(int x) { return x; }
    void m() {
        var a = propio(heredado());
        a = propio(propio(a));
    }
}
""")

bien("sobrecargaPorAridad", "la aridad elige el metodo (§6.2)", """
class Aa {
    int f() { return 0; }
    boolean f(int x) { return true; }
    char f(int x, int y) { return 'c'; }
    void m() {
        var a = f();
        var b = f(1);
        var c = f(1, 2);
        a = 3;
        b = false;
        c = 'd';
    }
}
""")

bien("sobrecargaQueAtraviesaLaHerencia", "la sobrecarga por aridad combina propios y heredados", """
class Aa { int f() { return 0; } }
class Bb extends Aa {
    boolean f(int x) { return true; }
    void m(Bb b) {
        var i = f();
        var j = b.f(1);
        i = 1;
        j = false;
    }
}
""")

bien("llamadasEstaticas", "estaticos sin receptor, con la clase o con una expresion (§10.2)", """
class Aa {
    static int e() { return 1; }
    int d() { return e(); }
    static void m(Aa a) {
        var x = e();
        x = Aa.e();
        x = a.e();
        x = new Aa().e();
    }
}
""")

bien("estaticoHeredadoPorElNombreDeLaHija", "un estatico heredado se llama por la descendiente", """
class Aa { static int e() { return 1; } }
class Bb extends Aa {
    void m() {
        var x = Bb.e();
        x = e();
        Bb.debugPrint(x);
    }
}
""")

bien("llamadaVoidComoSentencia", "una llamada void vale como sentencia (§12.2)", """
class Aa {
    void v() { }
    static void s() { }
    void m() {
        v();
        this.v();
        s();
        Aa.s();
    }
}
""")

bien("llamadaConValorComoSentencia", "una llamada que produce valor vale como sentencia; el valor se descarta (§12.2)", """
class Aa {
    int n() { return 1; }
    Aa yo() { return this; }
    void m() {
        n();
        yo().yo().n();
        new Aa();
        new Aa().n();
    }
}
""")

bien("argumentosConformantes", "cada argumento conforma con su parametro (§11.6)", """
interface Ix { }
class Aa implements Ix { }
class Bb extends Aa { }
class Uso {
    void f(Aa a, Ix i, Object o, int[] v, String s) { }
    void m(Bb b) {
        f(b, b, b, null, null);
        f(new Aa(), new Bb(), new int[1], new int[2], "x");
    }
}
""")

bien("argumentosQueSonAsignaciones", "un argumento puede ser una asignacion", """
class Aa {
    int f(int x) { return x; }
    void m(int y) {
        f(y = 3);
        f(f(f(y)));
    }
}
""")

bien("encadenadosDeLlamadasYAtributos", "receptores encadenados de llamadas y atributos (§11.5)", """
class Persona {
    int edad;
    Persona pareja;
    int obtenerEdad() { return edad; }
    Persona yo() { return this; }
    static Persona crear() { return new Persona(); }
}
class Uso {
    void m(Persona p) {
        var e = p.yo().pareja.yo().obtenerEdad();
        e = Persona.crear().edad;
        e = Persona.crear().pareja.obtenerEdad();
        p.pareja.pareja.edad = e;
        Persona.crear().yo().obtenerEdad();
    }
}
""")

bien("metodoDeclaradoDespues", "un metodo se resuelve aunque se declare despues (§10.3)", """
class Aa {
    void m() {
        var x = despues();
        x = Bb.otro();
    }
    int despues() { return 1; }
}
class Bb { static int otro() { return 2; } }
""")

bien("llamadaRecursiva", "un metodo se llama a si mismo", """
class Aa {
    int f(int n) {
        if (n == 0) return 1;
        return n * f(n - 1);
    }
}
""")

bien("predefinidasDeSystem", "los diez metodos de System (§7)", """
class Aa {
    void m() {
        var c = System.read();
        c = c + 1;
        System.printB(true);
        System.printC('c');
        System.printI(c);
        System.printS("s");
        System.println();
        System.printBln(false);
        System.printCln('d');
        System.printIln(3);
        System.printSln("t");
    }
}
""")

bien("predefinidasDeObject", "debugPrint y toString de Object (§7)", """
class Aa {
    void m(Aa a, String s) {
        Object.debugPrint(3);
        debugPrint(4);
        s = a.toString();
        s = toString();
        s = "lit".toString();
        s = s.toString().toString();
        s = new Object().toString();
    }
}
""")

bien("toStringSobreArreglosEInterfaces", "un arreglo y una interfaz ofrecen el toString de Object (§11.8, duda 5)", """
interface Ix { }
class Aa {
    void m(int[] v, Ix i, String s) {
        s = v.toString();
        s = i.toString();
        s = new int[3].toString();
    }
}
""")

bien("receptorDeTipoInterfaz", "una interfaz ofrece sus metodos declarados y heredados (§10.2)", """
interface Ia { int f(); }
interface Ib extends Ia { boolean g(int x); }
class Cc implements Ib {
    int f() { return 1; }
    boolean g(int x) { return true; }
}
class Uso {
    void m(Ib i) {
        var a = i.f();
        var b = i.g(a);
        i = new Cc();
    }
}
""")

bien("retornoComoReceptorYOperando", "el retorno de una llamada se usa como operando o como receptor", """
class Aa {
    int x;
    Aa yo() { return this; }
    int n() { return 2; }
    boolean m() {
        return yo().x + n() * yo().yo().n() > 0;
    }
}
""")

mal("metodoNoDeclarado", "noExiste", "el metodo no esta en la clase ni en sus ancestros (§10.2)", """
class Aa {
    void m() {
        noExiste(); // <-
    }
}
""")

mal("metodoConOtraAridad", "f", "(f, 2) no existe aunque exista (f, 1) (§10.2)", """
class Aa {
    void f(int x) { }
    void m() {
        f(1, 2); // <-
    }
}
""")

mal("metodoSinArgumentosLlamadoConUno", "f", "(f, 1) no existe aunque exista (f, 0)", """
class Aa {
    void f() { }
    void m() {
        f(1); // <-
    }
}
""")

mal("metodoDeUnaDescendiente", "f", "un metodo de una hija no es visible en la madre", """
class Aa {
    void m() {
        f(); // <-
    }
}
class Bb extends Aa { void f() { } }
""")

mal("argumentoIncompatible", "f", "el argumento no conforma con el parametro: el error va en el metodo (semIIError04)", """
class Aa {
    void f(int x) { }
    void m() {
        f(true); // <-
    }
}
""")

mal("segundoArgumentoIncompatible", "f", "el segundo argumento no conforma", """
class Aa {
    void f(int x, String s) { }
    void m() {
        f(1, 2); // <-
    }
}
""")

mal("argumentoVoid", "f", "una llamada void no es un argumento (§9.5)", """
class Aa {
    void v() { }
    void f(int x) { }
    void m() {
        f(v()); // <-
    }
}
""")

mal("argumentoNullAPrimitivo", "f", "null no conforma con int", """
class Aa {
    void f(int x) { }
    void m() {
        f(null); // <-
    }
}
""")

mal("argumentoSuperclaseAParametroSubclase", "f", "Aa no conforma con Bb", """
class Aa { }
class Bb extends Aa { }
class Uso {
    void f(Bb b) { }
    void m(Aa a) {
        f(a); // <-
    }
}
""")

mal("argumentoCharAInt", "f", "char no conforma con int (§9.1)", """
class Aa {
    void f(int x) { }
    void m() {
        f('a'); // <-
    }
}
""")

mal("errorDentroDeUnArgumento", "+", "el error de un argumento se reporta en el argumento", """
class Aa {
    void f(int x) { }
    void m() {
        f(1 + true); // <-
    }
}
""")

mal("metodoInexistenteAntesQueSusArgumentos", "noExiste", "la llamada se resuelve antes de chequear sus argumentos (§11.6)", """
class Aa {
    void m() {
        noExiste(zz); // <-
    }
}
""")

mal("argumentoIncompatibleEnLlamadaEncadenada", "f", "en una llamada con receptor el error de argumento va en el metodo", """
class Aa { void f(int x) { } }
class Uso {
    void m(Aa a) {
        a.f("x"); // <-
    }
}
""")

mal("argumentoIncompatibleEnLlamadaEstatica", "f", "en una llamada estatica el error de argumento va en el metodo", """
class Aa { static void f(int x) { } }
class Uso {
    void m() {
        Aa.f(true); // <-
    }
}
""")

mal("argumentoIncompatibleASystem", "printI", "System.printI exige int (§7)", """
class Aa {
    void m() {
        System.printI("hola"); // <-
    }
}
""")

mal("argumentoIncompatibleADebugPrint", "debugPrint", "debugPrint exige int (§7)", """
class Aa {
    void m() {
        debugPrint('c'); // <-
    }
}
""")

mal("metodoDeInstanciaDesdeEstatico", "n", "en un estatico, una llamada sin receptor elige solo estaticos (§10.4)", """
class Aa {
    void n() { }
    static void m() {
        n(); // <-
    }
}
""")

mal("metodoDeInstanciaHeredadoDesdeEstatico", "toString", "toString es de instancia y no se llama sin receptor en un estatico", """
class Aa {
    static void m() {
        var s = toString(); // <-
    }
}
""")

mal("metodoDeInstanciaPorElNombreDeLaClase", ".", "un metodo de instancia no se llama con el nombre de la clase (§10.2)", """
class Aa {
    void n() { }
    void m() {
        Aa.n(); // <-
    }
}
""")

mal("toStringPorElNombreDeLaClase", ".", "toString es de instancia (§7)", """
class Aa {
    void m() {
        var s = String.toString(); // <-
    }
}
""")

mal("metodoEstaticoNoDeclaradoEnLaClase", ".", "el metodo no esta en la clase nombrada", """
class Aa { }
class Uso {
    void m() {
        Aa.f(); // <-
    }
}
""")

mal("metodoEstaticoConOtraAridad", ".", "(f, 1) no existe en la clase nombrada", """
class Aa { static void f() { } }
class Uso {
    void m() {
        Aa.f(1); // <-
    }
}
""")

mal("systemSinEseMetodo", ".", "System no declara print (§7)", """
class Aa {
    void m() {
        System.print(1); // <-
    }
}
""")

mal("systemPrintlnConArgumento", ".", "System.println no tiene parametros (§7)", """
class Aa {
    void m() {
        System.println("x"); // <-
    }
}
""")

mal("llamadaEstaticaAClaseNoDeclarada", "Nada", "la clase de una llamada estatica tiene que existir (§10.2)", """
class Aa {
    void m() {
        Nada.f(); // <-
    }
}
""")

mal("llamadaEstaticaAInterfaz", "Ix", "Clase.m exige una clase, no una interfaz (§10.2)", """
interface Ix { void f(); }
class Aa {
    void m() {
        Ix.f(); // <-
    }
}
""")

mal("metodoNoDeclaradoEnElReceptor", ".", "el metodo se busca en el tipo estatico del receptor (§10.2)", """
class Aa { }
class Uso {
    void m(Aa a) {
        a.f(); // <-
    }
}
""")

mal("metodoDeLaClaseDinamicaNoVisible", ".", "la seleccion usa el tipo estatico: Aa no tiene f aunque Bb si (§11.6)", """
class Aa { }
class Bb extends Aa { void f() { } }
class Uso {
    void m() {
        Aa a = null;
    }
    void n(Aa a) {
        a.f(); // <-
    }
}
""".replace("        Aa a = null;\n", "        var b = new Bb();\n"))

mal("metodoSobreReceptorPrimitivo", ".", "un primitivo no tiene metodos (§10.1)", """
class Aa {
    void m(int x) {
        x.f(); // <-
    }
}
""")

mal("metodoSobreLlamadaVoid", ".", "void no tiene metodos (§10.1)", """
class Aa {
    void v() { }
    void m() {
        v().toString(); // <-
    }
}
""")

mal("lengthComoMetodoDeArreglo", ".", "length es un atributo y no un metodo (§11.8)", """
class Aa {
    void m(int[] v) {
        var n = v.length(); // <-
    }
}
""")

mal("cloneDeArreglo", ".", "los arreglos no tienen clone (§11.8)", """
class Aa {
    void m(int[] v) {
        var c = v.clone(); // <-
    }
}
""")

mal("debugPrintSobreArreglo", ".", "un arreglo ofrece solo el toString de Object, no debugPrint (§13)", """
class Aa {
    void m(int[] v) {
        v.debugPrint(1); // <-
    }
}
""")

mal("metodoDeObjectEstaticoSobreInterfaz", ".", "una interfaz no tiene el debugPrint estatico de Object (JLS §9.2)", """
interface Ix { }
class Aa {
    void m(Ix i) {
        i.debugPrint(1); // <-
    }
}
""")

mal("metodoDeLaImplementacionSobreLaInterfaz", ".", "por una interfaz solo se ven sus metodos (§10.2)", """
interface Ix { }
class Aa implements Ix { void f() { } }
class Uso {
    void m(Ix i) {
        i.f(); // <-
    }
}
""")

mal("metodoSobreString", ".", "String no tiene metodos propios (§7)", """
class Aa {
    void m(String s) {
        var n = s.length(); // <-
    }
}
""")

mal("errorEnElReceptorAntesQueEnElMetodo", "zz", "el receptor se chequea antes que el metodo", """
class Aa {
    void m() {
        zz.f(); // <-
    }
}
""")

mal("atributoNoDeclaradoEnElReceptor", ".", "el atributo se busca en el tipo estatico del receptor (§10.1)", """
class Aa { }
class Uso {
    void m(Aa a) {
        var x = a.x; // <-
    }
}
""")

mal("atributoDeLaHijaPorLaMadre", ".", "un atributo de la hija no se ve por un receptor de la madre", """
class Aa { }
class Bb extends Aa { int x; }
class Uso {
    void m(Aa a) {
        a.x = 1; // <-
    }
}
""")

mal("atributoSobreInterfaz", ".", "las interfaces no declaran atributos (§10.1)", """
interface Ix { }
class Uso {
    void m(Ix i) {
        var x = i.x; // <-
    }
}
""")

mal("atributoSobrePrimitivo", ".", "un primitivo no tiene atributos (§10.1)", """
class Aa {
    void m(int n) {
        var x = n.x; // <-
    }
}
""")

mal("atributoSobreString", ".", "String no tiene atributos", """
class Aa {
    void m(String s) {
        var x = s.length; // <-
    }
}
""")

mal("lengthSobreNoArreglo", ".", "length es solo de los arreglos (§11.8)", """
class Aa {
    void m(Aa a) {
        var x = a.length; // <-
    }
}
""")

mal("lengthSobreObject", ".", "un arreglo con tipo estatico Object no ofrece length (§11.8)", """
class Aa {
    void m(Object o) {
        o = new int[3];
        var x = o.length; // <-
    }
}
""")

mal("atributoDeArregloDistintoDeLength", ".", "un arreglo tiene length y nada mas (§11.8)", """
class Aa {
    void m(int[] v) {
        var x = v.size; // <-
    }
}
""")

mal("atributoSobreLlamadaVoid", ".", "void no tiene atributos", """
class Aa {
    int x;
    void v() { }
    void m() {
        var y = v().x; // <-
    }
}
""")

mal("atributoSobreThisEnEstatico", "this", "this no vale en un metodo estatico, aunque se acceda a un atributo (§10.4)", """
class Aa {
    int x;
    static void m() {
        var y = this.x; // <-
    }
}
""")

mal("llamadaEncadenadaConErrorAlFinal", ".", "un encadenado largo falla en su ultimo eslabon", """
class Aa {
    Aa sig;
    Aa yo() { return this; }
    void m() {
        yo().sig.yo().sig.nada(); // <-
    }
}
""")
