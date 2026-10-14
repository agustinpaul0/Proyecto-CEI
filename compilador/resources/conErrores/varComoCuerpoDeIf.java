///[Error:x|5]
// una declaracion no es el cuerpo directo de un if; va en el nombre, como en javac (§12.3)
class Aa {
    void m(boolean c) {
        if (c) var x = 1; // <-
    }
}
