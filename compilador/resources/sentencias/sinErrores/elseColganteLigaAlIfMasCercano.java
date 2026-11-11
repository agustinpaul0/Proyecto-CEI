// el else liga al if mas cercano y las dos ramas se chequean
class Aa {
    void m(boolean a, boolean b, int i) {
        if (a) if (b) i = 1; else i = 2;
        if (a) { if (b) i = 3; } else i = 4;
    }
}
