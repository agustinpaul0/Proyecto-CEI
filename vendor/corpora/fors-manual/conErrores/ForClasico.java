// [Error:int|6]
// For clasico con declaracion tipada: el inicio del for solo admite expresiones o 'var'

class Prueba {
    void main() {
        for (int i = 0; i < 10; i = i + 1) f(i);
    }
}