///1&2&3&4&5&12345&exitosamente
class Init {
    static int ver(int x) {
        debugPrint(x);
        return x;
    }
    static int juntar(int a, int b, int c, int d, int e) {
        return (a * 10000) + (b * 1000) + (c * 100) + (d * 10) + e;
    }
    static void main() {
        debugPrint(juntar(ver(1), ver(2), ver(3), ver(4), ver(5)));
    }
}
