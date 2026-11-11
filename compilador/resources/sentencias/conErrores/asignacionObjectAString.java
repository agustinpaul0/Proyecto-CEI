///[Error:=|5]
// new Object() no conforma con String: sin conversiones ni boxing (§9.1)
class Aa {
    void m(String d) {
        d = new Object(); // <-
    }
}
