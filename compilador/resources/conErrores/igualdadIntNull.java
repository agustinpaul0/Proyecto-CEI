///[Error:==|5]
// null no conforma con un primitivo (§9.5)
class Aa {
    void m(int i, boolean r) {
        r = i == null; // <-
    }
}
