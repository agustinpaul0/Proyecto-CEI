// el argumento puede ser una interfaz o una predefinida
interface Ix { }
class Caja<T> { T dato; }
class Uso {
    void m(Caja<Ix> a, Caja<Object> b, Caja<String> c) {
        a.dato = null;
        b.dato = c;
        b.dato = "s";
    }
}
