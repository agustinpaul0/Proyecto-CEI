///[Error:y|6]
// tampoco del else (§12.3)
class Aa {
    void m(boolean c) {
        if (c) ;
        else var y = 1; // <-
    }
}
