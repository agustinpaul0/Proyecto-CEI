///[Error:Aa|4]
interface Ia { void m(); }
interface Ib extends Ia { void n(); }
class Aa implements Ib {
  void n() { }
}
