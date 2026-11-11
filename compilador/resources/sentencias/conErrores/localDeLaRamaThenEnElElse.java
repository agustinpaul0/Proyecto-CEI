///[Error:x|8]
// la local de un bloque del then no es visible en el else
class Aa {
    void m(boolean b) {
        if (b) {
            var x = 1;
        } else {
            x = 2; // <-
        }
    }
}
