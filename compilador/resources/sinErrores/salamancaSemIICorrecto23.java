// CP-SII-18: una local es visible en los bloques anidados
class Anidados {
    void m1() {
        var exterior = 1;
        {
            var interior = 2;
            exterior = exterior + interior;
        }
    }
}
