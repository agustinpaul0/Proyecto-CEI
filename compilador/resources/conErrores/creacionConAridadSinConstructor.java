///[Error:new|6]
// no hay constructor con esa aridad (§10.3)
class Aa { }
class Uso {
    void m() {
        var a = new Aa(1); // <-
    }
}
