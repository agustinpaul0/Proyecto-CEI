"""Asignaciones: documento §11.9, con la conformidad del §9."""
from catalogo import bien, mal

bien("asignacionADestinosAsignables", "locales, parametros, atributos y componentes son asignables (§11.9)", """
class Aa {
    int at;
    Aa otro;
    int[] v;
    int[][] mat;
    void m(int p) {
        var l = 0;
        l = 1;
        p = 2;
        at = 3;
        this.at = 4;
        otro.at = 5;
        otro.otro.otro.at = 6;
        v[0] = 7;
        mat[1][2] = 8;
        mat[0] = v;
    }
}
""")

bien("asignacionSobreLlamadaYCreacion", "el ultimo acceso decide: m().a y new Aa().a son asignables (§11.5)", """
class Aa {
    int at;
    Aa crear() { return new Aa(); }
    void m() {
        crear().at = 1;
        new Aa().at = 2;
        this.crear().crear().at = 3;
    }
}
""")

bien("asignacionDeAtributoHeredado", "un atributo heredado es asignable como uno propio", """
class Aa { int at; }
class Bb extends Aa { }
class Cc extends Bb {
    void m(Cc c) {
        at = 1;
        c.at = 2;
        this.at = 3;
    }
}
""")

bien("asignacionConformantePorHerencia", "una subclase conforma con sus ancestros y con Object (§9.2)", """
class Aa { }
class Bb extends Aa { }
class Cc extends Bb { }
class Uso {
    void m(Aa a, Bb b, Cc c, Object o) {
        a = b;
        a = c;
        b = c;
        o = c;
        o = a;
    }
}
""")

bien("asignacionConformantePorInterfaz", "una clase conforma con su interfaz y las ancestras de esta (§9.2)", """
interface Ia { }
interface Ib extends Ia { }
class Cc implements Ib { }
class Dd extends Cc { }
class Uso {
    void m(Ia a, Ib b, Cc c, Dd d, Object o) {
        b = c;
        a = c;
        a = b;
        a = d;
        o = b;
    }
}
""")

bien("asignacionDeNullAReferencias", "null conforma con clases, interfaces, arreglos y T (§9.5)", """
interface Ix { }
class Caja<T> {
    T dato;
    void m(Caja<String> c, Ix i, int[] v, String s) {
        c = null;
        i = null;
        v = null;
        s = null;
        dato = null;
    }
}
""")

bien("asignacionDeArreglosAObject", "todo arreglo conforma con Object (§9.4)", """
class Aa {
    void m(Object o, int[] v, String[][] s) {
        o = v;
        o = s;
        o = new int[2];
    }
}
""")

bien("asignacionComoExpresion", "la asignacion produce el valor del destino (§11.9)", """
class Aa {
    int x;
    int m(int y) {
        x = (y = 3);
        var z = (x = 4);
        return x = 5;
    }
}
""")

bien("asignacionDeStringYObject", "String conforma con Object", """
class Aa {
    void m(Object o, String s) {
        o = s;
        o = "literal";
        s = "otro";
    }
}
""")

for nombre, fuente in [
    ("asignacionALiteral", "3 = x;"),
    ("asignacionAThis", "this = null;"),
    ("asignacionALlamada", "crear() = null;"),
    ("asignacionACreacion", "new Aa() = null;"),
    ("asignacionAParentizada", "(x) = 3;"),
    ("asignacionABinaria", "x + 1 = 3;"),
    ("asignacionAUnaria", "-x = 3;"),
    ("asignacionALength", "v.length = 3;"),
    ("asignacionAAtributoDeParentizada", "(this).x = 3;"),
    ("asignacionAComponenteDeParentizada", "(v)[0] = 3;"),
    ("asignacionALlamadaEncadenada", "this.crear() = null;"),
    ("asignacionAStringLiteral", "\"s\" = null;"),
    ("asignacionANull", "null = null;"),
    ("asignacionALlamadaEstatica", "Aa.fabrica() = null;"),
]:
    mal(nombre, "=", "el lado izquierdo no es asignable (§11.9, §11.4)", f"""
class Aa {{
    int x;
    int[] v;
    Aa crear() {{ return this; }}
    static Aa fabrica() {{ return null; }}
    void m() {{
        {fuente} // <-
    }}
}}
""")

mal("asignacionAParentizadaDeLength", "=", "los parentesis no hacen asignable a nada (§11.4)", """
class Aa {
    void m(int[] v) {
        (v.length) = 3; // <-
    }
}
""")

for nombre, destino, origen, previo in [
    ("asignacionBooleanAInt", "int", "true", ""),
    ("asignacionIntABoolean", "boolean", "1", ""),
    ("asignacionCharAInt", "int", "'a'", ""),
    ("asignacionIntAChar", "char", "65", ""),
    ("asignacionStringAInt", "int", '"1"', ""),
    ("asignacionNullAInt", "int", "null", ""),
    ("asignacionNullABoolean", "boolean", "null", ""),
    ("asignacionIntAString", "String", "1", ""),
    ("asignacionCharAString", "String", "'c'", ""),
    ("asignacionObjectAString", "String", "new Object()", ""),
    ("asignacionIntAObject", "Object", "3", ""),
    ("asignacionBooleanAObject", "Object", "true", ""),
]:
    mal(nombre, "=", f"{origen} no conforma con {destino}: sin conversiones ni boxing (§9.1)", f"""
class Aa {{
    void m({destino} d) {{
        d = {origen}; // <-
    }}
}}
""")

mal("asignacionDeLlamadaVoid", "=", "una llamada void no es un lado de asignacion (§9.5)", """
class Aa {
    void v() { }
    void m(int i) {
        i = v(); // <-
    }
}
""")

mal("asignacionSuperclaseASubclase", "=", "no se permite la direccion inversa del subtipado (§9.2)", """
class Aa { }
class Bb extends Aa { }
class Uso {
    void m(Aa a, Bb b) {
        b = a; // <-
    }
}
""")

mal("asignacionDelAbueloAlNieto", "=", "no se permite la direccion inversa, tampoco a dos niveles", """
class Aa { }
class Bb extends Aa { }
class Cc extends Bb { }
class Uso {
    void m(Aa a, Cc c) {
        c = a; // <-
    }
}
""")

mal("asignacionEntreHermanas", "=", "dos hijas de la misma clase no conforman", """
class Aa { }
class Bb extends Aa { }
class Cc extends Aa { }
class Uso {
    void m(Bb b, Cc c) {
        b = c; // <-
    }
}
""")

mal("asignacionInterfazAClase", "=", "una interfaz no conforma con una clase que la implementa (§9.2)", """
interface Ix { }
class Cc implements Ix { }
class Uso {
    void m(Ix i, Cc c) {
        c = i; // <-
    }
}
""")

mal("asignacionClaseAInterfazNoImplementada", "=", "una clase no conforma con una interfaz que no implementa", """
interface Ix { }
class Cc { }
class Uso {
    void m(Ix i, Cc c) {
        i = c; // <-
    }
}
""")

mal("asignacionSuperinterfazASubinterfaz", "=", "una interfaz no conforma con la que la extiende", """
interface Ia { }
interface Ib extends Ia { }
class Uso {
    void m(Ia a, Ib b) {
        b = a; // <-
    }
}
""")

mal("asignacionObjectAClase", "=", "Object no conforma con una clase", """
class Aa { }
class Uso {
    void m(Aa a, Object o) {
        a = o; // <-
    }
}
""")

mal("asignacionObjectAInterfaz", "=", "Object no conforma con una interfaz", """
interface Ix { }
class Uso {
    void m(Ix i, Object o) {
        i = o; // <-
    }
}
""")

mal("asignacionArregloCovariante", "=", "los arreglos son invariantes: String[] no conforma con Object[] (§9.4)", """
class Aa {
    void m(String[] s, Object[] o) {
        o = s; // <-
    }
}
""")

mal("asignacionArregloDeSubclases", "=", "Bb[] no conforma con Aa[] (§9.4)", """
class Aa { }
class Bb extends Aa { }
class Uso {
    void m(Aa[] a, Bb[] b) {
        a = b; // <-
    }
}
""")

mal("asignacionMatrizAArreglo", "=", "int[][] no conforma con int[] (§9.4)", """
class Aa {
    void m(int[][] m, int[] v) {
        v = m; // <-
    }
}
""")

mal("asignacionFilaAObjectArreglo", "=", "una fila String[] no conforma con Object[] (§9.4)", """
class Aa {
    void m(String[][] m, Object[] o) {
        o = m[0]; // <-
    }
}
""")

mal("asignacionArregloAInt", "=", "un arreglo no es un int", """
class Aa {
    void m(int[] v, int i) {
        i = v; // <-
    }
}
""")

mal("asignacionComponenteAArreglo", "=", "un componente int no es un int[]", """
class Aa {
    void m(int[] v) {
        v = v[0]; // <-
    }
}
""")

mal("asignacionObjectAArreglo", "=", "Object no conforma con un arreglo", """
class Aa {
    void m(int[] v, Object o) {
        v = o; // <-
    }
}
""")

mal("asignacionGenericaInvariante", "=", "Caja<String> no conforma con Caja<Object> (§9.3)", """
class Caja<T> { }
class Uso {
    void m(Caja<String> s, Caja<Object> o) {
        o = s; // <-
    }
}
""")

mal("asignacionGenericaInvarianteAlReves", "=", "Caja<Object> no conforma con Caja<String> (§9.3)", """
class Caja<T> { }
class Uso {
    void m(Caja<String> s, Caja<Object> o) {
        s = o; // <-
    }
}
""")

mal("asignacionArregloGenericoInvariante", "=", "Caja<String>[] no conforma con Caja<Object>[] (§9.4)", """
class Caja<T> { }
class Uso {
    void m(Caja<String>[] s, Caja<Object>[] o) {
        o = s; // <-
    }
}
""")

mal("asignacionLadoIzquierdoNoDeclaradoPrimero", "zz", "el lado izquierdo se chequea antes que el derecho", """
class Aa {
    void m() {
        zz = yy; // <-
    }
}
""")

mal("asignacionLadoDerechoNoDeclarado", "yy", "con el lado izquierdo correcto, el error esta en el derecho", """
class Aa {
    void m(int x) {
        x = yy; // <-
    }
}
""")

mal("asignacionChequeaElDerechoAntesQueLaAsignabilidad", "yy", "los dos lados se chequean antes que la asignabilidad (Clase 11, slides 26, 31 y 36)", """
class Aa {
    void m() {
        this = yy; // <-
    }
}
""")

mal("asignacionDeAsignacionIncompatible", "=", "x = (b = true): el resultado boolean no conforma con int", """
class Aa {
    void m(int x, boolean b) {
        x = (b = true); // <-
    }
}
""")

mal("asignacionAnidadaConError", "=", "el error de la asignacion de adentro se reporta primero", """
class Aa {
    void m(int x, boolean b) {
        x = (b = 1); // <-
    }
}
""")
