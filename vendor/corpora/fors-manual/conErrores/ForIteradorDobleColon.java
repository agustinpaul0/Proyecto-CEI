// [Error::|6]
// Dos separadores ':' en el for de iteradores: solo se admite uno

class Prueba {
    void main() {
        for (var x : a : b) ;
    }
}