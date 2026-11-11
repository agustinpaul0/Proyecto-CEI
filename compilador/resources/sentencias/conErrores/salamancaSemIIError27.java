///[Error:=|7]
// CN-SII-76: no se asigna un Object a una variable de un tipo derivado sin cast (S12.1)
class ObjectNoT {
    Raza contenido;

    void m1(Object objeto) {
        contenido = objeto;
    }
}
class Raza {
}
