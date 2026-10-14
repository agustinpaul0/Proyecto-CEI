///[Error:==|5]
// String e int no se comparan
class Aa {
    void m(boolean r) {
        r = "1" == 1; // <-
    }
}
