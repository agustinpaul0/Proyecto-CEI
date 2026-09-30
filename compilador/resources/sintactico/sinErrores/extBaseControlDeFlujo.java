///[SinErrores]
// if/else anidados, while, var y return

class Control {
    int probar(int n) {
        var total = 0;
        var i = 0;
        if (n > 0) {
            total = n;
        } else {
            total = 0;
        }
        while (i < n) {
            i = i + 1;
        }
        if (n > 0) if (n > 10) total = 100; else total = 50;
        return total;
    }
}