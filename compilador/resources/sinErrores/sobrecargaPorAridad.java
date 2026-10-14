// la aridad elige el metodo (§6.2)
class Aa {
    int f() { return 0; }
    boolean f(int x) { return true; }
    char f(int x, int y) { return 'c'; }
    void m() {
        var a = f();
        var b = f(1);
        var c = f(1, 2);
        a = 3;
        b = false;
        c = 'd';
    }
}
