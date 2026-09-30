///[SinErrores]
// Prueba encabezados de metodos redefinicion valida
class A1 {
    void m3(A1 p1, B2 p2)
    {}  
}
class B2 extends A1 {
    void m3(A1 p1, B2 p2)
    {}  
}



class Init{
    static void main()
    { }
}




