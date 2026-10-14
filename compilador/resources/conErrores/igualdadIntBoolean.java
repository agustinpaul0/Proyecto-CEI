///[Error:==|5]
// int y boolean no conforman en ninguna direccion (§11.2)
class Aa {
    void m(int i, boolean r) {
        r = i == r; // <-
    }
}
