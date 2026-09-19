// [Error:int|6]
// For con declaracion tipada sin inicializador: el inicio del for solo admite expresiones o 'var'

class Prueba {
    void main() {
        for (int i; i < 5; i++) g(i);
    }
}