///1&2&1&2&3&10&2&exitosamente
interface Primera {
    int a();
}

interface Segunda extends Primera {
    int b();
}

class Zz implements Segunda {
    int c() {
        return 3;
    }
    int b() {
        return 2;
    }
    int a() {
        return 1;
    }
}

class Ww extends Zz {
    int a() {
        return 10;
    }
}

class Init {
    static int porPrimera(Primera p) {
        return p.a();
    }
    static int porSegunda(Segunda s) {
        return s.b();
    }
    static void main() {
        var z = new Zz();
        debugPrint(porPrimera(z));
        debugPrint(porSegunda(z));
        debugPrint(z.a());
        debugPrint(z.b());
        debugPrint(z.c());
        debugPrint(porPrimera(new Ww()));
        debugPrint(porSegunda(new Ww()));
    }
}
