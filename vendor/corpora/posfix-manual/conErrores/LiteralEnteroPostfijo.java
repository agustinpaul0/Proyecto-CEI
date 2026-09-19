// [Error:++|8]
// Postfijo sobre un literal entero (5++): Java tampoco lo permite (el operando
// de ++ debe ser una variable); aqui el rechazo viene de que los literales
// primitivos no admiten el operador postfijo.
//
class Prueba {
    void main() {
        5++;
    }
}