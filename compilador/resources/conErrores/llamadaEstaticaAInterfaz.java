///[Error:Ix|6]
// Clase.m exige una clase, no una interfaz (§10.2)
interface Ix { void f(); }
class Aa {
    void m() {
        Ix.f(); // <-
    }
}
