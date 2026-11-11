// CP-SII-25: un metodo estatico puede seleccionarse mediante un receptor expresion
class Persona {
    int edad;

    static int consultar(Persona p) {
        return p.edad;
    }

    void m1() {
        var r = Persona.consultar(this);
        var s = this.consultar(this);
    }
}
