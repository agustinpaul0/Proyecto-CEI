// CN-SII-P061: metodo estatico seleccionado por nombre de clase y por expresion receptora (S10.4)
class Utilidad {
    static int doble(int n) {
        return n * 2;
    }
}

class Principal {
    void m1(Utilidad u) {
        var a = Utilidad.doble(3);
        var b = u.doble(3);
        var c = a + b;
    }
}