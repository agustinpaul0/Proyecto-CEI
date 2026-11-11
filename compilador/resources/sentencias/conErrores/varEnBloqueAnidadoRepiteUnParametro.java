///[Error:x|6]
// tampoco en un bloque anidado (§12.3)
class Aa {
    void m(int x) {
        {
            var x = 1; // <-
        }
    }
}
