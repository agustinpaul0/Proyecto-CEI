///[Error:obtener|6]
class Caja<T> {
  T obtener() { }
}
class Texto extends Caja<String> {
  Object obtener() { }
}
