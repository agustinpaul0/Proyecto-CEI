// CP-SII-07: igualdad entre tipos conformantes en alguna direccion
class Base {
}

class Hija extends Base {
}

class Compatibles {
    boolean m1(Hija h, Base b) {
        return h == b;
    }

    boolean m2(Base b, Hija h) {
        return b != h;
    }
}
