///[Error:+|5]
// CN-SII-78: caso no previsto de + (concatenacion de literales) (S12.1)
class ConcatString {
    String m1() {
        return "a" + "b";
    }
}
