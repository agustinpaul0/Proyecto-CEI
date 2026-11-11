///[Error:x|7]
// una local visible de un contenedor no se oculta (§12.3)
class Aa {
    void m() {
        var x = 1;
        if (true) {
            var x = 2; // <-
        }
    }
}
