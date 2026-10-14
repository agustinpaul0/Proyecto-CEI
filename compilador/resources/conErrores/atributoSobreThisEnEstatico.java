///[Error:this|6]
// this no vale en un metodo estatico, aunque se acceda a un atributo (§10.4)
class Aa {
    int x;
    static void m() {
        var y = this.x; // <-
    }
}
