///[Error:=|6]
// Object no conforma con una clase
class Aa { }
class Uso {
    void m(Aa a, Object o) {
        a = o; // <-
    }
}
