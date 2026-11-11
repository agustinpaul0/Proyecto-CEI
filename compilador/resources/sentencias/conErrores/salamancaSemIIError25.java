///[Error:=|5]
// CN-SII-74: invariancia de genericos: no se asigna una instancia de un tipo generico a otra instanciacion (S12.1)
class GenericoInvar {
    void m1(Caja<TipoA> destino, Caja<TipoB> fuente) {
        destino = fuente;
    }
}
class Caja<T> {
}
class TipoA {
}
class TipoB {
}
