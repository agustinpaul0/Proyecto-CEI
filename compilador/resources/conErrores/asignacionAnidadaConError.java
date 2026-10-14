///[Error:=|5]
// el error de la asignacion de adentro se reporta primero
class Aa {
    void m(int x, boolean b) {
        x = (b = 1); // <-
    }
}
