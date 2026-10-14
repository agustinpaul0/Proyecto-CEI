///[Error:=|5]
// 1 no conforma con String: sin conversiones ni boxing (§9.1)
class Aa {
    void m(String d) {
        d = 1; // <-
    }
}
