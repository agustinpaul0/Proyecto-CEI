///[Error:.|6]
// una interfaz no tiene el debugPrint estatico de Object (JLS §9.2)
interface Ix { }
class Aa {
    void m(Ix i) {
        i.debugPrint(1); // <-
    }
}
