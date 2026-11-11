///[Error:m|9]
// CN-SII-116: descendiente no puede redefinir un metodo estatico del ancestro (S10.4)
class Ancestro {
    static void m() {
    }
}

class Descendiente extends Ancestro {
    void m() {
    }
}