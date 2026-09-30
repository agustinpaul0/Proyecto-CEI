///[Error:int|6]
// Se intenta declarar mas de una variable en el inicio del for (ademas, los tipos no se admiten)

class Prueba {
    void main() {
        for (int i = 0, j = 0; i < 3; i++) ;
    }
}