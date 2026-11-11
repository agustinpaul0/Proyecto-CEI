///[Error:=|7]
// una interfaz no conforma con una clase que la implementa (§9.2)
interface Ix { }
class Cc implements Ix { }
class Uso {
    void m(Ix i, Cc c) {
        c = i; // <-
    }
}
