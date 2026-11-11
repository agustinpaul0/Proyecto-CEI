// un metodo se llama a si mismo
class Aa {
    int f(int n) {
        if (n == 0) return 1;
        return n * f(n - 1);
    }
}
