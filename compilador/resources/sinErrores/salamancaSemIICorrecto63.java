// CN-SII-P063: variable tipada con interfaz e invocacion de su metodo (S10.5)
interface Figurable {
    int area();
}

class Cuadrado implements Figurable {
    public int area() {
        return 4;
    }
}

class Principal {
    void m1(Figurable f) {
        var v = f.area();
    }
}