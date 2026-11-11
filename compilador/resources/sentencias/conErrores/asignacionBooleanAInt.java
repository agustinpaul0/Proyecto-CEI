///[Error:=|5]
// true no conforma con int: sin conversiones ni boxing (§9.1)
class Aa {
    void m(int d) {
        d = true; // <-
    }
}
