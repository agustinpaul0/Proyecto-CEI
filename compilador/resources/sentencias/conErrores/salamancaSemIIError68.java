///[Error:=|9]
// CN-SII-117: no se usa un primario parentizado como base de una asignacion (S11.4)
class Persona {
    public int edad;
}

class Principal {
    void m1(Persona p) {
        (p).edad = 5;
    }
}