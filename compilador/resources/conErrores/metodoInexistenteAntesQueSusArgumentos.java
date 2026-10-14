///[Error:noExiste|5]
// la llamada se resuelve antes de chequear sus argumentos (§11.6)
class Aa {
    void m() {
        noExiste(zz); // <-
    }
}
