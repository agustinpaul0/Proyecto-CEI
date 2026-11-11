///[Error:!|5]
// CN-SII-79: ! solo se aplica a boolean (S12.1)
class NotSobreInt {
    boolean m1(int x) {
        return !x;
    }
}
