///[SinErrores]
// Formas combinadas: tipo explicito y diamante en la misma expresion

class Prueba {
    void main() {
        var a = new Caja<Integer>();
        var b = new Caja<>();
        var c = new Caja<Integer>();
        var d = c;
    }
}

class Caja<T> {
}