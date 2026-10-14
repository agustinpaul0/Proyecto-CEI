///[Error:=|5]
// CN-SII-68: asginacion a la izquierda de una variable no declarada en el alcance (S12.1)
class ParenLHS {
    void m1(int x) {
        (x) = 3;
    }
}
