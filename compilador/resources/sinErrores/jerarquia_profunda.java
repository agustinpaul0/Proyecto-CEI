///1&2&3&4&4&10&exitosamente
class N1 {
    int a;
    int nivel() {
        return 1;
    }
}

class N2 extends N1 {
    int b;
    int nivel() {
        return 2;
    }
}

class N3 extends N2 {
    int c;
    int nivel() {
        return 3;
    }
}

class N4 extends N3 {
    int d;
    int nivel() {
        return 4;
    }
    int sumaDeAtributos() {
        return a + b + c + d;
    }
}

class Init {
    static int nivelDe(N1 n) {
        return n.nivel();
    }
    static void main() {
        debugPrint(nivelDe(new N1()));
        debugPrint(nivelDe(new N2()));
        debugPrint(nivelDe(new N3()));
        debugPrint(nivelDe(new N4()));
        var n = new N4();
        debugPrint(n.nivel());
        n.a = 1;
        n.b = 2;
        n.c = 3;
        n.d = 4;
        debugPrint(n.sumaDeAtributos());
    }
}
