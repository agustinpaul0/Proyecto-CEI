///[SinErrores]
// Arreglos: creacion multidimensional, arreglo de objetos y accesos

class Arreglos {
    int[][] matriz;

    int probar() {
        var local = new int[3][4];
        var caja = new Objeto[3];
        local[0][1] = 5;
        caja[2] = null;
        matriz = local;
        return local[2][3];
    }
}

class Objeto {
}