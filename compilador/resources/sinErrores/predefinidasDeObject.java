// debugPrint y toString de Object (§7)
class Aa {
    void m(Aa a, String s) {
        Object.debugPrint(3);
        debugPrint(4);
        s = a.toString();
        s = toString();
        s = "lit".toString();
        s = s.toString().toString();
        s = new Object().toString();
    }
}
