///[Error:.|6]
// el metodo se busca en el tipo estatico del receptor (§10.2)
class Aa { }
class Uso {
    void m(Aa a) {
        a.f(); // <-
    }
}
