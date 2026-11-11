///2&1&exitosamente
// sin-contraste: se evalua primero el lado derecho, como la Clase 16; Java evalua primero el izquierdo (consulta 4 al profesor)
class Init {
    int x;
    static Init obj() {
        debugPrint(1);
        return new Init();
    }
    static int val() {
        debugPrint(2);
        return 5;
    }
    static void main() {
        obj().x = val();
    }
}
