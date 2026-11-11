///55&0&12&3&exitosamente
class Init {
    static void main() {
        var i = 1;
        var suma = 0;
        while (i <= 10) {
            suma = suma + i;
            i = i + 1;
        }
        debugPrint(suma);
        var nunca = false;
        while (nunca) debugPrint(99);
        var vueltas = 0;
        while (vueltas < 0) vueltas = vueltas + 1;
        debugPrint(vueltas);
        var filas = 0;
        var total = 0;
        while (filas < 3) {
            var columnas = 0;
            while (columnas < 4) {
                total = total + 1;
                columnas = columnas + 1;
            }
            filas = filas + 1;
        }
        debugPrint(total);
        debugPrint(filas);
    }
}
