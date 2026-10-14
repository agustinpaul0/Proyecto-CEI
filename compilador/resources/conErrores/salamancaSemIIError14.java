///[Error:edad|7]
// CN-SII-64: acceso implicito a un atributo prohibido en contexto estatico (S10.1/S10.4)
class AttrStatic {
    int edad;

    static void m1() {
        edad = 5;
    }
}
