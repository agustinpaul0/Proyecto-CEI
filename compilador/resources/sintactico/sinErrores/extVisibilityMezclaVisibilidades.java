///[SinErrores]
// Mezcla de visibilidades: public, private e implicita en campos, metodos, constructores y estaticos

class Mezcla {
    public int a;
    private int b;
    int c;

    public void m1() {}
    private void m2() {}
    void m3() {}

    public Mezcla() {}
    private Mezcla(int x) {}
    Mezcla(String s) {}

    public static int s1() { return 0; }
    private static void s2() {}
}
