///[Error:yy|5]
// los dos lados se chequean antes que la asignabilidad (Clase 11, slides 26, 31 y 36)
class Aa {
    void m() {
        this = yy; // <-
    }
}
