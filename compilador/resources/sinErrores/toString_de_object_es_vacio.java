///[]&[]&[]&exitosamente
// sin-contraste: Object.toString devuelve el String vacio (Apunte §14); en Java, el nombre y el hash
class Aa {
}

class Init {
    static void main() {
        System.printS("[");
        System.printS(new Aa().toString());
        System.printSln("]");
        var arreglo = new int[3];
        System.printS("[");
        System.printS(arreglo.toString());
        System.printSln("]");
        System.printS("[");
        System.printS("hola".toString());
        System.printSln("]");
    }
}
