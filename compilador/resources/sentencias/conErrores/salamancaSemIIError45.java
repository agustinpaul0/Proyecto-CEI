///[Error:.|5]
// CN-SII-94: invocacion a un metodo no declarado en la clase del receptor (S11.1)
class MetodoNoExisteReceptor {
    void m1(Persona p) {
        p.hacer();
    }
}
class Persona {
}
