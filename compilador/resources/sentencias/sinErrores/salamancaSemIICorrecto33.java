// CP-SII-28: arreglos basicos y multidimensionales; indexacion y length
class Arreglos {
    int[] datos;
    int[][] matriz;

    void m1() {
        datos = new int[3];
        datos[0] = 5;
        var x = datos[0];
        matriz = new int[3][4];
        matriz[0][1] = 9;
        var fila = matriz[0];
        var n = matriz.length;
        var k = matriz[0].length;
        var largo = datos.length;
    }
}
