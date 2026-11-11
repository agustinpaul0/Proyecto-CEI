///[Error:+|6]
// el error de un argumento se reporta en el argumento
class Aa {
    void f(int x) { }
    void m() {
        f(1 + true); // <-
    }
}
