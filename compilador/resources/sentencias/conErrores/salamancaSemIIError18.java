///[Error:=|5]
// CN-SII-67: la parte izquierda de una asignacion no debe ser un primitivo ni una expresion de operacion (S12.1)
class OperacionLHS {
    void m1(int a, int b, int c) {
        (a + b) = c;
    }
}
