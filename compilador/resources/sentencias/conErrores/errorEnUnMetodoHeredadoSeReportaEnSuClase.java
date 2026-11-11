///[Error:zz|5]
// un cuerpo heredado se chequea una sola vez, en su clase (§14)
class Base {
    void f() {
        zz = 1; // <-
    }
}
class Hija extends Base { }
