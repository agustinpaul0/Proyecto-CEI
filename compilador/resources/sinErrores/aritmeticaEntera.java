// los cinco operadores aritmeticos sobre int dan int (§11.2)
class Aa {
    int m(int a, int b) {
        a = a + b;
        a = a - b;
        a = a * b;
        a = a / b;
        a = a % b;
        return a + b - a * b / a % b;
    }
}
