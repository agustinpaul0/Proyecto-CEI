///[Error:n|6]
// una llamada implicita a un metodo de instancia en un estatico (§10.4)
class Aa {
    int n() { return 1; }
    static void m() {
        System.printI(n()); // <-
    }
}
