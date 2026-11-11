///[Error:.|5]
// los arreglos no tienen clone (§11.8)
class Aa {
    void m(int[] v) {
        var c = v.clone(); // <-
    }
}
