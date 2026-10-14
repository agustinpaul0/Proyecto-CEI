// CP-SII-20: llamada implicita a metodos de instancia y sobrecarga por aridad
class Sobrecarga {
    int repetir() {
        return 1;
    }

    int repetir(int x) {
        return x;
    }

    void m1() {
        var a = repetir();
        var b = repetir(2);
    }
}
