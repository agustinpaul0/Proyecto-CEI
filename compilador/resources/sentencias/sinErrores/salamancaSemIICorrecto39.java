// CP-SII-34: sustitucion generica desde el receptor (incluye arreglos) y herencia sin argumento
class Base<T> {
    T dato;
    T[] vector;

    T obtener() {
        return dato;
    }

    void setVector(T[] v) {
        vector = v;
    }

    T[] getVector() {
        return vector;
    }
}

class Intermedia<U> extends Base<U> {
}

class Final extends Intermedia<String> {
}

class Caja<T> {
    T contenido;

    T obtener() {
        return contenido;
    }

    void guardar(T valor) {
        this.contenido = valor;
    }
}

class Derivada extends Base {
}

class Uso {
    void m1() {
        var caja = new Caja<String>();
        caja.guardar("hola");
        var s = caja.obtener();
        var f = new Final();
        var sf = f.obtener();
        f.dato = "texto";
        f.setVector(new String[2]);
        var v = f.getVector();
    }

    void m2() {
        var d = new Derivada();
        var t = d.obtener();
    }
}
