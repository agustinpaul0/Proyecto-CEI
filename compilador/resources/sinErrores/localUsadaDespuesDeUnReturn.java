// las sentencias despues de un return se chequean y ven las locales (§12.6)
class Aa {
    int m() {
        var x = 1;
        return x;
        x = 2;
    }
}
