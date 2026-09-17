///[SinErrores]
// Declaraciones locales como cuerpo de un if sin llaves (como en Java)

class Foo {

    void probar(boolean b) {
        int valor = 1;
        if (b) int x = valor;
        while (b) int y = valor;
    }
}