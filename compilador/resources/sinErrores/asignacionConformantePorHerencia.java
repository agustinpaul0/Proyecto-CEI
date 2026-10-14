// una subclase conforma con sus ancestros y con Object (§9.2)
class Aa { }
class Bb extends Aa { }
class Cc extends Bb { }
class Uso {
    void m(Aa a, Bb b, Cc c, Object o) {
        a = b;
        a = c;
        b = c;
        o = c;
        o = a;
    }
}
