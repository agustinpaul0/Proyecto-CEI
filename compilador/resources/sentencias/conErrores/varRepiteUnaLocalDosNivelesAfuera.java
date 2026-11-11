///[Error:x|9]
// la regla vale a cualquier profundidad
class Aa {
    void m() {
        var x = 1;
        {
            {
                {
                    var x = 2; // <-
                }
            }
        }
    }
}
