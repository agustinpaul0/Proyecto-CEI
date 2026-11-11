// receptores encadenados de llamadas y atributos (§11.5)
class Persona {
    int edad;
    Persona pareja;
    int obtenerEdad() { return edad; }
    Persona yo() { return this; }
    static Persona crear() { return new Persona(); }
}
class Uso {
    void m(Persona p) {
        var e = p.yo().pareja.yo().obtenerEdad();
        e = Persona.crear().edad;
        e = Persona.crear().pareja.obtenerEdad();
        p.pareja.pareja.edad = e;
        Persona.crear().yo().obtenerEdad();
    }
}
