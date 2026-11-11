// CP-SII-39: == y != entre referencias del mismo tipo estatico y entre arreglos
class ReferenciasIgualdad {
    String s1;
    String s2;
    int[] a1;
    int[] a2;

    boolean m1() {
        var r1 = s1 == s2;
        var r2 = a1 != a2;
        var r3 = s1 == null;
        var r4 = a2 != null;
        return (r1 || r2) || (r3 || r4);
    }
}