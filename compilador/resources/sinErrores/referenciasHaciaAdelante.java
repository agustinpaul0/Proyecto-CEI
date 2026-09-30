class Aa extends Bb {
  Cc x;
  Ix m(Cc c) { }
}
class Bb { }
class Cc implements Ix {
  void n() { }
}
interface Ix { void n(); }
