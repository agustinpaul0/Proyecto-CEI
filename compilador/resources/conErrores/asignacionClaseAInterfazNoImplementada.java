///[Error:=|7]
// una clase no conforma con una interfaz que no implementa
interface Ix { }
class Cc { }
class Uso {
    void m(Ix i, Cc c) {
        i = c; // <-
    }
}
