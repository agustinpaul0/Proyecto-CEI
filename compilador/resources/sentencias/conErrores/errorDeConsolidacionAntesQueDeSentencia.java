///[Error:f|7]
// una redefinicion incorrecta se reporta antes que un cuerpo (Clase 10 slide 50)
class Aa {
    int f() { return true; }
}
class Bb extends Aa {
    boolean f() { return true; } // <-
}
