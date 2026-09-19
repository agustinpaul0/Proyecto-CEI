///[SinErrores]
// Metodos y estaticos con visibilidad y genericos

class Prueba {
    public static Caja<Integer> crear() {
        return new Caja<Integer>();
    }

    private Caja<Integer> obtener() {
        return new Caja<Integer>();
    }

    static Caja<Integer> crearImplicito() {
        return new Caja<Integer>();
    }
}

class Caja<T> {
}