// [SinErrores]
// Postfijo sobre this (this++): sobre-derivacion de la gramatica. Java no
// permite modificar this, pero el parser trata a this como un destino valido;
// la restriccion quedaria para el analisis semantico (igual que new Caja()++).
//
class Prueba {
    void main() {
        this++;
        this--;
    }
}