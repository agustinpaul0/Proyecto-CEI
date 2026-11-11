///[Error:1|5]
// CN-SII-90: prohibido return acompanado de expresion en un constructor (S12.2)
class RetCtor {
    RetCtor() {
        return 1;
    }
}
