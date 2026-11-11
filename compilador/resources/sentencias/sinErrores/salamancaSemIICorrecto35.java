// CP-SII-30: arreglos: conformidad con Object, length y toString
class ArreglosDos {
    static void recibir(Object o) {
    }

    static void m1() {
        var datos = new int[10];
        recibir(datos);
        var largo = datos.length;
        var texto = datos.toString();
    }
}
