// CP-SII-36: un metodo estatico puede usar tipos genericos instanciados
class Caja<T> {
    static void m1() {
        var c = new Caja<String>();
        var arr = new Caja<String>[10];
        var s = c.toString();
    }
}
