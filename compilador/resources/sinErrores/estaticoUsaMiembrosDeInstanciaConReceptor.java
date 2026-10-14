// en un estatico se accede a lo de instancia con un receptor explicito (§10.4)
class Persona {
    int edad;
    int obtenerEdad() { return edad; }
    static int consultar(Persona persona) {
        var otra = new Persona();
        otra.edad = persona.obtenerEdad();
        return persona.obtenerEdad() + new Persona().edad + crear().edad;
    }
    static Persona crear() { return new Persona(); }
}
