// un atributo heredado es asignable como uno propio
class Aa { int at; }
class Bb extends Aa { }
class Cc extends Bb {
    void m(Cc c) {
        at = 1;
        c.at = 2;
        this.at = 3;
    }
}
