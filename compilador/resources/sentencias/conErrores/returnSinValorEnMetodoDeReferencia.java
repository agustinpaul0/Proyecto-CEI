///[Error:return|5]
// un metodo que retorna una clase exige return e (§12.5)
class Aa {
    Aa m() {
        return; // <-
    }
}
