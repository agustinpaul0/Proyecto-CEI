// CP-SII-52: una asignacion booleana puede ser condicion de if y de while
class FlujoBoolean {
    void m1(boolean bandera) {
        if (bandera = true) {
            System.println();
        }
        while (bandera = false) {
            bandera = true;
        }
    }
}