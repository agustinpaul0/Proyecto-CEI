///[Error:yy|8]
// el error esta al final del archivo
class Aa { void m() { } }
class Bb { void m() { } }
class Cc {
    void m() { }
    void n() {
        var x = yy; // <-
    }
}
