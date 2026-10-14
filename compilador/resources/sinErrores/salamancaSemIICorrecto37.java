// CP-SII-32: asignaciones a componentes, atributos y conformidad polimorfica
class Base {
}

class Hija extends Base {
}

class Punto {
    int x;
}

class Persona {
    int edad;
}

class Conformidad {
    Base campo;

    void m1(Hija h) {
        var p = new Punto();
        p.x = 1;
        p.x = p.x + 1;
        var arr = new int[3];
        arr[1] = p.x;
        campo = h;
        var arr2 = new Persona[2];
        arr2[0] = new Persona();
    }
}
