// CP-SII-43: filas de arreglos asignables; un subarreglo solo a un destino de tipo igual
class ArreglosFilas {
    int[][] numeros;
    String[][] palabras;

    void m1() {
        numeros = new int[3][4];
        numeros[1] = new int[4];
        var filaNumeros = numeros[0];
        filaNumeros = numeros[2];
        palabras = new String[2][3];
        palabras[0] = new String[3];
        var filaPalabras = palabras[1];
        palabras[1] = filaPalabras;
        numeros[0][1] = 7;
        var celda = numeros[1][2];
    }
}