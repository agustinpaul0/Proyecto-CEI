///[Error:x|6]
// en un estatico un atributo no se usa sin receptor (§10.1, §10.4)
class Aa {
    int x;
    static void m() {
        x = 1; // <-
    }
}
