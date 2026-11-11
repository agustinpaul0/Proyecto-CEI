///[Error:[|5]
// una referencia de tipo Object no se indexa (§9.4)
class Aa {
    void m(Object o) {
        var x = o[0]; // <-
    }
}
