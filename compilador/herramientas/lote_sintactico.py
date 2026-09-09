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
SIN_ERRORES = RAIZ / "resources" / "sintactico" / "sinErrores"
CON_ERRORES = RAIZ / "resources" / "sintactico" / "conErrores"

# Los logros que hace falta declarar cuando el caso usa su sintaxis.
requisitos = {}


def correcto(nombre, fuente, logros=()):
    destino = SIN_ERRORES / f"{nombre}.java"
    destino.write_text(fuente if fuente.endswith("\n") or not fuente else fuente + "\n",
                       encoding="utf-8")
    if logros:
        requisitos[f"sinErrores/{destino.name}"] = logros


def con_error(nombre, fuente, lexema, linea, logros=()):
    """`linea` es la línea del fuente; la cabecera corre todo un renglón."""
    destino = CON_ERRORES / f"{nombre}.java"
    cabecera = f"///[Error:{lexema}|{linea + 1}]\n"
    destino.write_text(cabecera + (fuente if fuente.endswith("\n") else fuente + "\n"),
                       encoding="utf-8")
    if logros:
        requisitos[f"conErrores/{destino.name}"] = logros


TODOS = ("visibilidad", "atrinit", "varclasica", "for", "ternario", "posfijo",
         "lambda", "arrinit", "generico", "float")


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
""", ("visibilidad",))
    correcto("publicEnMetodoDeInterfaz", "interface I1 { public void m1(); }\n",
             ("visibilidad",))
    correcto("atributosInicializados", """class C1 {
  int x = 5;
  Persona p = new Persona();
  int[] a = new int[3];
  char c = 'a';
}
""", ("atrinit",))
    correcto("visibilidadYStaticEnEseOrden",
             "class C1 { public static int m1(){} private static void m2(){} }\n",
             ("visibilidad",))
    correcto("cienMiembrosSeguidos",
             "class C1 {\n" + "".join(f"  int x{i};\n" for i in range(100)) + "}\n")

    con_error("metodoDeInterfazConCuerpo", "interface I1 { void m1(){} }\n", "{", 1)
    con_error("privateEnMetodoDeInterfaz", "interface I1 { private void m1(); }\n",
              "private", 1, ("visibilidad",))
    con_error("staticAntesDeLaVisibilidad", "class C1 { static public int m1(){} }\n",
              "public", 1, ("visibilidad",))
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
""", ("varclasica",))
    correcto("idClaseSeDesempataConElTokenSiguiente",
             "class C1 { void m(){ Persona p; Persona.crear(); } }\n", ("varclasica",))

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
        "var a = 1.5; var b = 1.; var c = 1e5; var d = 1.5e-3; var e = 1f; var f = 1.5D;"),
        ("float",))
    correcto("tipoFloat", "class C1 { float x; float[] v; float m(float a){} }\n", ("float",))
    correcto("ternarioSimple", envolver("var x = a ? b : c;"), ("ternario",))
    correcto("ternarioAnidadoEnLasDosRamas", envolver(
        "var x = a ? b ? c : d : e; var y = a ? b : c ? d : e;"), ("ternario",))
    correcto("ternarioConAsignacionEnElMedio", envolver("var x = a ? b = 1 : c;"), ("ternario",))
    correcto("ternariosAnidadosProfundo", envolver(
        "var x = " + "a ? b : " * 50 + "c;"), ("ternario",))
    correcto("posfijos", envolver("x++; y--; var a = x++ + y--;"), ("posfijo",))
    correcto("cadaFormaDeCreacion", envolver(
        "var a = new Clase(); var b = new Clase(1, 2); var c = new int[3];"
        " var d = new int[3][4]; var e = new Clase[3]; var f = new T[2];"))
    correcto("inicializadorDeArreglo", envolver(
        "var a = new int[]{}; var b = new int[]{1}; var c = new int[]{1,2,3};"
        " var d = new int[][]{{1,2},{3,4}}; var e = new int[][][]{{{1}}};"), ("arrinit",))
    correcto("genericosAnidados",
             "class C1 { Lista<Lista<Persona>> x; Lista<Lista<Lista<T>>> y; }\n",
             ("generico",))
    correcto("diamanteAlInstanciar", envolver("var x = new Lista<>();"), ("generico",))
    correcto("argumentosActuales", envolver("m1(); m2(1); m3(1, 2, 3); m4(m5(m6()));"))

    con_error("expresionSinOperandoDerecho", envolver("var x = a + ;"), ";", 3)
    con_error("dosOperadoresBinariosSeguidos", envolver("var x = a + * b;"), "*", 3)
    con_error("parentesisSinCerrar", envolver("var x = (a + b;"), ";", 3)
    con_error("parentesisSinAbrir", envolver("var x = a + b);"), ")", 3)
    con_error("corcheteSinCerrar", envolver("var x = a[0;"), ";", 3)
    con_error("asignacionEncadenada", envolver("a = b = c;"), "=", 3)
    con_error("ternarioSinRamaFalsa", envolver("var x = a ? b;"), ";", 3, ("ternario",))
    con_error("ternarioSinDosPuntos", envolver("var x = a ? b c;"), "c", 3, ("ternario",))
    con_error("asignacionEnLaRamaDerechaDelTernario", envolver("var x = a ? b : c = 1;"),
              "=", 3, ("ternario",))
    con_error("dosPosfijosSeguidos", envolver("x++ ++;"), "++", 3, ("posfijo",))
    con_error("posfijoSobreUnLiteral", envolver("var x = 5++;"), "++", 3, ("posfijo",))
    con_error("newSinTipo", envolver("var x = new ;"), ";", 3)
    # Despues de `new Clase` tiene que venir `<`, `[` o `(`.
    con_error("newDeClaseSinNadaDetras", envolver("var x = new Persona;"), ";", 3)
    # Y despues de cerrar el generico, `[` o `(`.
    con_error("newGenericoSinNadaDetras", envolver("var x = new Lista<T>;"), ";", 3,
              ("generico",))
    # Con el logro de inicializacion de arreglos prendido, `new int[]` no es un
    # error de dimension: es la forma que pide su `{...}` a continuacion.
    con_error("newConArregloSinDimension", envolver("var x = new int[];"), ";", 3,
              ("arrinit",))
    con_error("inicializadorFueraDeNew", envolver("var x = {1,2};"), "{", 3, ("arrinit",))
    con_error("diamanteEnPosicionDeTipo", "class C1 { Lista<> x; }\n", ">", 1, ("generico",))
    con_error("diamanteAlCrearArreglo", envolver("var x = new Lista<>[3];"), "[", 3,
              ("generico",))
    con_error("comaSobranteEnArgsActuales", envolver("m1(1,);"), ")", 3)
    con_error("puntoSinNombre", envolver("var x = a.;"), ";", 3)
    con_error("llamadaEstaticaSinArgumentos", envolver("var x = Clase.m1;"), ";", 3)


def casos_de_lambdas_y_fors():
    correcto("cadaFormaDeLambda", envolver(
        "var a = () -> 1; var b = x -> x; var c = (x) -> x; var d = (x, y) -> x;"
        " var e = (x, y, z) -> z;"), ("lambda",))
    correcto("lambdaConMuchosParametros", envolver(
        "var f = (" + ", ".join(f"p{i}" for i in range(50)) + ") -> p0;"), ("lambda",))
    correcto("lambdaAnidada", envolver("var f = x -> y -> y;"), ("lambda",))
    correcto("lambdaConCuerpoCompuesto", envolver("var f = x -> x + x * x;"), ("lambda",))
    correcto("parentizadaSigueSiendoParentizada", envolver("var x = (a) + 1;"), ("lambda",))
    correcto("lambdaQueLlamaUnMetodoDelParametro", envolver("var f = x -> x.metodo();"),
             ("lambda",))
    correcto("lambdaQueLlamaUnMetodoEstatico", envolver("var f = x -> Clase.metodo(x);"),
             ("lambda",))
    # La llamada sin punto queda para el semantico: podria ser un metodo static.
    correcto("lambdaConLlamadaSinPunto", envolver("var f = x -> metodo(x);"), ("lambda",))
    correcto("lambdaComoArgumento", envolver("m1(x -> x, () -> 1);"), ("lambda",))
    correcto("cadaFormaDeFor", envolver(
        "for(;;) ; for(var i = 0; ; ) ; for(; i < 10; ) ; for(; ; i) ;"
        " for(var i = 0; i < 10; i) ; for(var e : lista) ;"), ("for",))
    correcto("forConDeclaradorTipado", envolver(
        "for(int i = 0; i < 10; i) ; for(int[] v = null; ; ) ; for(Persona p : lista) ;"),
        ("for",))
    correcto("forConPosfijo", envolver("for(int i = 0; i < 10; i++) ;"), ("for", "posfijo"))
    correcto("forsAnidados", envolver("for(;;) for(;;) for(;;) ;"), ("for",))
    correcto("forConBloque", envolver("for(int i = 0; i < 10; i++) { m1(); m2(); }"),
             ("for", "posfijo"))

    con_error("lambdaConCuerpoDeLlaves", envolver("var f = x -> { return x; };"), "{", 3,
              ("lambda",))
    con_error("lambdaConParametroTipado", envolver("var f = (int x) -> x;"), "int", 3,
              ("lambda",))
    con_error("lambdaConParametroQueNoEsIdentificador", envolver("var f = (a + 1) -> a;"),
              "a", 3, ("lambda",))
    con_error("lambdaConParametroRepetido", envolver("var f = (a, a) -> a;"), "a", 3,
              ("lambda",))
    con_error("thisExplicitoEnLambda", envolver("var f = x -> this.campo;"), "this", 3,
              ("lambda",))
    con_error("capturaDeVariableEnLambda", envolver("var f = x -> x + n;"), "n", 3,
              ("lambda",))
    con_error("lambdaAnidadaQueUsaElParametroDeAfuera", envolver("var f = x -> y -> x;"),
              "x", 3, ("lambda",))
    con_error("lambdaSinCuerpo", envolver("var f = x -> ;"), ";", 3, ("lambda",))
    con_error("flechaSinLambda", envolver("var f = 1 -> 2;"), "->", 3, ("lambda",))
    con_error("forConDosExpresionesPorSeparador", envolver("for(var i = 0, j = 0; ; ) ;"),
              ",", 3, ("for",))
    con_error("forDeIteradoresConClausulas", envolver("for(var e : lista; ; ) ;"), ";", 3,
              ("for",))
    con_error("forSinParentesis", envolver("for ;; ;"), ";", 3, ("for",))
    con_error("forConUnSoloSeparador", envolver("for(;) ;"), ")", 3, ("for",))
    # Despues de `for (var i` tiene que venir `:` o `=`.
    con_error("forConVarSinInicializar", envolver("for(var i;;) ;"), ";", 3, ("for",))
    con_error("forConDimensionesAlEstiloC", envolver("for(int v[] = null; ; ) ;"), "[", 3,
              ("for",))


def casos_combinados():
    correcto("todosLosLogrosJuntos", """class Registro<T> extends Base<T> {
  private Lista<Lista<T>> datos = new Lista<>();
  public int cuenta = 0;
  private float promedio = 0.5f;

  public Registro(Lista<T> iniciales){
    int i, j, total = 0;
    for(int k = 0; k < 10; k++) {
      var f = (a, b) -> a;
      total = k > 5 ? k : total;
    }
    for(T elemento : iniciales) {
      this.cuenta++;
    }
    var matriz = new int[][]{{1,2},{3,4}};
  }

  private Lista<T> filtrar(Lista<T> origen, int limite){
    var predicado = x -> x;
    return origen;
  }
}
""", TODOS)
    correcto("programaRealistaSinLogros", """class Pila {
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


def escribir_manifiesto():
    """Se fusiona con lo que ya declaró el generador del fuzzer: los dos escriben
    en el mismo manifiesto y ninguno tiene que pisar al otro."""
    manifiesto = RAIZ / "resources" / "sintactico" / "requisitos-de-logros.txt"
    lineas = manifiesto.read_text(encoding="utf-8").splitlines() if manifiesto.exists() else []
    mios = set(requisitos)
    conservadas = [l for l in lineas
                   if not any(l.strip().startswith(caso) for caso in mios)]
    if conservadas and conservadas[-1].strip():
        conservadas.append("")
    conservadas.append("# Casos escritos a mano (herramientas/lote_sintactico.py).")
    for caso in sorted(requisitos):
        conservadas.append(f"{caso:<48} | {','.join(requisitos[caso])} |")
    manifiesto.write_text("\n".join(conservadas) + "\n", encoding="utf-8")
    print(f"{len(requisitos)} casos escritos a mano con requisitos de logros")


def main():
    for carpeta in (SIN_ERRORES, CON_ERRORES):
        carpeta.mkdir(parents=True, exist_ok=True)
    antes = (len(list(SIN_ERRORES.glob("*.java"))), len(list(CON_ERRORES.glob("*.java"))))

    casos_degenerados()
    casos_de_estructura()
    casos_de_miembros()
    casos_de_sentencias()
    casos_de_expresiones()
    casos_de_lambdas_y_fors()
    casos_combinados()
    escribir_manifiesto()

    despues = (len(list(SIN_ERRORES.glob("*.java"))), len(list(CON_ERRORES.glob("*.java"))))
    print(f"sinErrores: {antes[0]} -> {despues[0]}")
    print(f"conErrores: {antes[1]} -> {despues[1]}")


if __name__ == "__main__":
    sys.exit(main())
