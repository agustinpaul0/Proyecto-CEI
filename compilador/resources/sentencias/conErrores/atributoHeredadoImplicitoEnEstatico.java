///[Error:x|6]
// tampoco uno heredado
class Aa { int x; }
class Bb extends Aa {
    static void m() {
        var y = x; // <-
    }
}
