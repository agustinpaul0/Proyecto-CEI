// un atributo y un metodo se llaman igual sin confundirse
class Aa {
    int valor;
    int valor() { return valor; }
    void m() {
        valor = valor();
    }
}
