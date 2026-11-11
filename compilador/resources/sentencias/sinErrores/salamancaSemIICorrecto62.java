// CN-SII-P062: herencia, atributo heredado y redefinicion con mismo tipo de retorno (S10.1)
class Base {
    public int edad;

    public int obtener() {
        return this.edad;
    }
}

class Hijo extends Base {
    public int obtener() {
        return edad + 1;
    }
}

class Principal {
    void m1(Hijo h) {
        h.edad = 4;
        var v = h.obtener();
    }
}