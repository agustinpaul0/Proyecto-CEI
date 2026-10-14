///[Error:m2|10]
// Tipos incompatibles en el pasaje de parametros: B no conforma con String - ln: 10
class A1 {
     int a1;
     int v1;
    

     void m1(int p1) 
    {
        m2(p1+(v1-10), new B2(), "hola!");
    }
    
     void m2(int p1, String x, B2 p2)
    {}
         
   

}

class B2{
}


class Init{
    static void main()
    { }
}


