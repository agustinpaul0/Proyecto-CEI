// CP-SII-33: conformidad con ancestro, interfaz, superinterfaz, Object y null
interface Accion {
    void ejecutar();
}

interface Reaccion extends Accion {
    void reaccionar();
}

class Base {
}

class Hija extends Base {
}

class Tarea implements Reaccion {
    void ejecutar() {
    }

    void reaccionar() {
    }
}

class Conformidad {
    Base base;
    Accion accion;

    static void tomar(Object o) {
    }

    void usar(Accion a) {
    }

    void m1(Hija h, Tarea t) {
        base = h;
        accion = t;
        usar(t);
        tomar(h);
        tomar(t);
        base = null;
        accion = null;
    }
}
