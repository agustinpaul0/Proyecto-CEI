///[SinErrores]
// var con expresiones y lambdas

class VarYLambda {

    void probar() {
        var w = 1;
        var solo = x -> x + 1;
        var parentizado = (x) -> x * 2;
        var varios = (a, b, c) -> a + b + c;
        var vacios = () -> 42;
        w = solo.aplicar(w);
    }
}