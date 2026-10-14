///[Error:f|6]
// en una llamada estatica el error de argumento va en el metodo
class Aa { static void f(int x) { } }
class Uso {
    void m() {
        Aa.f(true); // <-
    }
}
