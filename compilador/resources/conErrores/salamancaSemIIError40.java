///[Error:1|5]
// CN-SII-89: solo las expresiones con valor pueden acompanar a return (S12.1)
class ReturnConExprVoid {
    void m1() {
        return 1;
    }
}
