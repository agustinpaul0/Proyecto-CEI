// if y while anidados con bloques y locales a varios niveles
class Aa {
    int m(int n) {
        var total = 0;
        while (n > 0) {
            var i = n;
            if (i > 10) {
                while (i > 10) {
                    var j = i;
                    if (j == 15) {
                        {
                            total = total + j;
                        }
                    } else {
                        total = total - 1;
                    }
                    i = i - 1;
                }
            }
            n = n - 1;
        }
        return total;
    }
}
