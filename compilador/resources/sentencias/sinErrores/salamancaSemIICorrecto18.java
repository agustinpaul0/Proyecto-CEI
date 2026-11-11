// CP-SII-13: una asignacion parentizada produce un valor
class Asignaciones {
    int m1() {
        var x = 0;
        var y = (x = 3);
        return y;
    }
}
