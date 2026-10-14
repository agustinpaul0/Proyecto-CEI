///[Error:x|6]
// la repeticion no depende del tipo
class Aa {
    void m() {
        var x = 1;
        var x = true; // <-
    }
}
