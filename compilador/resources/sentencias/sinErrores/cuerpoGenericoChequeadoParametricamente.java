// dentro de Caja<T> los valores de tipo T conforman solo con T (§9.3)
class Caja<T> {
    T contenido;
    T obtener() { return contenido; }
    void guardar(T valor) { contenido = valor; }
    void copiar(Caja<T> otra) {
        contenido = otra.obtener();
        otra.guardar(contenido);
        var t = this.obtener();
        t = null;
        guardar(t);
        var yo = this;
        yo = otra;
    }
}
