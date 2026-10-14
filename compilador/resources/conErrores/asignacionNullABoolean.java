///[Error:=|5]
// null no conforma con boolean: sin conversiones ni boxing (§9.1)
class Aa {
    void m(boolean d) {
        d = null; // <-
    }
}
