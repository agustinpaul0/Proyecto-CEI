// cada literal tiene su tipo (§11.1)
class Aa {
    void m(int i, char c, boolean b, String s, Aa o) {
        i = 42;
        c = 'z';
        b = true;
        b = false;
        s = "hola";
        o = null;
    }
}
