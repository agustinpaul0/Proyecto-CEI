///[Error:=|5]
// CN-SII-73: invariancia de arreglos: no se asigna un arreglo de un tipo a un arreglo de otro (S12.1)
class ArregloInvariante {
    void m1(int[] destino, char[] fuente) {
        destino = fuente;
    }
}
