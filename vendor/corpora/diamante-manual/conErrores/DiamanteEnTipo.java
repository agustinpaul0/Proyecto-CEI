// [Error:>|5]
// El diamante solo es valido en la instanciacion, no como tipo de campo

class Prueba {
    private Caja<> caja;
}

class Caja<T> {
}