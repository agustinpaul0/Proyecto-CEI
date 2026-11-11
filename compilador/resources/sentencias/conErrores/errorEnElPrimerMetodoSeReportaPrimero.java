///[Error:zz|5]
// el primer error corta el analisis: el del primer metodo
class Aa {
    void m() {
        zz = 1; // <-
    }
    void n() {
        yy = 2;
    }
}
