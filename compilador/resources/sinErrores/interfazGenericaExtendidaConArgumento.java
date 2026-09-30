interface Ia<T> { T m(); }
interface Ib extends Ia<String> { String m(); }
class Aa implements Ib {
  String m() { }
}
