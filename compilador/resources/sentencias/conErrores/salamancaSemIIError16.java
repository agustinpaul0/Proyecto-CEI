///[Error:m1|8]
// CN-SII-66: invocacion de metodo no estatico prohibido en contexto estatico (S10.4)
class MetodoNoStatic {
    void m1() {
    }

    static void m2() {
        m1();
    }
}
