///[SinErrores]
// Lambdas y variables var dentro de if/else/while

class Control {

    int probar(int n) {
        var f = x -> x + 1;
        var total = 0;
        if (n > 0) {
            total = f.aplicar(n);
        } else {
            total = n;
        }
        while (total < 10) {
            var g = y -> y * 2;
            total = g.aplicar(total);
        }
        return total;
    }
}