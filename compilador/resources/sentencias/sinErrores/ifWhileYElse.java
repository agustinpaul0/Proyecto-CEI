// if con y sin else y while con condicion boolean (§12.4)
class Aa {
    void m(boolean b, int i) {
        if (b) i = 1;
        if (i < 3) i = 2; else i = 3;
        if (b && (i == 3)) { i = 4; } else { i = 5; }
        while (i > 0) i = i - 1;
        while (b) { b = false; }
        if (b) ; else ;
        while (false) ;
    }
}
