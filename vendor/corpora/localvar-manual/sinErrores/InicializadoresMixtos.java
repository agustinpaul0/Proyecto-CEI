///[SinErrores]
// Inicializadores locales variados: literales, objetos y arreglos

class Prueba {
    void main() {
        int a = 0, b = 1 + 2, c;
        boolean p = false, q = !p;
        Caja o1 = new Caja(), o2 = new Caja[2];
        int[] xs = new int[3], ys = new int[2];
    }
}

class Caja {
}