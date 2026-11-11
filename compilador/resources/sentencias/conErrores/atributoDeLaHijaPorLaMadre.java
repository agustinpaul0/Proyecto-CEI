///[Error:.|7]
// un atributo de la hija no se ve por un receptor de la madre
class Aa { }
class Bb extends Aa { int x; }
class Uso {
    void m(Aa a) {
        a.x = 1; // <-
    }
}
