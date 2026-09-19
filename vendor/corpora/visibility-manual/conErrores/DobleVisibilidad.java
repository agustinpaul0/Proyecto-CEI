///[Error:public|7]
// Doble modificador de visibilidad: solo se permite uno

class Foo {
    int x;

    public public void m() {}
}
