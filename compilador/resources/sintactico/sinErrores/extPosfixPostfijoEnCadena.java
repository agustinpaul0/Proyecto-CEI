///[SinErrores]
// Postfijo sobre acceso a campo encadenado

class Prueba {
    void main() {
        var o = new Reloj();
        o.horamina++;
        o.horamina--;
        this.minutos++;
    }
}

class Reloj {
    private int horamina;
    private int minutos;
}