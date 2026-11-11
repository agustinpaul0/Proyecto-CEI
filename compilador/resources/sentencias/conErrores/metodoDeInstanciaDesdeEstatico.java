///[Error:n|6]
// en un estatico, una llamada sin receptor elige solo estaticos (§10.4)
class Aa {
    void n() { }
    static void m() {
        n(); // <-
    }
}
