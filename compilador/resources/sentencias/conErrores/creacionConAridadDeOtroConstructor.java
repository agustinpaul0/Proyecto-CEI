///[Error:new|6]
// con un constructor declarado no existe el de por defecto (§6.3)
class Aa { Aa(int x) { } }
class Uso {
    void m() {
        var a = new Aa(); // <-
    }
}
