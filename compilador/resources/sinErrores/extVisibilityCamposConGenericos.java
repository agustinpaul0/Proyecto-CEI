///[SinErrores]
// Campos con visibilidad y tipos genericos

class Prueba<T> {
    public Caja<T> publica;
    private Caja<T> privada;
    Caja<T> implicita;
    public Caja<Entero>[] publicaArr;
}

class Caja<U> {
}

class Entero {
}