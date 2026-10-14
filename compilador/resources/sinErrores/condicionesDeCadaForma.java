// la condicion puede ser una llamada, un atributo o una asignacion boolean (§12.4)
class Aa {
    boolean bandera;
    boolean f() { return true; }
    void m(boolean b) {
        if (f()) ;
        if (bandera) ;
        if (this.bandera) ;
        if (b = false) ;
        while (b = f()) ;
        if (!f() || bandera && (b == true)) ;
        if ((b)) ;
    }
}
