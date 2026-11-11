///[Error:x|8]
// al cerrar el bloque la local deja de ser visible (§12.6)
class Aa {
    void m() {
        {
            var x = 1;
        }
        x = 2; // <-
    }
}
