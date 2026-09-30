///[SinErrores]
// El postfijo sobre una instanciacion es aceptado por el parser
// (la restriccion de variable se resolvira en la fase de atributos)

class Prueba {
    void main() {
        new Caja()++;
    }
}

class Caja {
}