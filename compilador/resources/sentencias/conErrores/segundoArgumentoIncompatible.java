///[Error:f|6]
// el segundo argumento no conforma
class Aa {
    void f(int x, String s) { }
    void m() {
        f(1, 2); // <-
    }
}
