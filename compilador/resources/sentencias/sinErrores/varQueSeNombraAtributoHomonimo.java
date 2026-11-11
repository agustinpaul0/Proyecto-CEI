// en el inicializador el nombre sigue siendo el atributo (§12.3)
class Aa {
    int x;
    void m() {
        var x = x + 1;
        x = 3;
    }
}
