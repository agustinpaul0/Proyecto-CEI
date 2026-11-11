///[Error:.|6]
// el atributo se busca en el tipo estatico del receptor (§10.1)
class Aa { }
class Uso {
    void m(Aa a) {
        var x = a.x; // <-
    }
}
