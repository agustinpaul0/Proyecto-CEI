///[Error:m|4]
interface Ix<T> { T m(); }
class Aa implements Ix<String> {
  Object m() { }
}
