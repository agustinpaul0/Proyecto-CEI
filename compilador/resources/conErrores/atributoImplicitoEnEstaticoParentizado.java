///[Error:x|6]
// ni dentro de parentesis (§10.4)
class Aa {
    int x;
    static int m() {
        return (x); // <-
    }
}
