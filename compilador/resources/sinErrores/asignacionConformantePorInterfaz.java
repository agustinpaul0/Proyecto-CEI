// una clase conforma con su interfaz y las ancestras de esta (§9.2)
interface Ia { }
interface Ib extends Ia { }
class Cc implements Ib { }
class Dd extends Cc { }
class Uso {
    void m(Ia a, Ib b, Cc c, Dd d, Object o) {
        b = c;
        a = c;
        a = b;
        a = d;
        o = b;
    }
}
