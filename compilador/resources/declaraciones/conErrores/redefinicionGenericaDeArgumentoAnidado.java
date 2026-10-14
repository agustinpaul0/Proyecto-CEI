///[Error:m|6]
class Caja<T> {
  Caja<T> m() { }
}
class Texto extends Caja<String> {
  Caja<Object> m() { }
}
