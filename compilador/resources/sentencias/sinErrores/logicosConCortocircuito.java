// && y || sobre boolean dan boolean (§11.2)
class Aa {
    void m(boolean a, boolean b, boolean r) {
        r = a && b;
        r = a || b;
        r = a && b || a && b;
    }
}
