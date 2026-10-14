// CP-SII-19: bloques hermanos pueden declarar el mismo nombre
class Hermanos {
    void m1() {
        var acumulador = 0;
        {
            var indice = 0;
            acumulador = indice;
        }
        {
            var indice = 1;
            acumulador = indice;
        }
    }
}
