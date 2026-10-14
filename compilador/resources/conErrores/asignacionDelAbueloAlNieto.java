///[Error:=|8]
// no se permite la direccion inversa, tampoco a dos niveles
class Aa { }
class Bb extends Aa { }
class Cc extends Bb { }
class Uso {
    void m(Aa a, Cc c) {
        c = a; // <-
    }
}
