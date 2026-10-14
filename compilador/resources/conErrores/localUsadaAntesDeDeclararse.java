///[Error:x|5]
// una local es visible recien despues de su declaracion (§10.3)
class Aa {
    void m() {
        x = 1; // <-
        var x = 2;
    }
}
