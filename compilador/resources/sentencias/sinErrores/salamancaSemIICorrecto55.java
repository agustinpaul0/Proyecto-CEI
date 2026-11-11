// CP-SII-50: asignacion anidada con parentesis en el lado derecho
class Encadenadas {
    int primero;
    int segundo;

    void m1() {
        primero = (segundo = 3);
    }
}