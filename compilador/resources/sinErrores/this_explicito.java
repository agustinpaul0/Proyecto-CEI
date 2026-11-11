///6&6&true&exitosamente
class Aa {
    int x;
    Aa yoMismo() {
        return this;
    }
    int doble() {
        return this.x * 2;
    }
}

class Init {
    static void main() {
        var a = new Aa();
        a.x = 3;
        debugPrint(a.doble());
        debugPrint(a.yoMismo().doble());
        System.printBln(a.yoMismo() == a);
    }
}
