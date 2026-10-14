// CP-SII-49: no hay llamada implicita a un constructor de la superclase
class Base {
    public Base(int valor) {
    }
}

class Derivada extends Base {
    int campo;

    public Derivada() {
        this.campo = 1;
    }
}