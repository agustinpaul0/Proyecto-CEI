// no hay llamada implicita al constructor de la superclase (§6.3)
class Base {
    Base(int valor) { }
}
class Derivada extends Base {
    Derivada() { }
}
class Uso {
    void m() {
        var d = new Derivada();
    }
}
