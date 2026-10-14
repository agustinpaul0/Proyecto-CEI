interface Ia { void m(); }
interface Ib extends Ia {
  void n();
  void m();
}
class Aa implements Ib {
  void m() { }
  void n() { }
}
