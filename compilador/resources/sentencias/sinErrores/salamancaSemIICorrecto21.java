// CP-SII-16: this.nombre selecciona el atributo con una local homonima visible
class Sombras {
    int edad;

    void m1() {
        var edad = 5;
        this.edad = edad;
    }
}
