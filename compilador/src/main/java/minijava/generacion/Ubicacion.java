package minijava.generacion;

// Dónde vive una variable en ejecución (Clase 14): un parámetro o una local, en el
// registro de activación; un atributo, en el CIR de `this`. Lo fija la resolución
// de nombres y lo usa la generación (Clase 16, slides 17 y 29).
public sealed interface Ubicacion {

    void cargar(Generador generador);

    // Con el valor a guardar en el tope de la pila, que se consume.
    void almacenar(Generador generador);

    record EnRegistroDeActivacion(int offset) implements Ubicacion {

        @Override
        public void cargar(Generador generador) {
            generador.gen("LOAD " + offset);
        }

        @Override
        public void almacenar(Generador generador) {
            generador.gen("STORE " + offset);
        }
    }

    record EnThis(int offset) implements Ubicacion {

        @Override
        public void cargar(Generador generador) {
            generador.gen("LOAD 3");
            generador.gen("LOADREF " + offset);
        }

        // STOREREF espera la referencia debajo del valor (Apunte §7).
        @Override
        public void almacenar(Generador generador) {
            generador.gen("LOAD 3");
            generador.gen("SWAP");
            generador.gen("STOREREF " + offset);
        }
    }
}
