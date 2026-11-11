///[Error:.|6]
// las interfaces no declaran atributos (§10.1)
interface Ix { }
class Uso {
    void m(Ix i) {
        var x = i.x; // <-
    }
}
