///[Error:1|5]
// un constructor no retorna un valor (§12.5)
class Aa {
    Aa() {
        return 1; // <-
    }
}
