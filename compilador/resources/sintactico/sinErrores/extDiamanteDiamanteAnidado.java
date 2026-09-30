///[SinErrores]
// Diamante anidado: argumento de tipo que es otra clase generica

class Prueba {
    void main() {
        var a = new Par<Caja<Integer>>();
        var b = new Par<>();
    }
}

class Par<T> {
}

class Caja<T> {
}