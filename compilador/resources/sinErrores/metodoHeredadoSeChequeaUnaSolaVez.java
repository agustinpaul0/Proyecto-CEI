// el cuerpo heredado se chequea en su clase de origen (§14, Clase 10 slide 51)
class Base {
    int x;
    int f() { return x; }
}
class Hija extends Base { }
class Nieta extends Hija {
    int g() { return f(); }
}
