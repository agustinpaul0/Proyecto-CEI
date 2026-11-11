// un argumento puede ser una asignacion
class Aa {
    int f(int x) { return x; }
    void m(int y) {
        f(y = 3);
        f(f(f(y)));
    }
}
