// una expresion larga con parentesis y todos los operadores
class Aa {
    boolean m(int a, int b) {
        return ((a + b) * (a - b) / (a % (b + 1)) > 0) && !(a <= b) || (a >= b) && (a != 0) || (b == 0);
    }
}
