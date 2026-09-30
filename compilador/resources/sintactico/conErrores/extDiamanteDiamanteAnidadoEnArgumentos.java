///[Error:>|8]
// Diamante anidado dentro de un argumento de tipo (new Caja<Caja<>>()):
// Java lo permite desde la API de diamante anidado, pero esta gramatica exige
// que el argumento del diamante sea solo un tipo simple o un parametro de tipo.
//
class Prueba {
    void main() {
        var a = new Caja<Caja<>>();
    }
}

class Caja<T> {
}