// al cerrar un bloque sus locales dejan de ser visibles (§12.6)
class Aa {
    void m() {
        {
            var x = 1;
        }
        var x = true;
        x = false;
    }
}
