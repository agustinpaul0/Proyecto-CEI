// CP-SII-45: un parametro puede ocultar un atributo
class Ocultados {
    int total;

    void acumular(int total) {
        var auxiliar = total + 1;
        this.total = auxiliar;
    }
}