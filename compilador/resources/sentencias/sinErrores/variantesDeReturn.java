// return sin valor en void y constructores, con valor conformante en los demas (§12.5)
class Aa { }
class Bb extends Aa { }
class Uso {
    Uso() { return; }
    void v() { return; }
    int i() { return 1; }
    boolean b() { return 1 < 2; }
    Aa a() { return new Bb(); }
    Aa n() { return null; }
    Object o() { return "s"; }
    int[] arr() { return new int[2]; }
    Object arrComoObject() { return new int[2]; }
    String s() { return "x"; }
    int asignacion(int y) { return y = 4; }
}
