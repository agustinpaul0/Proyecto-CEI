// CP-SII-55: un receptor de tipo interfaz selecciona sus metodos declarados y heredados
interface Accion {
    void ejecutar();
}

interface Reaccion extends Accion {
    void responder();
}

class Tarea implements Reaccion {
    void ejecutar() {
    }

    void responder() {
    }
}

class Uso {
    Reaccion reaccion;
    Accion accion;

    void m1() {
        reaccion = new Tarea();
        accion = reaccion;
        reaccion.responder();
        reaccion.ejecutar();
        accion.ejecutar();
    }
}