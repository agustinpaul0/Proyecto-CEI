///[Error:.|5]
// CN-SII-95: acceso a un atributo no declarado en la clase del receptor (S11.1)
class AtributoNoExiste {
    void m1(Persona p) {
        var v = p.nombre;
    }
}
class Persona {
}
