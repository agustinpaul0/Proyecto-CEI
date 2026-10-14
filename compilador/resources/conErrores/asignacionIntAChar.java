///[Error:=|5]
// 65 no conforma con char: sin conversiones ni boxing (§9.1)
class Aa {
    void m(char d) {
        d = 65; // <-
    }
}
