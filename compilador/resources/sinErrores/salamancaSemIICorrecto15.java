// CP-SII-10: this en una clase generica tiene tipo Clase<T>
class Caja<T> {
    T contenido;

    Caja<T> identidad() {
        return this;
    }

    void guardar(T valor) {
        this.contenido = valor;
    }
}
