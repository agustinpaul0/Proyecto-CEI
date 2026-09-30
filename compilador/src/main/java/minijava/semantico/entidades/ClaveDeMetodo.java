package minijava.semantico.entidades;

// La identidad de un método en MiniJava: el nombre y la aridad (documento de
// semántica §2 y §6.2). `m(int)` y `m(char)` tienen la misma clave.
public record ClaveDeMetodo(String nombre, int aridad) {

    @Override
    public String toString() {
        return nombre + "/" + aridad;
    }
}
