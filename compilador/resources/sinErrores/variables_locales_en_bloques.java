///3&4&13&100&3&exitosamente
class Init {
    static void main() {
        var a = 1;
        var b = a + 1;
        {
            var c = b + 1;
            debugPrint(c);
            {
                var d = c + 1;
                debugPrint(d);
            }
            var e = 10;
            debugPrint(e + c);
        }
        {
            var c = 100;
            debugPrint(c);
        }
        debugPrint(a + b);
    }
}
