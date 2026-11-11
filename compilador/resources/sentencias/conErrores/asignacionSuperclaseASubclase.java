///[Error:=|7]
// no se permite la direccion inversa del subtipado (§9.2)
class Aa { }
class Bb extends Aa { }
class Uso {
    void m(Aa a, Bb b) {
        b = a; // <-
    }
}
