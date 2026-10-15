class Base<T> { T obtener() { } }
class MismoParametro<T> extends Base<T> { T obtener() { } }
class OtroParametro<U> extends Base<U> { U obtener() { } }
class Instanciada<T> extends Base<String> { String obtener() { } }
class NoGenerica { }
class Caja<T> extends NoGenerica { }
interface Ix<T> { }
interface Iy<U> extends Ix<U> { }
class Aa<T> implements Iy<T> { }
