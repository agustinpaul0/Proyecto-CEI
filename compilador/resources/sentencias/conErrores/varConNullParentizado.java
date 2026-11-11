///[Error:x|5]
// los parentesis conservan el tipo de null
class Aa {
    void m() {
        var x = (null); // <-
    }
}
