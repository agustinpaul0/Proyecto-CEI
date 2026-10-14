///[Error:.|8]
// como sentencia solo vale una asignacion o una llamada (§12.2, §11.4)
class Aa {
    int x;
    int[] v;
    Aa yo() { return this; }
    void m(boolean b) {
        v.length; // <-
    }
}
