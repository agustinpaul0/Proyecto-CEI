///[Error:=|5]
// 3 no conforma con Object: sin conversiones ni boxing (§9.1)
class Aa {
    void m(Object d) {
        d = 3; // <-
    }
}
