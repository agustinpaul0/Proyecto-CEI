///1234&2468&hola&4000&55&exitosamente
// 

class Aa{
    int a1;
    int a2;
    String a3;
    
    
      void setall(int p1){
        a1 = p1;
        a2 = a1*2;
        a3 = "hola";
    }
    
      void m1(){
        debugPrint(a1);
        debugPrint(a2);
        System.printSln(a3);
        
    }
}

class Bb extends Aa{
     int a4;
     int a5;
    
    
      void seta4(){
        a4 = 4000;
        a5 = 55;
    }
    
      void m2(){
        debugPrint(a4);
        debugPrint(a5);
    }
}


class Init{
    static void main()
    
    {
        
        var x = new Bb();
        x.seta4();
        x.setall(1234);
        x.m1();
        x.m2();
    }
}


