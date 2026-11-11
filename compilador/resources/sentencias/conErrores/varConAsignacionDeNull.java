///[Error:x|6]
// x = null tiene el tipo del destino, pero var y = (o = null) toma el de o
class Aa {
    void m(Aa o) {
        var y = (o = null);
        var x = (null); // <-
    }
}
