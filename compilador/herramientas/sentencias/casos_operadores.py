"""Literales y operadores: documento §11.1 y §11.2."""
from catalogo import bien, mal

bien("literalesDeCadaTipo", "cada literal tiene su tipo (§11.1)", """
class Aa {
    void m(int i, char c, boolean b, String s, Aa o) {
        i = 42;
        c = 'z';
        b = true;
        b = false;
        s = "hola";
        o = null;
    }
}
""")

bien("aritmeticaEntera", "los cinco operadores aritmeticos sobre int dan int (§11.2)", """
class Aa {
    int m(int a, int b) {
        a = a + b;
        a = a - b;
        a = a * b;
        a = a / b;
        a = a % b;
        return a + b - a * b / a % b;
    }
}
""")

bien("relacionalesEnteros", "los relacionales sobre int dan boolean (§11.2)", """
class Aa {
    void m(int a, int b, boolean r) {
        r = a < b;
        r = a > b;
        r = a <= b;
        r = a >= b;
    }
}
""")

bien("logicosConCortocircuito", "&& y || sobre boolean dan boolean (§11.2)", """
class Aa {
    void m(boolean a, boolean b, boolean r) {
        r = a && b;
        r = a || b;
        r = a && b || a && b;
    }
}
""")

bien("igualdadDePrimitivos", "== y != entre primitivos del mismo tipo (§11.2)", """
class Aa {
    void m(int i, char c, boolean b, boolean r) {
        r = i == i;
        r = c == 'a';
        r = b != true;
        r = 'x' != c;
    }
}
""")

bien("igualdadEntreReferenciasRelacionadas", "== en las dos direcciones de la conformidad (§11.2)", """
class Aa { }
class Bb extends Aa { }
interface Ix { }
class Cc implements Ix { }
class Uso {
    void m(Aa a, Bb b, Ix i, Cc c, Object o, String s, boolean r) {
        r = a == b;
        r = b == a;
        r = i != c;
        r = o == s;
        r = s == o;
        r = a == a;
    }
}
""")

bien("igualdadConNull", "null se compara con referencias, arreglos y consigo mismo (§9.5)", """
class Aa {
    void m(Aa a, int[] v, String s, boolean r) {
        r = a == null;
        r = null != v;
        r = s == null;
        r = null == null;
    }
}
""")

bien("igualdadDeArreglosIguales", "dos arreglos del mismo tipo se comparan (§11.2)", """
class Aa {
    void m(int[] a, int[] b, Object o, boolean r) {
        r = a == b;
        r = a != o;
    }
}
""")

bien("unarios", "+ y - sobre int, ! sobre boolean (§11.2)", """
class Aa {
    void m(int i, boolean b) {
        i = -i;
        i = +i;
        i = -(i + 1);
        b = !b;
        b = !(i < 3);
    }
}
""")

bien("agrupacionPlanaDeIzquierdaADerecha", "los binarios van en un nivel y asocian a izquierda (§11, duda 1)", """
class Aa {
    void m(int a, int b, boolean r) {
        var x = a - b - a;
        r = a < b == true;
        r = a == b == false;
        var y = 1 + 2 * 3;
    }
}
""")

bien("comparacionDeComparaciones", "(a < b) == (c < d) compara dos boolean", """
class Aa {
    void m(int a, int b, boolean r) {
        r = (a < b) == (b < a);
        r = !(a == b) && (a != b);
    }
}
""")

bien("expresionesLargas", "una expresion larga con parentesis y todos los operadores", """
class Aa {
    boolean m(int a, int b) {
        return ((a + b) * (a - b) / (a % (b + 1)) > 0) && !(a <= b) || (a >= b) && (a != 0) || (b == 0);
    }
}
""")

for operador, nombre in [("+", "Suma"), ("-", "Resta"), ("*", "Multiplicacion"), ("/", "Division"), ("%", "Modulo")]:
    mal(f"{nombre.lower()}IntBoolean", operador, f"{operador} exige int y recibe boolean (§11.2)", f"""
class Aa {{
    void m(int i, boolean b) {{
        i = i {operador} b; // <-
    }}
}}
""")
    mal(f"{nombre.lower()}DeChars", operador, f"{operador} no admite char: no hay promocion numerica (§11.2)", f"""
class Aa {{
    void m(char c, int i) {{
        i = c {operador} c; // <-
    }}
}}
""")

mal("sumaDeStringsNoConcatena", "+", "no hay concatenacion de cadenas con + (§11.2)", """
class Aa {
    void m(String s) {
        s = s + "x"; // <-
    }
}
""")

mal("sumaIntString", "+", "+ no convierte int a String (§11.2)", """
class Aa {
    void m(String s) {
        s = "x" + 1; // <-
    }
}
""")

mal("sumaConNull", "+", "+ no admite null", """
class Aa {
    void m(int i) {
        i = i + null; // <-
    }
}
""")

mal("sumaDeReferencias", "+", "+ no admite referencias", """
class Aa {
    void m(Aa a, int i) {
        i = a + a; // <-
    }
}
""")

mal("sumaConLlamadaVoid", "+", "un resultado void no se usa con operadores (§11.2)", """
class Aa {
    void v() { }
    void m(int i) {
        i = 1 + v(); // <-
    }
}
""")

for operador in ["<", ">", "<=", ">="]:
    mal({"<": "menor", ">": "mayor", "<=": "menorIgual", ">=": "mayorIgual"}[operador] + "DeBooleanos", operador,
        f"{operador} exige int (§11.2)", f"""
class Aa {{
    void m(boolean a, boolean r) {{
        r = a {operador} a; // <-
    }}
}}
""")

mal("menorDeChars", "<", "char no se compara con < (§11.2)", """
class Aa {
    void m(char c, boolean r) {
        r = c < 'z'; // <-
    }
}
""")

mal("mayorDeReferencias", ">", "> no admite referencias", """
class Aa {
    void m(String s, boolean r) {
        r = s > s; // <-
    }
}
""")

mal("andDeEnteros", "&&", "&& exige boolean (§11.2)", """
class Aa {
    void m(int i, boolean r) {
        r = i && i; // <-
    }
}
""")

mal("orIntBoolean", "||", "|| exige boolean en los dos lados (§11.2)", """
class Aa {
    void m(int i, boolean r) {
        r = r || i; // <-
    }
}
""")

mal("andConNull", "&&", "null no es boolean", """
class Aa {
    void m(boolean r) {
        r = null && r; // <-
    }
}
""")

mal("igualdadIntBoolean", "==", "int y boolean no conforman en ninguna direccion (§11.2)", """
class Aa {
    void m(int i, boolean r) {
        r = i == r; // <-
    }
}
""")

mal("igualdadCharInt", "==", "char e int no se comparan (§11.2)", """
class Aa {
    void m(char c, boolean r) {
        r = c == 97; // <-
    }
}
""")

mal("distintoCharInt", "!=", "char e int no se comparan (§11.2)", """
class Aa {
    void m(char c, boolean r) {
        r = 1 != c; // <-
    }
}
""")

mal("igualdadIntNull", "==", "null no conforma con un primitivo (§9.5)", """
class Aa {
    void m(int i, boolean r) {
        r = i == null; // <-
    }
}
""")

mal("igualdadClasesNoRelacionadas", "==", "dos clases sin relacion no se comparan (§11.2)", """
class Aa { }
class Bb { }
class Uso {
    void m(Aa a, Bb b, boolean r) {
        r = a == b; // <-
    }
}
""")

mal("igualdadClasesHermanas", "!=", "dos hijas de una misma clase no conforman entre si", """
class Aa { }
class Bb extends Aa { }
class Cc extends Aa { }
class Uso {
    void m(Bb b, Cc c, boolean r) {
        r = b != c; // <-
    }
}
""")

mal("igualdadInterfazYClaseQueNoLaImplementa", "==", "una interfaz y una clase que no la implementa (§9.2)", """
interface Ix { }
class Aa { }
class Uso {
    void m(Ix i, Aa a, boolean r) {
        r = i == a; // <-
    }
}
""")

mal("igualdadArreglosDeDistintoTipo", "==", "int[] y boolean[] no conforman (§9.4)", """
class Aa {
    void m(int[] a, boolean[] b, boolean r) {
        r = a == b; // <-
    }
}
""")

mal("igualdadArreglosDeDistintaDimension", "==", "int[] e int[][] no conforman (§9.4)", """
class Aa {
    void m(int[] a, int[][] b, boolean r) {
        r = a == b; // <-
    }
}
""")

mal("igualdadArregloYString", "==", "un arreglo conforma con Object y nada mas (§9.4)", """
class Aa {
    void m(int[] a, String s, boolean r) {
        r = a == s; // <-
    }
}
""")

mal("igualdadDeLlamadasVoid", "==", "void no se compara (§11.2)", """
class Aa {
    void v() { }
    void m(boolean r) {
        r = v() == v(); // <-
    }
}
""")

mal("igualdadStringInt", "==", "String e int no se comparan", """
class Aa {
    void m(boolean r) {
        r = "1" == 1; // <-
    }
}
""")

mal("menosUnarioSobreBoolean", "-", "- unario exige int (§11.2)", """
class Aa {
    void m(boolean b, int i) {
        i = -b; // <-
    }
}
""")

mal("masUnarioSobreChar", "+", "+ unario exige int (§11.2)", """
class Aa {
    void m(char c, int i) {
        i = +c; // <-
    }
}
""")

mal("negacionDeEntero", "!", "! exige boolean (§11.2)", """
class Aa {
    void m(int i, boolean b) {
        b = !i; // <-
    }
}
""")

mal("negacionDeNull", "!", "! exige boolean", """
class Aa {
    void m(boolean b) {
        b = !null; // <-
    }
}
""")

mal("negacionDeString", "!", "! exige boolean", """
class Aa {
    void m(boolean b) {
        b = !"true"; // <-
    }
}
""")

mal("menosUnarioSobreLlamadaVoid", "-", "un resultado void no se usa con operadores (§11.2)", """
class Aa {
    void v() { }
    void m(int i) {
        i = -v(); // <-
    }
}
""")

mal("precedenciaPlanaMenorYSuma", "+", "en un solo nivel x < y + 1 es (x < y) + 1 (§11, duda 1)", """
class Aa {
    void m(int x, int y) {
        var b = x < y + 1; // <-
    }
}
""")

mal("precedenciaPlanaOrYComparacion", "||", "en un solo nivel b || x == 1 es (b || x) == 1 (§11, duda 1)", """
class Aa {
    void m(boolean b, int x, boolean r) {
        r = b || x == 1; // <-
    }
}
""")

mal("precedenciaPlanaSumaYAnd", "&&", "en un solo nivel x + 1 > 0 && b vale, y x > 0 && x < 1 es ((x > 0) && x) < 1 y falla en el && (§11, duda 1)", """
class Aa {
    void m(boolean b, int x, boolean r) {
        r = x + 1 > 0 && b;
        r = x > 0 && x < 1; // <-
    }
}
""")

mal("errorEnElOperandoIzquierdoPrimero", "&&", "una binaria chequea sus dos operandos y despues el operador", """
class Aa {
    void m(int i, boolean r) {
        r = (i && i) || (r + r); // <-
    }
}
""")

mal("errorEnElOperandoDerechoDeUnaBinaria", "zz", "el operando derecho se chequea antes que el operador", """
class Aa {
    void m(boolean r) {
        r = 1 + zz; // <-
    }
}
""")

mal("operadorEnLaUltimaSentencia", "*", "el error esta en la ultima sentencia de un metodo largo", """
class Aa {
    void m(int a, boolean b) {
        a = a + 1;
        a = a - 1;
        b = !b;
        a = a * b; // <-
    }
}
""")

mal("operadorDentroDeParentesisProfundos", "+", "un error dentro de varios parentesis", """
class Aa {
    void m(int a, boolean b) {
        a = ((((a + b)))); // <-
    }
}
""")
