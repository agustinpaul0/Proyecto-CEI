// un estatico heredado se llama por la descendiente
class Aa { static int e() { return 1; } }
class Bb extends Aa {
    void m() {
        var x = Bb.e();
        x = e();
        Bb.debugPrint(x);
    }
}
