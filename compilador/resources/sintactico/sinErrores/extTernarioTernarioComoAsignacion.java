///[SinErrores]
// El ternario como lado derecho de una asignacion (x = a ? b : c), tanto
// simple como con un segundo ternario en la rama falsa.
//
class Prueba {
    void main() {
        var x = 0;
        var y = 0;
        x = a ? b : c;
        y = a ? b : c ? d : e;
        x = c ? a : d;
    }
}