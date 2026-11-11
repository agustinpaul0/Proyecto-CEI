///0&0&3&0&exitosamente
class Init {
    static void main() {
        var vacio = new int[0];
        debugPrint(vacio.length);
        var sinFilas = new int[0][5];
        debugPrint(sinFilas.length);
        var filasVacias = new int[3][0];
        debugPrint(filasVacias.length);
        debugPrint(filasVacias[2].length);
    }
}
