///[SinErrores]
// Diamante con argumentos de construccion variados

class Prueba {
    void main() {
        var a = new Caja<>(1, 2);
        var b = new Caja<>("clave", 3 + 4);
        var c = new Caja<>(true, this);
    }
}

class Caja<T> {
}