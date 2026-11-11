///[Error:=|5]
// null no conforma con int: sin conversiones ni boxing (§9.1)
class Aa {
    void m(int d) {
        d = null; // <-
    }
}
