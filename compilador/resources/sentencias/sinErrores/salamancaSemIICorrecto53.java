// CP-SII-48: sobrecarga de constructores por aridad
class Caja {
    int capacidad;

    public Caja() {
        this.capacidad = 0;
    }

    public Caja(int n) {
        this.capacidad = n;
    }
}

class Uso {
    void m1() {
        var a = new Caja();
        var b = new Caja(10);
    }
}