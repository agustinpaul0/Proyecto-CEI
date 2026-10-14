///[Error:this|5]
// los parentesis no habilitan this en un estatico (§11.4)
class Aa {
    static void m() {
        var a = (this); // <-
    }
}
