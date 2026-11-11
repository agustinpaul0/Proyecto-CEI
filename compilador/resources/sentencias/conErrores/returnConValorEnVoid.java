///[Error:1|5]
// un metodo void solo admite return; el error va en el valor (§12.5)
class Aa {
    void m() {
        return 1; // <-
    }
}
