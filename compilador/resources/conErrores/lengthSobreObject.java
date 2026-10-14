///[Error:.|6]
// un arreglo con tipo estatico Object no ofrece length (§11.8)
class Aa {
    void m(Object o) {
        o = new int[3];
        var x = o.length; // <-
    }
}
