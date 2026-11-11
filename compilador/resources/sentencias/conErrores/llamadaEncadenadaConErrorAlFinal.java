///[Error:.|7]
// un encadenado largo falla en su ultimo eslabon
class Aa {
    Aa sig;
    Aa yo() { return this; }
    void m() {
        yo().sig.yo().sig.nada(); // <-
    }
}
