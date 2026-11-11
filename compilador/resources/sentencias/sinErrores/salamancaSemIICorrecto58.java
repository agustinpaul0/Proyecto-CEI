// CP-SII-53: el tipo base de un arreglo puede ser una interfaz
interface Figura {
    void dibujar();
}

class Circulo implements Figura {
    void dibujar() {
    }
}

class Uso {
    Figura[] figuras;

    void m1() {
        figuras = new Figura[5];
        figuras[0] = new Circulo();
        var primera = figuras[0];
        primera.dibujar();
    }
}