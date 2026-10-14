// CP-SII-42: un tipo generico instanciado conforma consigo mismo y new devuelve el tipo completo
class Caja<T> {
    T contenido;

    void guardar(T valor) {
        contenido = valor;
    }

    T obtener() {
        return contenido;
    }
}

class Uso {
    Caja<String> campo;

    void aceptar(Caja<String> c) {
    }

    Caja<String> devolver() {
        return new Caja<String>();
    }

    void m1() {
        campo = new Caja<String>();
        aceptar(campo);
        var otra = campo;
    }
}