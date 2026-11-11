// this vale en constructores y metodos de instancia (§11.3)
class Aa {
    Aa otro;
    Aa() { otro = this; }
    Aa yo() { return this; }
    void m() {
        var a = this;
        a = (this);
        this.otro = this.yo();
    }
}
