///[Error:=|5]
// un char[] no conforma con int[] (§9.4)
class Aa {
    void m(int[] v) {
        v = new char[3]; // <-
    }
}
