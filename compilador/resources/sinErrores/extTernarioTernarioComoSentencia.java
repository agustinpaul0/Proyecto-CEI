///[SinErrores]
// Un ternario como sentencia suelta (a ? b : c;) ES aceptado por esta
// gramatica: sobre-derivacion respecto de Java, donde las sentencias de
// expresion solo admiten asignaciones, llamadas e instanciaciones, y un
// condicional suelto esta prohibido.
//
class Prueba {
    void main() {
        a ? b : c;
    }
}