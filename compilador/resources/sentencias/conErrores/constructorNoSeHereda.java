///[Error:new|7]
// los constructores no se heredan (§6.3)
class Aa { Aa(int x) { } }
class Bb extends Aa { }
class Uso {
    void m() {
        var b = new Bb(1); // <-
    }
}
