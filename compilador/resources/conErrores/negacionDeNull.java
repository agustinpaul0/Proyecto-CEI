///[Error:!|5]
// ! exige boolean
class Aa {
    void m(boolean b) {
        b = !null; // <-
    }
}
