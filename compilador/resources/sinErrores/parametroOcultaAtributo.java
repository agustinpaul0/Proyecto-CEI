// un parametro oculta un atributo; this.nombre lo selecciona (§10.1)
class Aa {
    int x;
    void m(boolean x) {
        x = true;
        this.x = 3;
    }
}
