// [SinErrores]
// Postfijo sobre un literal de string ("hola"++): sobre-derivacion de la
// gramatica. Java no permite incrementar literales, pero el literal de string
// es un no-primitivo que el parser admite como destino postfijo.
//
class Prueba {
    void main() {
        var s = "hola";
        "hola"++;
        "adiós"--;
    }
}