///[Error:f|6]
// en una llamada con receptor el error de argumento va en el metodo
class Aa { void f(int x) { } }
class Uso {
    void m(Aa a) {
        a.f("x"); // <-
    }
}
