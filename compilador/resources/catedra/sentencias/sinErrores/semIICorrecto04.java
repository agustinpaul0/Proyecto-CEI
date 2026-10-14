// mediante la llamada a un metodo directo accede encadenadamente a una var
// Tambien la invocacion a un Ctor
class A1 {
    int a1;
    
     void m1(int p1)
    {
        p1 = m3().a1;
    }
    
     void m2()
    {}
    
     A1 m3(){
        return new A1();
    }
         
    

}




class Init{
    static void main()
    { }
}


