///0&1&3&10&11&13&exitosamente
class Aa {
    int m() {
        return 0;
    }
    int m(int a) {
        return a;
    }
    int m(int a, int b) {
        return a + b;
    }
    static int s() {
        return 10;
    }
    static int s(int a) {
        return 10 + a;
    }
    static int s(int a, int b) {
        return 10 + a + b;
    }
}

class Init {
    static void main() {
        var a = new Aa();
        debugPrint(a.m());
        debugPrint(a.m(1));
        debugPrint(a.m(1, 2));
        debugPrint(Aa.s());
        debugPrint(Aa.s(1));
        debugPrint(Aa.s(1, 2));
    }
}
