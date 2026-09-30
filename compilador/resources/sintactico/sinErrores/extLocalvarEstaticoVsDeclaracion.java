///[SinErrores]
// Llamadas estaticas: el lexema de clase arranca una declaracion o una llamada

class EstaticoVsDeclaracion {

    void probar() {
        Matematica m;
        Matematica a, b = Matematica.crear();
        Matematica.operar();
        int r = Matematica.sumar(1, 2);
    }
}

class Matematica {
    static Matematica crear() {
        return null;
    }

    static int sumar(int a, int b) {
        return a + b;
    }

    static void operar() {
    }
}