#!/usr/bin/env python3
"""Escribe el corpus del analizador sintáctico ESCRITO A MANO.

    python3 herramientas/lote_sintactico.py

Es el complemento del corpus generado por el fuzzer: éste cubre a propósito lo
que un generador al azar no busca —el caso vacío, el de un solo elemento, el
anidamiento profundo, el error en el primer y en el último token, el `$` del
EOF— y cada expectativa se PREDICE a mano antes de correr el compilador. Si al
correr no coincide, o el caso estaba mal escrito o el compilador tiene un
defecto, y las dos cosas hay que mirarlas.

`linea` es la línea DEL FUENTE, contando desde 1. El script le suma la cabecera,
que es lo que fácil se erra a mano.
"""
import sys
from pathlib import Path

RAIZ = Path(__file__).resolve().parent.parent
SIN_ERRORES = RAIZ / "resources" / "sinErrores"
CON_ERRORES = RAIZ / "resources" / "conErrores"

def correcto(nombre, fuente):
    destino = SIN_ERRORES / f"{nombre}.java"
    destino.write_text(fuente if fuente.endswith("\n") or not fuente else fuente + "\n",
                       encoding="utf-8")


def con_error(nombre, fuente, lexema, linea):
    """`linea` es la línea del fuente; la cabecera corre todo un renglón."""
    destino = CON_ERRORES / f"{nombre}.java"
    cabecera = f"///[Error:{lexema}|{linea + 1}]\n"
    destino.write_text(cabecera + (fuente if fuente.endswith("\n") else fuente + "\n"),
                       encoding="utf-8")




def casos_degenerados():
    correcto("vacioTotal", "")
    correcto("soloUnComentarioDeLinea", "// nada mas que esto\n")
    correcto("soloUnComentarioMultilinea", "/* nada\n   mas\n   que esto */\n")
    correcto("soloBlancos", "\n\n   \t\n\n")
    correcto("claseVacia", "class C1 { }\n")
    correcto("interfazVacia", "interface I1 { }\n")
    correcto("claseSinEspacios", "class C1{}\n")
    correcto("todoEnUnaSolaLinea", "class C1 { void m(){ var x = 1; } } class C2 { }\n")
    correcto("sinSaltoDeLineaFinal", "class C1 { }")
    correcto("claseYComentarioIntercalados",
             "/* uno */ class /* dos */ C1 /* tres */ { } // cuatro\n")

    # El primer token del archivo es el que falla.
    con_error("errorEnElPrimerToken", "void\n", "void", 1)
    con_error("errorEnElPrimerTokenDeUnArchivoLargo",
              "public\nclass C1 { }\nclass C2 { }\n", "public", 1)
    # El error cae en el ultimo token del archivo.
    con_error("errorEnElUltimoToken", "class C1 { } class\n", "$", 2)
    # Y el caso del EOF: el lexema es `$` (Pautas slide 26).
    con_error("claseSinCerrar", "class C1 {\n", "$", 2)
    con_error("interfazSinCerrar", "interface I1 {\n", "$", 2)
    con_error("metodoSinCerrar", "class C1 {\n  void m(){\n", "$", 3)
    con_error("soloLaPalabraClass", "class\n", "$", 2)
    con_error("archivoDeUnSoloTokenInvalido", "}\n", "}", 1)
    con_error("comentarioYLuegoError", "// un comentario\n// otro\nclass 1\n", "1", 3)


def casos_de_estructura():
    correcto("variasClasesEInterfaces",
             "class C1 { }\ninterface I1 { }\nclass C2 { }\ninterface I2 { }\n")
    correcto("herenciaConExtends", "class C1 extends C2 { }\n")
    correcto("herenciaConImplements", "class C1 implements I1 { }\n")
    correcto("interfazConExtends", "interface I1 extends I2 { }\n")
    correcto("claseGenerica", "class C1<T> { }\n")
    correcto("interfazGenerica", "interface I1<T> { }\n")
    correcto("claseGenericaQueHereda", "class C1<T> extends C2<T> { }\n")
    correcto("cienClasesSeguidas", "".join(f"class C{i} {{ }}\n" for i in range(100)))

    con_error("claseSinNombre", "class { }\n", "{", 1)
    con_error("claseConNombreDeVariable", "class c1 { }\n", "c1", 1)
    con_error("claseConNombreDeUnaSolaMayuscula", "class C { }\n", "C", 1)
    con_error("claseSinLlaveDeApertura", "class C1 }\n", "}", 1)
    con_error("dosPalabrasClass", "class class C1 { }\n", "class", 1)
    con_error("extendsSinTipo", "class C1 extends { }\n", "{", 1)
    con_error("extendsDosVeces", "class C1 extends C2 extends C3 { }\n", "extends", 1)
    con_error("implementsYExtendsJuntos", "class C1 extends C2 implements I1 { }\n",
              "implements", 1)
    con_error("interfazConImplements", "interface I1 implements I2 { }\n", "implements", 1)
    con_error("dosParametrosDeTipo", "class C1<T,U> { }\n", ",", 1)
    con_error("parametroDeTipoQueEsIdClase", "class C1<Persona> { }\n", "Persona", 1)
    con_error("genericidadSinCerrar", "class C1<T { }\n", "{", 1)
    con_error("basuraEntreClases", "class C1 { } ; class C2 { }\n", ";", 1)


def casos_de_miembros():
    correcto("cadaFormaDeMiembro", """class C1 {
  int x;
  int[] a;
  int[][] m;
  Persona p;
  T t;
  Lista<Persona> l;
  void m1(){}
  int m2(){}
  static int m3(){}
  static void m4(){}
  Persona m5(){}
  int m6(int a){}
  int m7(int a, char b, Persona c){}
  int m8(int[] a){}
  public C1(){}
  public C1(int a){}
}
""")
    correcto("interfazConCadaFormaDeMetodo", """interface I1 {
  int m1();
  void m2();
  Persona m3(int a);
  T m4();
  int[] m5(int[] a, char b);
}
""")
    correcto("visibilidadEnTodoMiembro", """class C1 {
  private int x;
  public int y;
  int z;
  private void m1(){}
  public void m2(){}
  void m3(){}
  private C1(){}
  public C1(int a){}
  C1(char c){}
}
""")
    correcto("publicEnMetodoDeInterfaz", "interface I1 { public void m1(); }\n")
    correcto("atributosInicializados", """class C1 {
  int x = 5;
  Persona p = new Persona();
  int[] a = new int[3];
  char c = 'a';
}
""")
    correcto("visibilidadYStaticEnEseOrden",
             "class C1 { public static int m1(){} private static void m2(){} }\n")
    correcto("cienMiembrosSeguidos",
             "class C1 {\n" + "".join(f"  int x{i};\n" for i in range(100)) + "}\n")

    con_error("metodoDeInterfazConCuerpo", "interface I1 { void m1(){} }\n", "{", 1)
    con_error("privateEnMetodoDeInterfaz", "interface I1 { private void m1(); }\n",
              "private", 1)
    correcto("staticAntesDeLaVisibilidad", "class C1 { static public int m1(){} }\n")
    correcto("staticDespuesDeLaVisibilidad", "class C1 { public static int m1(){} }\n")
    con_error("visibilidadRepetida", "class C1 { public private int m1(){} }\n", "private", 1)
    con_error("staticRepetido", "class C1 { static static int m1(){} }\n", "static", 1)
    con_error("visibilidadADosLadosDelStatic", "class C1 { public static private int m1(){} }\n",
              "private", 1)
    con_error("atributoSinPuntoYComa", "class C1 { int x }\n", "}", 1)
    con_error("atributoStatic", "class C1 { static int x; }\n", ";", 1)
    con_error("metodoSinCuerpoEnUnaClase", "class C1 { void m1(); }\n", ";", 1)
    con_error("constructorSinCuerpo", "class C1 { public C1(); }\n", ";", 1)
    con_error("argumentoFormalSinTipo", "class C1 { void m1(a){} }\n", "a", 1)
    con_error("argumentoFormalSinNombre", "class C1 { void m1(int){} }\n", ")", 1)
    con_error("comaSobranteEnArgsFormales", "class C1 { void m1(int a,){} }\n", ")", 1)
    con_error("comaAlPrincipioDeArgsFormales", "class C1 { void m1(,int a){} }\n", ",", 1)
    con_error("atributoConTipoVoid", "class C1 { void x; }\n", ";", 1)
    con_error("miembroQueEmpiezaConIdMetVar", "class C1 { x = 1; }\n", "x", 1)
    con_error("dosPuntosYComaEntreMiembros", "class C1 { int x;; }\n", ";", 1)


def casos_de_sentencias():
    correcto("cadaFormaDeSentencia", """class C1 {
  void m(){
    ;
    {}
    {;}
    {{{;}}}
    return;
    var x = 1;
    x = 1;
    x.y = 1;
    a[0] = 1;
    m1();
    this.m1();
    this.x = 1;
    Clase.m1();
    a.b.c();
    a.b().c();
    if (a) ;
    if (a) ; else ;
    if (a) { } else { }
    while (a) ;
    while (a) { m1(); }
  }
}
""")
    correcto("returnConExpresion", "class C1 { int m(){ return a + b * c; } }\n")
    correcto("elseColganteLigaAlIfMasCercano",
             "class C1 { void m(){ if (a) if (b) m1(); else m2(); } }\n")
    correcto("cienBloquesAnidados",
             "class C1 { void m(){ " + "{" * 100 + ";" + "}" * 100 + " } }\n")
    correcto("cienIfsAnidados",
             "class C1 { void m(){ " + "if (a) " * 100 + "; } }\n")
    correcto("variablesLocalesClasicas", """class C1 {
  void m(){
    int x;
    int y = 1;
    int a, b;
    int c, d, e = 10;
    int[] v;
    int[][] w;
    Persona p;
    Persona q = new Persona();
    Lista<Persona> l;
    T t;
  }
}
""")
    correcto("idClaseSeDesempataConElTokenSiguiente",
             "class C1 { void m(){ Persona p; Persona.crear(); } }\n")

    con_error("elseSinIf", "class C1 { void m(){ else ; } }\n", "else", 1)
    con_error("ifSinParentesis", "class C1 { void m(){ if a ; } }\n", "a", 1)
    con_error("ifSinSentencia", "class C1 { void m(){ if (a) } }\n", "}", 1)
    con_error("whileSinCondicion", "class C1 { void m(){ while () ; } }\n", ")", 1)
    con_error("sentenciaSinPuntoYComa", "class C1 { void m(){ x = 1 } }\n", "}", 1)
    con_error("varSinInicializar", "class C1 { void m(){ var x; } }\n", ";", 1)
    con_error("varConTipo", "class C1 { void m(){ var int x = 1; } }\n", "int", 1)
    con_error("bloqueSinCerrar", "class C1 { void m(){ { ; } }\n", "$", 2)
    con_error("llaveDeMasEnElBloque", "class C1 { void m(){ ; } } }\n", "}", 1)
    con_error("returnFueraDeUnMetodo", "class C1 { return; }\n", "return", 1)


def envolver(sentencias):
    return "class C1 {\n  void m(){\n    " + sentencias + "\n  }\n}\n"


def casos_de_expresiones():
    correcto("cadaOperando", envolver(
        "var a = true; var b = false; var c = 1; var d = 'x'; var e = null;"
        " var f = \"hola\"; var g = this; var h = x; var i = m1(); var j = Clase.m1();"
        " var k = new Clase(); var l = (1);"))
    correcto("cadaOperadorBinario", envolver(
        " ".join(f"var v{i} = a {op} b;" for i, op in enumerate(
            ["||", "&&", "==", "!=", "<", ">", "<=", ">=", "+", "-", "*", "/", "%"]))))
    correcto("cadaOperadorUnario", envolver("var a = +x; var b = -x; var c = !x;"))
    correcto("cadenaLarguisimaDeBinarios", envolver("var x = " + " + ".join("a" * 1 for _ in range(200)) + ";"))
    correcto("parentizadoProfundo", envolver("var x = " + "(" * 100 + "1" + ")" * 100 + ";"))
    correcto("encadenamientoLargo", envolver("var x = " + "a" + ".b" * 100 + ";"))
    correcto("accesoAArregloEncadenado", envolver("var x = a[0][1][2].b[3].c();"))
    correcto("unarioDespuesDeBinario", envolver("var x = a + -b * !c;"))
    correcto("literalesFloat", envolver(
        "var a = 1.5; var b = 1.; var c = 1e5; var d = 1.5e-3; var e = 1f; var f = 1.5D;"))
    correcto("tipoFloat", "class C1 { float x; float[] v; float m(float a){} }\n")
    correcto("ternarioSimple", envolver("var x = a ? b : c;"))
    correcto("ternarioAnidadoEnLasDosRamas", envolver(
        "var x = a ? b ? c : d : e; var y = a ? b : c ? d : e;"))
    correcto("ternarioConAsignacionEnElMedio", envolver("var x = a ? b = 1 : c;"))
    correcto("ternariosAnidadosProfundo", envolver(
        "var x = " + "a ? b : " * 50 + "c;"))
    correcto("posfijos", envolver("x++; y--; var a = x++ + y--;"))
    correcto("cadaFormaDeCreacion", envolver(
        "var a = new Clase(); var b = new Clase(1, 2); var c = new int[3];"
        " var d = new int[3][4]; var e = new Clase[3]; var f = new T[2];"))
    correcto("inicializadorDeArreglo", envolver(
        "var a = new int[]{}; var b = new int[]{1}; var c = new int[]{1,2,3};"
        " var d = new int[][]{{1,2},{3,4}}; var e = new int[][][]{{{1}}};"))
    correcto("inicializadorDeArregloConComaFinal", envolver(
        "var a = new int[]{1,2,}; var b = new int[]{,}; var c = new int[][]{{1,},{2},};"))
    correcto("inicializadorDeArregloConListasVacias", envolver(
        "var a = new int[][]{}; var b = new int[][]{{}}; var c = new int[][]{{},{}};"
        " var d = new int[][][]{{},{{}}};"))
    # Que el anidamiento coincida con las dimensiones es un chequeo de tipos: el
    # parser de javac acepta los dos y el error lo da el analisis semantico.
    correcto("inicializadorConAnidamientoDistintoDeLasDimensiones", envolver(
        "var a = new int[]{{}}; var b = new int[][]{1};"))
    correcto("genericosAnidados",
             "class C1 { Lista<Lista<Persona>> x; Lista<Lista<Lista<T>>> y; }\n")
    correcto("diamanteAlInstanciar", envolver("var x = new Lista<>();"))
    correcto("argumentosActuales", envolver("m1(); m2(1); m3(1, 2, 3); m4(m5(m6()));"))

    con_error("expresionSinOperandoDerecho", envolver("var x = a + ;"), ";", 3)
    con_error("dosOperadoresBinariosSeguidos", envolver("var x = a + * b;"), "*", 3)
    con_error("parentesisSinCerrar", envolver("var x = (a + b;"), ";", 3)
    con_error("parentesisSinAbrir", envolver("var x = a + b);"), ")", 3)
    con_error("corcheteSinCerrar", envolver("var x = a[0;"), ";", 3)
    con_error("asignacionEncadenada", envolver("a = b = c;"), "=", 3)
    con_error("ternarioSinRamaFalsa", envolver("var x = a ? b;"), ";", 3)
    con_error("ternarioSinDosPuntos", envolver("var x = a ? b c;"), "c", 3)
    con_error("asignacionEnLaRamaDerechaDelTernario", envolver("var x = a ? b : c = 1;"),
              "=", 3)
    con_error("dosPosfijosSeguidos", envolver("x++ ++;"), "++", 3)
    con_error("posfijoSobreUnLiteral", envolver("var x = 5++;"), "++", 3)
    con_error("newSinTipo", envolver("var x = new ;"), ";", 3)
    # Despues de `new Clase` tiene que venir `<`, `[` o `(`.
    con_error("newDeClaseSinNadaDetras", envolver("var x = new Persona;"), ";", 3)
    # Y despues de cerrar el generico, `[` o `(`.
    con_error("newGenericoSinNadaDetras", envolver("var x = new Lista<T>;"), ";", 3)
    # Sin inicializador, toda dimension de una creacion lleva su tamanio.
    con_error("newConArregloSinDimension", envolver("var x = new int[];"), ";", 3)
    # Con inicializador, ninguna la lleva, como en Java.
    con_error("inicializadorConDimensionConTamanio", envolver("var x = new int[3]{1,2,3};"),
              "{", 3)
    con_error("inicializadorConComasDeMas", envolver("var x = new int[]{1,,};"), ",", 3)
    con_error("inicializadorSinComaEntreListas", envolver("var x = new int[][]{{}{}};"), "{", 3)
    con_error("inicializadorSinComaEntreElementos", envolver("var x = new int[]{1 2};"), "2", 3)
    con_error("inicializadorFueraDeNew", envolver("var x = {1,2};"), "{", 3)
    con_error("diamanteEnPosicionDeTipo", "class C1 { Lista<> x; }\n", ">", 1)
    con_error("diamanteAlCrearArreglo", envolver("var x = new Lista<>[3];"), "[", 3)
    con_error("comaSobranteEnArgsActuales", envolver("m1(1,);"), ")", 3)
    con_error("puntoSinNombre", envolver("var x = a.;"), ";", 3)
    con_error("llamadaEstaticaSinArgumentos", envolver("var x = Clase.m1;"), ";", 3)


def casos_de_fors():
    correcto("cadaFormaDeFor", envolver(
        "for(;;) ; for(var i = 0; ; ) ; for(; i < 10; ) ; for(; ; i) ;"
        " for(var i = 0; i < 10; i) ; for(var e : lista) ;"))
    correcto("forConPosfijo", envolver("for(var i = 0; i < 10; i++) ;"))
    correcto("forsAnidados", envolver("for(;;) for(;;) for(;;) ;"))
    correcto("forConBloque", envolver("for(var i = 0; i < 10; i++) { m1(); m2(); }"))

    con_error("forConDosExpresionesPorSeparador", envolver("for(var i = 0, j = 0; ; ) ;"),
              ",", 3)
    con_error("forDeIteradoresConClausulas", envolver("for(var e : lista; ; ) ;"), ";", 3)
    con_error("forSinParentesis", envolver("for ;; ;"), ";", 3)
    con_error("forConUnSoloSeparador", envolver("for(;) ;"), ")", 3)
    # Despues de `for (var i` tiene que venir `:` o `=`.
    con_error("forConVarSinInicializar", envolver("for(var i;;) ;"), ";", 3)
    # La variable de control se declara con `var`: el for no toma el declarador
    # tipado de Variables Locales Clasicas.
    con_error("forConDeclaradorTipado", envolver("for(int i = 0; i < 10; i) ;"), "int", 3)
    con_error("forDeIteradoresConDeclaradorTipado", envolver("for(Persona p : lista) ;"), "p", 3)


def casos_combinados():
    correcto("todosLosLogrosJuntos", """class Registro<T> extends Base<T> {
  private Lista<Lista<T>> datos = new Lista<>();
  public int cuenta = 0;
  private float promedio = 0.5f;

  public Registro(Lista<T> iniciales){
    int i, j, total = 0;
    for(var k = 0; k < 10; k++) {
      total = k > 5 ? k : total;
    }
    for(var elemento : iniciales) {
      this.cuenta++;
    }
    var matriz = new int[][]{{1,2},{3,4}};
  }

  private Lista<T> filtrar(Lista<T> origen, int limite){
    return origen;
  }
}
""")
    correcto("programaRealistaDelNucleo", """class Pila {
  Nodo tope;
  int tamanio;

  public Pila(){
    this.tope = null;
    this.tamanio = 0;
  }

  void apilar(Objeto valor){
    var nodo = new Nodo(valor, this.tope);
    this.tope = nodo;
    this.tamanio = this.tamanio + 1;
  }

  Objeto desapilar(){
    if (this.tamanio == 0) return null;
    var valor = this.tope.valor();
    this.tope = this.tope.siguiente();
    this.tamanio = this.tamanio - 1;
    return valor;
  }

  boolean estaVacia(){ return this.tamanio == 0; }
}

interface Comparable {
  int comparar(Objeto otro);
}
""")

    con_error("errorProfundoEnUnProgramaGrande", """class C1 {
  void a(){ if (x) { while (y) { m1(); } } }
  void b(){ var v = new int[3]; v[0] = 1; }
  void c(){ if (p) { if (q) { m2(); } else { m3(; } } }
  void d(){ }
}
""", ";", 4)
    con_error("errorEnLaUltimaClaseDeVarias",
              "class C1 { }\nclass C2 { }\nclass C3 { }\nclass C4 { void m(){ x = } }\n",
              "}", 4)




def main():
    for carpeta in (SIN_ERRORES, CON_ERRORES):
        carpeta.mkdir(parents=True, exist_ok=True)
    antes = (len(list(SIN_ERRORES.glob("*.java"))), len(list(CON_ERRORES.glob("*.java"))))

    casos_degenerados()
    casos_de_estructura()
    casos_de_miembros()
    casos_de_sentencias()
    casos_de_expresiones()
    casos_de_fors()
    casos_combinados()

    despues = (len(list(SIN_ERRORES.glob("*.java"))), len(list(CON_ERRORES.glob("*.java"))))
    print(f"sinErrores: {antes[0]} -> {despues[0]}")
    print(f"conErrores: {antes[1]} -> {despues[1]}")


if __name__ == "__main__":
    sys.exit(main())
