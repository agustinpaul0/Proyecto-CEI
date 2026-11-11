///texto&texto&exitosamente
interface Describible {
    String toString();
}

class Cosa implements Describible {
    String toString() {
        return "texto";
    }
}

class Init {
    static void main() {
        var c = new Cosa();
        System.printSln(c.toString());
        System.printSln(describir(c));
    }
    static String describir(Describible d) {
        return d.toString();
    }
}
