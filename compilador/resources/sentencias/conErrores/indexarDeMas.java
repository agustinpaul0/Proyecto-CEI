///[Error:[|5]
// un int[] admite una sola indexacion (§11.8)
class Aa {
    void m(int[] v) {
        var x = v[0][0]; // <-
    }
}
