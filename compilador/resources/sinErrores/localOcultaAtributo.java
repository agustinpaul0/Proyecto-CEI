// una local puede ocultar un atributo (§12.3)
class Aa {
    int x;
    void m() {
        var x = true;
        x = false;
        this.x = 3;
    }
}
