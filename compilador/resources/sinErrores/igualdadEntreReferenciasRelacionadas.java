// == en las dos direcciones de la conformidad (§11.2)
class Aa { }
class Bb extends Aa { }
interface Ix { }
class Cc implements Ix { }
class Uso {
    void m(Aa a, Bb b, Ix i, Cc c, Object o, String s, boolean r) {
        r = a == b;
        r = b == a;
        r = i != c;
        r = o == s;
        r = s == o;
        r = a == a;
    }
}
