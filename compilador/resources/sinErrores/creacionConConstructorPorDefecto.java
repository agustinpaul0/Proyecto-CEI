// sin constructores declarados hay uno sin parametros (§6.3)
class Aa { }
class Uso {
    void m() {
        var a = new Aa();
        var o = new Object();
        var s = new String();
        a = new Aa();
    }
}
