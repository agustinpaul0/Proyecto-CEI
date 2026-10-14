// no se exige retorno en todos los caminos ni se rechaza codigo inalcanzable (§12.5)
class Aa {
    int sinRetorno() { }
    int parcial(boolean elegir) {
        if (elegir) return 1;
    }
    int conCodigoInalcanzable() {
        return 1;
        var numero = 2;
    }
    void dosReturns() {
        return;
        return;
    }
}
