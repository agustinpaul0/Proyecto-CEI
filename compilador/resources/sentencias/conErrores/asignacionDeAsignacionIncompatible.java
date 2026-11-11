///[Error:=|5]
// x = (b = true): el resultado boolean no conforma con int
class Aa {
    void m(int x, boolean b) {
        x = (b = true); // <-
    }
}
