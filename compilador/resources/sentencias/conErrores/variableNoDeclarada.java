///[Error:zz|5]
// el nombre no es local, ni parametro, ni atributo (§10.1)
class Aa {
    void m() {
        zz = 1; // <-
    }
}
