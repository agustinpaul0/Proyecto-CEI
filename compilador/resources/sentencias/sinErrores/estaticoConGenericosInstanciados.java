// un estatico puede usar Caja<String> aunque no T (§10.4)
class Caja<T> {
    static void valido() {
        var datos = new Caja<String>[10];
        var una = new Caja<String>();
        datos[0] = una;
    }
}
