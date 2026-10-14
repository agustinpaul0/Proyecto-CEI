///[Error:=|6]
// una llamada void no es un lado de asignacion (§9.5)
class Aa {
    void v() { }
    void m(int i) {
        i = v(); // <-
    }
}
