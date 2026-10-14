///[Error:x|6]
// dos locales con el mismo nombre en un bloque (§12.3)
class Aa {
    void m() {
        var x = 1;
        var x = 2; // <-
    }
}
