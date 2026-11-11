///[Error:==|5]
// char e int no se comparan (§11.2)
class Aa {
    void m(char c, boolean r) {
        r = c == 97; // <-
    }
}
