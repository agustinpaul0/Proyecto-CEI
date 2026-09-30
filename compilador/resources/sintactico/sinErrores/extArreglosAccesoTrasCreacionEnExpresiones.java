///[SinErrores]
// El patron new T[]{...}[i] en diversos puestos de expresion

class Prueba {
    void main() {
        metodo(new int[]{1, 2}[0]);
        var y = new int[]{1, 2}[0] + 1;
        if (new int[]{1, 2}[0] == 1) metodo(0);
        while (new int[]{1, 2}[0] > 0) metodo(0);
        var z = new int[]{1, 2}[0] < new int[]{3, 4}[1];
    }

    int metodo(int a) {
        return new int[]{1, 2}[0];
    }
}