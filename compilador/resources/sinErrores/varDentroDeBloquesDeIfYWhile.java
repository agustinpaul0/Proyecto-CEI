// con llaves, el cuerpo de un if o un while puede declarar (§12.3)
class Aa {
    void m(boolean c) {
        if (c) { var x = 1; }
        if (c) { var x = 1; } else { var y = 2; }
        while (c) { var x = 1; c = false; }
    }
}
