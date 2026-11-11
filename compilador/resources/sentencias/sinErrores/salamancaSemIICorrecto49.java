// CP-SII-44: null conforma con referencias, arreglos, interfaces y T; no con primitivas
interface Etiquetable {
}

class Persona {
    int edad;
}

class Caja<T> {
    T contenido;

    T obtener() {
        return null;
    }

    void guardar(T valor) {
        contenido = valor;
    }
}

class Uso {
    Persona persona;
    Persona[] personas;
    Etiquetable etiqueta;

    void procesar(Persona p) {
    }

    void m1() {
        persona = null;
        personas = null;
        etiqueta = null;
        procesar(null);
    }
}