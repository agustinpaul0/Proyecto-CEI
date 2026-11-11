// se busca local, despues parametro, despues atributo (§10.1)
class Aa {
    int x;
    boolean y;
    void m(char y) {
        var z = 'a';
        y = z;
        x = 3;
    }
}
