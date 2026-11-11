///[Error:noExiste|5]
// el metodo no esta en la clase ni en sus ancestros (§10.2)
class Aa {
    void m() {
        noExiste(); // <-
    }
}
