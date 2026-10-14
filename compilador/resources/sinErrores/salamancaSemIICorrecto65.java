// CN-SII-P065: toString heredado de Object y accesible sobre un arreglo (S9.4)
class Persona {
    void m1() {
        var a = this.toString();
        var b = toString();
    }

    void m2(int[] arreglo) {
        var c = arreglo.toString();
    }
}