///[Error:(|6]
// void no conforma con int
class Aa {
    void v() { }
    int m() {
        return v(); // <-
    }
}
