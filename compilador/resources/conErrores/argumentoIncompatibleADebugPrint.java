///[Error:debugPrint|5]
// debugPrint exige int (§7)
class Aa {
    void m() {
        debugPrint('c'); // <-
    }
}
