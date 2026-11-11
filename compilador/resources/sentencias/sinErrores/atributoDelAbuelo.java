// un atributo se busca tambien en los ancestros (§10.1)
class Aa { int x; }
class Bb extends Aa { }
class Cc extends Bb {
    void m() {
        x = 1;
    }
}
