///[Error:this|6]
// this tampoco como argumento
class Aa {
    static void f(Aa a) { }
    static void m() {
        f(this); // <-
    }
}
