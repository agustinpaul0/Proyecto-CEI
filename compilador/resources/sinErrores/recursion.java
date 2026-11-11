///120&1&55&5050&exitosamente
class Matematica {
    int fib(int n) {
        if (n < 2) {
            return n;
        }
        return fib(n - 1) + fib(n - 2);
    }
}

class Init {
    static int factorial(int n) {
        if (n == 0) {
            return 1;
        }
        return n * factorial(n - 1);
    }
    static int sumar(int n) {
        if (n == 0) {
            return 0;
        }
        return n + sumar(n - 1);
    }
    static void main() {
        debugPrint(factorial(5));
        debugPrint(factorial(0));
        debugPrint(new Matematica().fib(10));
        debugPrint(sumar(100));
    }
}
