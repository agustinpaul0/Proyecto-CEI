// String conforma con Object
class Aa {
    void m(Object o, String s) {
        o = s;
        o = "literal";
        s = "otro";
    }
}
