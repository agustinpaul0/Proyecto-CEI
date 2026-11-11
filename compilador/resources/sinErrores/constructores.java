///0&0&5&0&5&7&7&12&99&exitosamente
class Punto {
    int x;
    int y;
    Punto() {
    }
    Punto(int v) {
        x = v;
    }
    Punto(int a, int b) {
        x = a;
        y = b;
        if (a > 100) {
            return;
        }
        y = b + 1;
    }
    int suma() {
        return x + y;
    }
}

class Contador {
    int valor;
    Contador(int inicial) {
        valor = inicial;
        incrementar();
    }
    void incrementar() {
        valor = valor + 1;
    }
}

class Init {
    static void main() {
        var p = new Punto();
        debugPrint(p.x);
        debugPrint(p.y);
        var q = new Punto(5);
        debugPrint(q.x);
        debugPrint(q.y);
        var r = new Punto(5, 6);
        debugPrint(r.x);
        debugPrint(r.y);
        debugPrint(new Punto(3, 3).y + 3);
        debugPrint(r.suma());
        debugPrint(new Contador(98).valor);
    }
}
