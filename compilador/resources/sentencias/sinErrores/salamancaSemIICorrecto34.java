// CP-SII-29: arreglos de referencias, genericos y parametros de tipo
class Caja<T> {
}

class Bolsa<T> {
    Caja<String>[] cajasGenericas;
    T[] elementos;
    T[][] matrizParametrica;

    void m1() {
        cajasGenericas = new Caja<String>[10];
        var ampliada = new Caja<String>[3][4];
        elementos = new T[10];
        matrizParametrica = new T[3][4];
        var c = cajasGenericas[0];
        var e = elementos[0];
    }
}
