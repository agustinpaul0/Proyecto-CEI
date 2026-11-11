// CN-SII-P066: null como argumento y en comparaciones con referencias y arreglos (S9.4)
class Persona {
}

class Principal {
    void usar(Persona p) {
    }

    void m1(Persona p, int[] arreglo) {
        usar(null);
        var a = p == null;
        var b = null == arreglo;
    }
}