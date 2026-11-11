// CP-SII-17: acceso implicito a atributos y metodos heredados
class Base {
    int base;

    int leer() {
        return base;
    }
}

class Sub extends Base {
    int sub;

    void m1(int p) {
        base = p;
        sub = leer();
    }
}
