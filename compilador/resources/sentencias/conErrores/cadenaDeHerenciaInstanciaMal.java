///[Error:=|7]
// Final extends Intermedia<String>: el dato heredado es String
class Base<T> { T dato; }
class Intermedia<U> extends Base<U> { }
class Final extends Intermedia<String> {
    void m() {
        dato = 3; // <-
    }
}
