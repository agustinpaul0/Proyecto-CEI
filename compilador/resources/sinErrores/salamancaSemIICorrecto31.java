// CP-SII-26: entidades predefinidas System, Object y String
class Consola {
    String etiquetar() {
        var s1 = this.toString();
        var s2 = "hola".toString();
        var s3 = new Consola().toString();
        return s1;
    }

    static void m1() {
        System.printI(1);
        System.printIln(2);
        System.printC('a');
        System.printCln('b');
        System.printB(true);
        System.printBln(false);
        System.printS("x");
        System.printSln("y");
        System.println();
        var leido = System.read();
        Object.debugPrint(leido);
    }
}
