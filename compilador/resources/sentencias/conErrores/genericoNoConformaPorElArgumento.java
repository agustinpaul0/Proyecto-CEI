///[Error:=|8]
// Caja<Bb> no conforma con Caja<Aa> aunque Bb extienda Aa (§9.3)
class Aa { }
class Bb extends Aa { }
class Caja<T> { }
class Uso {
    void m(Caja<Aa> a, Caja<Bb> b) {
        a = b; // <-
    }
}
