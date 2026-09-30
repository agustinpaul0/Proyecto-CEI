///[Error:public|6]
// Visibilidad en variable local: solo permitida en miembros de clase

class Foo {
    void m() {
        public int x;
    }
}
