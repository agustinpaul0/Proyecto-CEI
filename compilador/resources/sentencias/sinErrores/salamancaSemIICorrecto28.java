// CP-SII-23: receptor expresion y encadenados llamada -> atributo
class Persona {
    int edad;

    int obtenerEdad() {
        return edad;
    }

    static Persona crear() {
        return new Persona();
    }

    static void m1() {
        var a = new Persona().obtenerEdad();
        var b = crear().obtenerEdad();
        var c = crear().edad;
        crear().edad = 5;
    }
}
