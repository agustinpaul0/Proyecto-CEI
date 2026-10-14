///[Error:f|6]
// el argumento no conforma con el parametro: el error va en el metodo (semIIError04)
class Aa {
    void f(int x) { }
    void m() {
        f(true); // <-
    }
}
