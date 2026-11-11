// CP-SII-41: clase que implementa una interfaz generica con argumento conforma con ella
interface Contenedor<T> {
    void guardar(T valor);
}

class Repositorio implements Contenedor<String> {
    String almacenado;

    void guardar(String valor) {
        almacenado = valor;
    }
}

class Uso {
    Contenedor<String> contenedor;

    void aceptar(Contenedor<String> c) {
    }

    void m1() {
        contenedor = new Repositorio();
        aceptar(new Repositorio());
        contenedor.guardar("x");
    }
}