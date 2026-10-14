///[Error:=|9]
// El lado izquierdo de la asignación es no asignable - ln: 9
class A1 {
    int a1;
    
    static void m1(int p1)
    
    {
        new A1() = 5;
       
    }
    
     void m2()
    {}
         
    

}


class B2 extends A1{
    
}


class Init{
    static void main()
    { }
}


