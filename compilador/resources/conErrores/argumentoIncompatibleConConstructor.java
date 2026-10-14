///[Error:true|6]
// en un constructor el error va en el argumento, como en javac
class Aa { Aa(int x) { } }
class Uso {
    void m() {
        var a = new Aa(true); // <-
    }
}
