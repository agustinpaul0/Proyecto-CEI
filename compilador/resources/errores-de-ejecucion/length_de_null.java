///Desreferenciameinto de puntero nulo
class Aa {
    int[] datos;
}
class Init {
    static void main() {
        debugPrint(new Aa().datos.length);
    }
}
