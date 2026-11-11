///[Error:Aa|6]
// una clase no generica no admite argumento (§3)
class Aa { }
class Uso {
    void m() {
        var a = new Aa<String>(); // <-
    }
}
