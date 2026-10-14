///[Error:.|5]
// CN-SII-101: prohibida una sentencia formada por el acceso a un atributo (S9)
class SentAtributo {
    void m1(Persona persona) {
        persona.edad;
    }
}
class Persona {
    int edad;
}
