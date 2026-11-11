// personas[0].edad usa Persona como receptor (§11.8)
class Persona {
    int edad;
    Persona[] hijos;
    int obtenerEdad() { return edad; }
}
class Uso {
    void m(Persona[] personas) {
        personas[0].edad = 3;
        var e = personas[1].hijos[2].obtenerEdad();
        e = personas[0].hijos.length;
        personas[0].hijos[0] = personas[1];
    }
}
