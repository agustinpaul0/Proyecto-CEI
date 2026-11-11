// CP-SII-40: conformidad transitiva entre clases (Especifica -> SubRaza -> Raza)
class Raza {
    int campoRaza;
}

class SubRaza extends Raza {
}

class Especifica extends SubRaza {
}

class Jerarquia {
    Raza destinoBase;
    SubRaza destinoIntermedia;

    void m1(Raza r, SubRaza s, Especifica e) {
        destinoBase = e;
        destinoIntermedia = e;
        destinoBase = s;
        var x = r.campoRaza;
        var y = s.campoRaza;
        var z = e.campoRaza;
    }
}