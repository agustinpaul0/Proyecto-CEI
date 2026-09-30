///[Error:>|5]
// El diamante no es valido en el tipo de campo; solo en 'new'

class Prueba {
    private Caja<> caja = new Caja<>();
}

class Caja<T> {
}