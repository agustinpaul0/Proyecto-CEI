// estaticos sin receptor, con la clase o con una expresion (§10.2)
class Aa {
    static int e() { return 1; }
    int d() { return e(); }
    static void m(Aa a) {
        var x = e();
        x = Aa.e();
        x = a.e();
        x = new Aa().e();
    }
}
