///[Error:this|5]
// this no vale en un metodo estatico (§10.4)
class Aa {
    static void m() {
        var a = this; // <-
    }
}
