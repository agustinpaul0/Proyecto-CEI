///[Error:=|7]
// Bb[] no conforma con Aa[] (§9.4)
class Aa { }
class Bb extends Aa { }
class Uso {
    void m(Aa[] a, Bb[] b) {
        a = b; // <-
    }
}
