///[Error:=|5]
// 1 no conforma con boolean: sin conversiones ni boxing (§9.1)
class Aa {
    void m(boolean d) {
        d = 1; // <-
    }
}
