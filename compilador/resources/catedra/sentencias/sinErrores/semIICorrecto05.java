// Prueba un lado izquierdo con dos encadenamiento y atributos heredados

class A1 {
    B2 a1;
    int a2;
   
    
    
    
} 

class B2 extends A1{
    A1 a3;
    
     void m1(B2 p1)     
    {
        a1.a3.a2 = 4;
        
    }
}


class Init{
    static void main()
    { }
}


