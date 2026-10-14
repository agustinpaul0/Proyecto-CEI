// un componente de Figura[] recibe una clase que implementa Figura
interface Figura { }
class Cuadrado implements Figura { }
class Uso {
    void m() {
        var figuras = new Figura[2];
        figuras[0] = new Cuadrado();
        figuras[1] = null;
    }
}
