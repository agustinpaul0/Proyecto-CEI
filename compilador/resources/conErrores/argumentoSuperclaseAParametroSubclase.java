///[Error:f|8]
// Aa no conforma con Bb
class Aa { }
class Bb extends Aa { }
class Uso {
    void f(Bb b) { }
    void m(Aa a) {
        f(a); // <-
    }
}
