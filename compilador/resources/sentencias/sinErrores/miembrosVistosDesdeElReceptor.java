// desde Caja<String>, T se ve como String en atributos, parametros y retornos (§5.1)
class Caja<T> {
    T contenido;
    T[] varios;
    T obtener() { return contenido; }
    void guardar(T valor) { contenido = valor; }
    Caja<T> mismo() { return this; }
}
class Uso {
    void m(Caja<String> c, String s) {
        s = c.contenido;
        s = c.obtener();
        c.guardar("x");
        c.guardar(s);
        c.guardar(null);
        s = c.varios[0];
        c.varios = new String[3];
        c = c.mismo();
        s = c.mismo().mismo().obtener();
        var o = c.obtener().toString();
    }
}
