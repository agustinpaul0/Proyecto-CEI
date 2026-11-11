// un return dentro de un while
class Aa {
    int m(int n) {
        while (n > 0) {
            if (n == 5) return n;
            n = n - 1;
        }
        return 0;
    }
}
