// CP-SII-12: los parentesis agrupan valores en contextos de expresion
class Grupo {
    int m1(int a, int b, int c) {
        var total = (a + b) * c;
        return total;
    }
}
