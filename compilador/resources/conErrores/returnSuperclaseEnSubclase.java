///[Error:a|6]
// Aa no conforma con Bb
class Aa { }
class Bb extends Aa {
    Bb m(Aa a) {
        return a; // <-
    }
}
