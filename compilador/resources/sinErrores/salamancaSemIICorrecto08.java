// CP-SII-03: operadores aritmeticos + - * / % sobre int
class Aritmetica {
    int calcular(int a, int b) {
        var suma = a + b;
        var resta = a - b;
        var producto = a * b;
        var cociente = a / b;
        var resto = a % b;
        return (suma + resta) + ((producto - cociente) + resto);
    }
}
