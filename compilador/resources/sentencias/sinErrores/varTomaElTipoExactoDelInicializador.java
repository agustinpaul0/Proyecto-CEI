// var recibe el tipo del inicializador, con argumentos y dimensiones (§12.3)
class Caja<T> { }
class Bb { }
class Aa {
    void m(Caja<String> c, Bb[][] mat) {
        var a = c;
        a = new Caja<String>();
        var b = mat;
        b = new Bb[1][2];
        var s = "x";
        s = "y";
        var i = 1 + 2;
        i = 4;
    }
}
