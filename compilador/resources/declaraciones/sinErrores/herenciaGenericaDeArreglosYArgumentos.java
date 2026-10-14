class Caja<T> {
  T[] varios() { }
  Caja<T> copia() { }
}
class Texto extends Caja<String> {
  String[] varios() { }
  Caja<String> copia() { }
}
