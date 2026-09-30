///[SinErrores]
// El ternario puede aparecer en la condicion de if y de while, igual que en
// Java (la condicion es una expresion completa).
//
class Prueba {
    void main() {
        if (a ? b : c) {
            ejecutar();
        } else {
            while (d ? e : f) {
                parar();
            }
        }
    }

    void ejecutar() {
    }

    void parar() {
    }
}