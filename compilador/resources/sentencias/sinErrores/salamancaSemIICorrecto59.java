// CP-SII-54: crear arreglos cuyo argumento generico usa el parametro de la clase
class Caja<T> {
    T contenido;
}

class Estante<T> {
    Caja<T>[][] estanteria;

    void m1() {
        estanteria = new Caja<T>[3][4];
        estanteria[0][1] = new Caja<T>();
        var caja = estanteria[0][1];
    }
}