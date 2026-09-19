// [Error:o|6]
// For cuyo inicio declara una variable de tipo de clase: no se admiten declaraciones tipadas

class Prueba {
    void main() {
        for (Objeto o = fabricar(); o.activo(); o.siguiente()) ;
    }
}