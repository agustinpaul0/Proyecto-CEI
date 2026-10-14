///[Error:yy|10]
// la primera clase esta bien y el error esta en la segunda
class Aa {
    void m() {
        var x = 1;
    }
}
class Bb {
    void n() {
        yy = 2; // <-
    }
}
