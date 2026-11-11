///[Error:Caja|7]
// CN-SII-120: instanciacion raw de clase generica prohibida (S10.3)
class Caja<T> {
}
class Uso {
    void m1() {
        var c = new Caja();
    }
}
