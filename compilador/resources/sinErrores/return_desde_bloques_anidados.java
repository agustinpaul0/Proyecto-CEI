///21&-1&7&15&3&2&1&8&exitosamente
class Init {
    static int primerMultiplo(int n, int limite) {
        var i = 1;
        while (i < limite) {
            var candidato = i * n;
            if (candidato > 20) {
                var ajuste = 0;
                return candidato + ajuste;
            }
            i = i + 1;
        }
        return -1;
    }
    static void contar(int n) {
        if (n == 0) {
            return;
        }
        var copia = n;
        debugPrint(copia);
        contar(n - 1);
    }
    static void main() {
        var antes = 7;
        debugPrint(primerMultiplo(3, 100));
        debugPrint(primerMultiplo(50, 1));
        debugPrint(antes);
        var despues = 8;
        debugPrint(antes + despues);
        contar(3);
        debugPrint(despues);
    }
}
