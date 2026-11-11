// cada argumento conforma con su parametro (§11.6)
interface Ix { }
class Aa implements Ix { }
class Bb extends Aa { }
class Uso {
    void f(Aa a, Ix i, Object o, int[] v, String s) { }
    void m(Bb b) {
        f(b, b, b, null, null);
        f(new Aa(), new Bb(), new int[1], new int[2], "x");
    }
}
