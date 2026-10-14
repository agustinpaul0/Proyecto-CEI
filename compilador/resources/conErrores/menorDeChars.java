///[Error:<|5]
// char no se compara con < (§11.2)
class Aa {
    void m(char c, boolean r) {
        r = c < 'z'; // <-
    }
}
