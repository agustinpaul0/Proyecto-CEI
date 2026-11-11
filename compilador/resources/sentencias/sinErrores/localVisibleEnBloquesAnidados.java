// una local es visible en los bloques anidados (§12.6)
class Aa {
    void m() {
        var x = 1;
        {
            {
                x = 2;
                var y = x;
            }
        }
    }
}
