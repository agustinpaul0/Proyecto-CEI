///[SinErrores]
// For de iteradores cuya expresion iterada es una llamada estatica

class Prueba {
    void main() {
        for (var o : Fabrica.todos()) ;
    }
}