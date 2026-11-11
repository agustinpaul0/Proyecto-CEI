///[Error:Aux|3]
// CN-SII-110: herencia circular A extends B, B extends A (S10.1)
class Aux extends Base {
}
class Base extends Aux {
}