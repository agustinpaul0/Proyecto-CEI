class Caja<T> {
  T contenido;
  T obtener() { }
  void guardar(T x) { }
}
class Texto extends Caja<String> {
  String obtener() { }
  void guardar(String x) { }
}
