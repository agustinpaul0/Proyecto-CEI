///true&1&2&hola&chau&2&exitosamente
class Aa {
    int quien() {
        return 1;
    }
}

class Bb extends Aa {
    int quien() {
        return 2;
    }
}

class Init {
    static void main() {
        var objetos = new Aa[2];
        System.printBln(objetos[1] == null);
        objetos[0] = new Aa();
        objetos[1] = new Bb();
        debugPrint(objetos[0].quien());
        debugPrint(objetos[1].quien());
        var textos = new String[2];
        textos[0] = "hola";
        textos[1] = "chau";
        System.printSln(textos[0]);
        System.printSln(textos[1]);
        var matriz = new Aa[2][3];
        matriz[1][2] = new Bb();
        debugPrint(matriz[1][2].quien());
    }
}
