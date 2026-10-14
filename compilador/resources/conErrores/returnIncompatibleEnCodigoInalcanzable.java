///[Error:'c'|6]
// el codigo inalcanzable se chequea igual (§12.5)
class Aa {
    int m() {
        return 1;
        return 'c'; // <-
    }
}
