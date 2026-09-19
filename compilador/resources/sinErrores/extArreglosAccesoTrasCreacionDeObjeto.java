///[SinErrores]
// Acceso sobre una instancia recien creada: new Foo()[i] con encadenado

class Prueba {
    void main() {
        var a = new Caja()[0].campo;
        var b = new Caja()[1].metodo();
    }
}

class Caja {
    int campo;

    int metodo() {
        return 0;
    }
}