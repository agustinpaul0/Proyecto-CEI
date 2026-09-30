///[SinErrores]
// Declaraciones locales en la misma sentencia y en bloques anidados

class Anidadas {

    static Objeto crear() {
        return null;
    }

    void probar() {
        if (true) {
            int x = 3;
            Objeto o, p = new Objeto();
            if (x > 3) {
                var q = Anidadas.crear();
            } else {
                int y, z = x;
            }
        }
    }
}

class Objeto {
}