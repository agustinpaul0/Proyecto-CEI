// CP-SII-46: la sobrecarga por aridad atraviesa la herencia
class Base {
    int f() {
        return 1;
    }
}

class Sub extends Base {
    int f(int x) {
        return x;
    }

    void m1() {
        var a = f();
        var b = f(2);
        var c = this.f();
        var d = this.f(3);
    }
}