///[SinErrores]
// Postfijo sobre el resultado de una llamada a metodo (f()++): tambien una
// sobre-derivacion, porque el operador postfijo se aplica a toda la expresion
// no-primitiva y no solo a variables; Java no lo admite.
//
class Prueba {
    void main() {
        var x = contar();
        contar()++;
        this.incrementos()--;
        aux(x, 1)++;
    }

    int contar() {
        return 0;
    }

    int incrementos() {
        return 0;
    }

    int aux(int a, int b) {
        return a;
    }
}