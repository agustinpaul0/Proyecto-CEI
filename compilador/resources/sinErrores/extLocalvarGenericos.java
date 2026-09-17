///[SinErrores]
// Variables locales tipadas con genericos y con parametro de tipo

class Genericos<T> {

    T probar() {
        Caja<T> caja;
        Caja<T> a, b = new Caja<T>(), c;
        T valor;
        valor = caja.obtener();
        return valor;
    }
}

class Caja<U> {
    U obtener() {
        return null;
    }
}