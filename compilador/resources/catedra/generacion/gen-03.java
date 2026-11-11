///33&exitosamente

class Aa{
  
  
    
      void m1(int p1){
        debugPrint(p1);
    }
    
      int m2(){
        
        return 33;
    }
}


class Init{
    static void main()

    { 
        
        var x = new Aa();
        x.m2();        
        debugPrint(x.m2());
    }
}


