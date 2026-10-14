// CP-SII-21: llamada implicita a un metodo estatico desde instancia y desde contexto estatico
class Estaticos {
    static int calcular() {
        return 1;
    }

    static void m1() {
        var a = calcular();
        calcular();
    }

    void m2() {
        var b = calcular();
    }
}
