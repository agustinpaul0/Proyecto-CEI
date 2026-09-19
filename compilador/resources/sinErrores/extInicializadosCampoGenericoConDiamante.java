///[SinErrores]
// Inicializador de campo generico con diamante y tipo explicito

class Prueba {
    private Caja<Integer> a = new Caja<Integer>();
    private Caja<Integer> b = new Caja<>();
}

class Caja<T> {
}