// CP-SII-35: en contexto estatico, acceso a miembros de instancia mediante receptor explicito
class Persona {
    int edad;

    int obtenerEdad() {
        return edad;
    }

    static int consultar(Persona p) {
        return p.obtenerEdad();
    }

    static int porParametro(Persona p) {
        return p.obtenerEdad();
    }

    static int porLocal() {
        var p = new Persona();
        return porParametro(p) + p.edad;
    }
}
