// bloques hermanos pueden declarar el mismo nombre (§12.3)
class Aa {
    void m() {
        {
            var x = 1;
        }
        {
            var x = true;
        }
        if (true) {
            var x = 'c';
        } else {
            var x = "s";
        }
        while (false) {
            var x = 0;
        }
    }
}
