// extends Base sin argumento conserva la T de Base, y se recorre como Base<T> (§5 y §9.3)
class Base<T> {
    void recibir(Base<T> otra) { }
    boolean igual(Base<T> otra) { return this == otra; }
}
class Derivada extends Base {
    void m() {
        recibir(this);
        var b = igual(new Derivada());
    }
}
class Nieta extends Derivada {
    void n() {
        recibir(new Nieta());
        var b = igual(this);
    }
}
