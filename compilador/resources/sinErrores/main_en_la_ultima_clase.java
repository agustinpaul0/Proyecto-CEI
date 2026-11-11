///10&20&exitosamente
class Primera {
    int x;
}

class Segunda extends Primera {
    int doble() {
        return x * 2;
    }
}

interface Algo {
    void hacer();
}

class Principal {
    static void main() {
        var s = new Segunda();
        s.x = 10;
        debugPrint(s.x);
        debugPrint(s.doble());
    }
}
