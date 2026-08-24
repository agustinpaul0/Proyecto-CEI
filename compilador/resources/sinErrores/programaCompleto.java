class Contador implements Iterador {
    var valor = 0;
    var limite = 10;

    public void incrementar() {
        if (valor < limite) {
            valor = valor + 1;
        } else {
            valor = 0;
        }
    }

    public boolean estaLleno() {
        return valor == limite;
    }
}

//#class,1
//#Contador,1
//#implements,1
//#Iterador,1
//#{,1
//#var,2
//#valor,2
//#=,2
//#0,2
//#;,2
//#var,3
//#limite,3
//#=,3
//#10,3
//#;,3
//#public,5
//#void,5
//#incrementar,5
//#(,5
//#),5
//#{,5
//#if,6
//#(,6
//#valor,6
//#<,6
//#limite,6
//#),6
//#{,6
//#valor,7
//#=,7
//#valor,7
//#+,7
//#1,7
//#;,7
//#},8
//#else,8
//#{,8
//#valor,9
//#=,9
//#0,9
//#;,9
//#},10
//#},11
//#public,13
//#boolean,13
//#estaLleno,13
//#(,13
//#),13
//#{,13
//#return,14
//#valor,14
//#==,14
//#limite,14
//#;,14
//#},15
//#},16
//#$,75
