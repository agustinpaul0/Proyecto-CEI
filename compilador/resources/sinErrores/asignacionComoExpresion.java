// la asignacion produce el valor del destino (§11.9)
class Aa {
    int x;
    int m(int y) {
        x = (y = 3);
        var z = (x = 4);
        return x = 5;
    }
}
