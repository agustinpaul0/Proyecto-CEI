// una local puede llamarse como un metodo
class Aa {
    int m() { return 1; }
    void n() {
        var m = m();
        m = m + m();
    }
}
