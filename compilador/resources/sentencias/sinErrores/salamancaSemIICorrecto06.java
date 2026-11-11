// CP-SII-01: literales bien tipados (int, char, boolean, String) y null en contexto de referencia
class Fabrica {
    Fabrica referencia;

    int entero() { return 7; }
    char caracter() { return 'x'; }
    boolean booleano() { return false; }
    String cadena() { return "hola"; }
    Fabrica nula() { return null; }

    void m1() {
        var a = 5;
        var b = 'z';
        var c = true;
        var d = "texto";
        this.referencia = null;
        a = a;
        b = b;
        c = c;
        d = d;
    }
}
