// el bloque exterior dispone de los parametros desde el comienzo (§12.6)
class Aa {
    void m(int a, int b) {
        {
            {
                a = b;
            }
        }
    }
}
