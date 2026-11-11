///false&true&exitosamente
// sin-contraste: dos literales iguales son objetos distintos; en Java, el mismo (consulta 5 al profesor)
class Init {
    static void main() {
        System.printBln("hola" == "hola");
        var s = "hola";
        System.printBln(s == s);
    }
}
