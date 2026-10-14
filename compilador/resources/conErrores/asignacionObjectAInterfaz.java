///[Error:=|6]
// Object no conforma con una interfaz
interface Ix { }
class Uso {
    void m(Ix i, Object o) {
        i = o; // <-
    }
}
