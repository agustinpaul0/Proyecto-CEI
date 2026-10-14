// CP-SII-31: encadenado sobre componentes de arreglos y tamanio con expresion
class Persona {
    int edad;
}

class Grupo {
    Persona[] personas;
    int[] a;

    void m1(int i, int j) {
        a = new int[(i + j)];
        a[(i * 2)] = j;
        var x = a[(j - 1)];
        personas = new Persona[5];
        personas[0].edad = 30;
        var e = personas[1].edad;
    }
}
