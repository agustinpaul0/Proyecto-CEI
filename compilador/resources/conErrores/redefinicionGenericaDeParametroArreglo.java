///[Error:guardar|6]
class Caja<T> {
  void guardar(T[] x) { }
}
class Texto extends Caja<String> {
  void guardar(String x) { }
}
