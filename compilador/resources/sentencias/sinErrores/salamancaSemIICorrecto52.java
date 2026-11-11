// CP-SII-47: un metodo estatico heredado puede invocarse sin receptor y mediante el nombre de la clase descendiente
class Base {
    static int calcular() {
        return 1;
    }
}

class Sub extends Base {
    void m1() {
        var a = calcular();
        var b = Sub.calcular();
        Sub.calcular();
    }
}