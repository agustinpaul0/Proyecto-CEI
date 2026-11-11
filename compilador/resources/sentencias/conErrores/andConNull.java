///[Error:&&|5]
// null no es boolean
class Aa {
    void m(boolean r) {
        r = null && r; // <-
    }
}
