// el ultimo acceso decide: m().a y new Aa().a son asignables (§11.5)
class Aa {
    int at;
    Aa crear() { return new Aa(); }
    void m() {
        crear().at = 1;
        new Aa().at = 2;
        this.crear().crear().at = 3;
    }
}
