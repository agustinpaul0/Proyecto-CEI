// CP-SII-37: sentencias vacias, bloques, if/else y while; var requiere bloque
class Flujo {
    void m1(boolean c) {
        ;
        {
            ;
        }
        if (c) {
            var x = 1;
            x = x + 1;
        } else {
            System.println();
        }
        while (c) {
            var y = true;
            c = y;
        }
        if (c) {
            System.println();
        } else {
            System.println();
        }
    }
}
