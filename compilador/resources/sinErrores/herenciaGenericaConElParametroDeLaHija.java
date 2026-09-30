class Caja<T> {
  T obtener() { }
}
class Otra<U> extends Caja<U> {
  U obtener() { }
}
class Final extends Otra<Caja<String>> {
  Caja<String> obtener() { }
}
