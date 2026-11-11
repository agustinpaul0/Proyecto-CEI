// CP-SII-38: variantes de return; no se exige retorno total ni se rechaza codigo inalcanzable
class Retornos {
    public Retornos() {
        return;
    }

    void vacio() {
    }

    void soloReturn() {
        return;
    }

    int sinRetornoDeclarado() {
    }

    int parcial(boolean elegir) {
        if (elegir) {
            return 1;
        }
    }

    int conInalcanzable() {
        return 1;
        var numero = 2;
    }
}
