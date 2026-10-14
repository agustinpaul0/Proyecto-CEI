///[Error:x|8]
// una local de otro metodo no es visible
class Aa {
    void m() {
        var x = 1;
    }
    void n() {
        x = 2; // <-
    }
}
