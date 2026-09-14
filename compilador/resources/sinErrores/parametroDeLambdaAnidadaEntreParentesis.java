///[SinErrores]
class C1 {
  void m(){
    var f = x -> (y) -> y;
    var g = x -> (y, z) -> y + z;
    var h = x -> ((y) -> y);
  }
}
