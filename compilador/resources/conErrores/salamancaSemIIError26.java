///[Error:contenido|7]
// CN-SII-75: un tipo base no conforma con un tipo derivado (la herencia es hacia arriba) (S13.1)
class TNoObjectReturn {
    Object contenido;

    Coleccion m1() {
        return contenido;
    }
}
class Coleccion {
}
