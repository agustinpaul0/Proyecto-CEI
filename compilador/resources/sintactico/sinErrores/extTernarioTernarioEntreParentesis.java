///[SinErrores]
// El ternario se puede parentizar en cualquier contexto, igual que en Java:
// var r = (a ? b : c), como argumento ((a ? b : c)) o como condicion de if.
//
class Prueba {
    void main() {
        var r = (a ? b : c);
        f((a ? b : c));
        if ((a ? b : c)) {
            g();
        }
    }

    void f(int n) {
    }

    void g() {
    }
}