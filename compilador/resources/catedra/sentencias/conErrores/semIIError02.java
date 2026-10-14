///[Error:=|11]
// Tipos incompatibles incompatibles en la asignacion: B2 no conforma con A1 - ln: 11

class A1 {

    A1 v1;   
    
     void m1(B2 p1)
    
    {
        p1 = v1;
        
    }
         
    

}

class B2 extends A1 {}


class Init{
    static void main()
    { }
}


