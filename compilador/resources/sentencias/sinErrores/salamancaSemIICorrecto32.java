// CP-SII-27: creacion de objetos con argumentos, generica y como sentencia o argumento
class Motor {
    public Motor() {
    }
}

class Auto {
    Motor motor;

    public Auto(Motor m) {
        this.motor = m;
    }

    static void m1() {
        var a = new Auto(new Motor());
        new Auto(new Motor());
    }
}

class Caja<T> {
    T contenido;

    T obtener() {
        return contenido;
    }

    public Caja() {
    }

    static void m2() {
        var c = new Caja<String>();
        var s = c.obtener();
    }
}
