///[Error:==|7]
// una interfaz y una clase que no la implementa (§9.2)
interface Ix { }
class Aa { }
class Uso {
    void m(Ix i, Aa a, boolean r) {
        r = i == a; // <-
    }
}
