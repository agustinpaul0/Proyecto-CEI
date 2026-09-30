interface Ix<T> {
  T m(T x);
}
class Aa implements Ix<String> {
  String m(String x) { }
}
class Bb<U> implements Ix<U> {
  U m(U x) { }
}
