// un metodo se resuelve aunque se declare despues (§10.3)
class Aa {
    void m() {
        var x = despues();
        x = Bb.otro();
    }
    int despues() { return 1; }
}
class Bb { static int otro() { return 2; } }
