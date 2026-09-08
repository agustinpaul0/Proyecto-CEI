#!/usr/bin/env bash
# Casos de aceptación/rechazo de la gramática de MiniJava.
#
# Simula el parser LL(1) (Esquema Simple) sobre secuencias de tokens, sin
# necesidad de que exista todavía el analizador sintáctico en Java. Verifica
# dos cosas por cada logro: que la gramática ACEPTE lo que el logro pide, y que
# lo RECHACE cuando el logro está apagado.
#
# Uso, desde compilador/:
#     bash herramientas/casos-gramatica.sh
set -uo pipefail
cd "$(dirname "$0")/.."

G=gramatica/minijava-ll1-logros.g
fallos=0

probar() { # $1=esperado(SI/NO) $2=logros $3=descripcion $4=tokens
  out=$(python3 herramientas/primeros.py "$G" --logros "$2" --aceptar "$4" 2>&1 | tail -1)
  case "$out" in ACEPTADA*) got=SI;; *) got=NO;; esac
  if [ "$got" = "$1" ]; then printf "  ok    %s\n" "$3"
  else printf "  FALLA %s\n        esperado=%s obtuvo=%s -- %s\n" "$3" "$1" "$got" "$out"
       fallos=$((fallos+1)); fi
}

C="class idClase {"
C="class idClase {"
M="$C void idMetVar ( ) {"
echo "--- nucleo (sin ningun logro) ---"
probar SI ninguno "clase vacia"                 "class idClase { } eof"
probar SI ninguno "atributo"                    "$C int idMetVar ; } eof"
probar SI ninguno "metodo con cuerpo"           "$C void idMetVar ( ) { } } eof"
probar SI ninguno "constructor"                 "$C public idClase ( ) { } } eof"
probar SI ninguno "var local + llamada estatica" "$C void idMetVar ( ) { var idMetVar = intLiteral ; idClase . idMetVar ( ) ; } } eof"
probar SI ninguno "if/else anidado"             "$C void idMetVar ( ) { if ( true ) if ( false ) ; else ; } } eof"
probar SI ninguno "encadenamiento"              "$C void idMetVar ( ) { this . idMetVar . idMetVar ( ) [ intLiteral ] ; } } eof"
probar NO ninguno "a = b = c NO es valido"      "$C void idMetVar ( ) { idMetVar = idMetVar = idMetVar ; } } eof"

echo "--- cada logro contra su propia sintaxis ---"
probar SI varclasica "int x , y = 10 ;"         "$C void idMetVar ( ) { int idMetVar , idMetVar = intLiteral ; } } eof"
probar SI varclasica "C x ;  (decl de referencia)" "$C void idMetVar ( ) { idClase idMetVar ; } } eof"
probar SI varclasica "C.m() ; sigue andando"    "$C void idMetVar ( ) { idClase . idMetVar ( ) ; } } eof"
probar NO ninguno    "int x ; sin el logro"     "$C void idMetVar ( ) { int idMetVar ; } } eof"
probar SI visibilidad "private int x ;"         "$C private int idMetVar ; } eof"
probar SI visibilidad "constructor sin public"  "$C idClase ( ) { } } eof"
probar SI visibilidad "interfaz: public int m() ;"      "interface idClase { public int idMetVar ( ) ; } eof"
probar NO visibilidad "interfaz: private int m() ;  (Java lo rechaza sin cuerpo)" \
                                                        "interface idClase { private int idMetVar ( ) ; } eof"
probar NO todos       "interfaz: metodo con cuerpo (MiniJava no los tiene)" \
                                                        "interface idClase { int idMetVar ( ) { } } eof"
probar SI ninguno     "interfaz: firma sola sin el logro" "interface idClase { int idMetVar ( ) ; } eof"
probar NO ninguno     "interfaz: public sin el logro"     "interface idClase { public int idMetVar ( ) ; } eof"
probar NO ninguno     "private sin el logro"    "$C private int idMetVar ; } eof"
probar SI atrinit     "int x = 5 ;"             "$C int idMetVar = intLiteral ; } eof"
probar NO ninguno     "atributo inicializado sin logro" "$C int idMetVar = intLiteral ; } eof"
probar SI for         "for estandar"            "$C void idMetVar ( ) { for ( var idMetVar = intLiteral ; true ; idMetVar ) ; } } eof"
probar SI for,posfijo "for con i++"             "$C void idMetVar ( ) { for ( var idMetVar = intLiteral ; true ; idMetVar ++ ) ; } } eof"
probar SI for         "for de iteradores"       "$C void idMetVar ( ) { for ( var idMetVar : idMetVar ) ; } } eof"
probar SI for,varclasica "for ( int i = 0 ; ; )" "$C void idMetVar ( ) { for ( int idMetVar = intLiteral ; ; ) ; } } eof"
probar SI ternario    "c ? a : b"               "$C void idMetVar ( ) { idMetVar = true ? intLiteral : intLiteral ; } } eof"
probar SI ternario    "ternario anidado a derecha" "$C void idMetVar ( ) { idMetVar = true ? intLiteral : false ? intLiteral : intLiteral ; } } eof"
probar NO ninguno     "ternario sin el logro"   "$C void idMetVar ( ) { idMetVar = true ? intLiteral : intLiteral ; } } eof"
# JLS 15.25: el operando del MEDIO es una Expression completa y el de la derecha
# una ConditionalExpression, o sea que en Java `a ? b = 1 : c` es valido y
# `a ? b : c = d` no lo es. Regla «lo no especificado es igual a Java».
probar SI ternario    "asignacion en el operando del medio"  "$C void idMetVar ( ) { idMetVar = true ? idMetVar = intLiteral : intLiteral ; } } eof"
probar NO ternario    "asignacion en el operando derecho"    "$C void idMetVar ( ) { idMetVar = true ? intLiteral : idMetVar = intLiteral ; } } eof"
probar SI ternario    "el medio admite otro ternario"        "$C void idMetVar ( ) { idMetVar = true ? false ? intLiteral : intLiteral : intLiteral ; } } eof"
# El ternario tiene que llegar a TODO lado derecho de un '=', no solo al de la
# asignacion: los cinco inicializadores tambien.
probar SI ternario "var x = c ? a : b ;"        "$C void idMetVar ( ) { var idMetVar = true ? intLiteral : intLiteral ; } } eof"
probar SI ternario,atrinit "atributo: int x = c ? a : b ;"  "$C int idMetVar = true ? intLiteral : intLiteral ; } eof"
probar SI ternario,varclasica "local clasica: int x = c ? a : b ;" "$C void idMetVar ( ) { int idMetVar = true ? intLiteral : intLiteral ; } } eof"
probar SI ternario,for "for ( var i = c ? a : b ; ; )"      "$C void idMetVar ( ) { for ( var idMetVar = true ? intLiteral : intLiteral ; ; ) ; } } eof"
probar NO ninguno  "a = b = c sigue prohibido sin logros"   "$C void idMetVar ( ) { idMetVar = idMetVar = idMetVar ; } } eof"
probar SI posfijo     "x ++"                    "$C void idMetVar ( ) { idMetVar ++ ; } } eof"
probar NO ninguno     "x ++ sin el logro"       "$C void idMetVar ( ) { idMetVar ++ ; } } eof"

echo "--- MiniLambdas (el caso que parecia necesitar lookahead ilimitado) ---"
probar SI lambda "x -> x            (1 param sin parentesis)" "$M var idMetVar = idMetVar -> idMetVar ; } } eof"
probar SI lambda "( ) -> 1          (0 params)"               "$M var idMetVar = ( ) -> intLiteral ; } } eof"
probar SI lambda "( a ) -> a        (1 param con parentesis)" "$M var idMetVar = ( idMetVar ) -> idMetVar ; } } eof"
probar SI lambda "( a , b ) -> a    (2 params)"               "$M var idMetVar = ( idMetVar , idMetVar ) -> idMetVar ; } } eof"
probar SI lambda "( a ) + 1         (parentizada, NO lambda)" "$M var idMetVar = ( idMetVar ) + intLiteral ; } } eof"
probar SI lambda "( a + b )         (parentizada compleja)"   "$M var idMetVar = ( idMetVar + idMetVar ) ; } } eof"
probar SI lambda "lambda como argumento"                      "$M idMetVar ( idMetVar -> idMetVar ) ; } } eof"
probar SI lambda "x -> y -> e   (currificada)"                "$M var idMetVar = idMetVar -> idMetVar -> intLiteral ; } } eof"
probar SI lambda,ternario "c ? x->e : y->e   (en un ternario)" "$M var idMetVar = true ? idMetVar -> intLiteral : idMetVar -> intLiteral ; } } eof"
probar SI lambda "( ) -> e   con cero parametros"             "$M var idMetVar = ( ) -> intLiteral ; } } eof"
probar NO lambda "(a, int b) -> e   2do parametro con tipo"   "$M var idMetVar = ( idMetVar , int idMetVar ) -> intLiteral ; } } eof"
probar NO lambda "(a, b+1) -> e   2do parametro no es id"     "$M var idMetVar = ( idMetVar , idMetVar + intLiteral ) -> intLiteral ; } } eof"
probar NO lambda "cuerpo con llaves NO se permite"            "$M var idMetVar = idMetVar -> { } ; } } eof"
probar NO lambda "parametro con tipo NO se permite"           "$M var idMetVar = ( int idMetVar ) -> idMetVar ; } } eof"
probar NO ninguno "lambda sin el logro"                       "$M var idMetVar = idMetVar -> idMetVar ; } } eof"
probar SI ninguno "( a ) sigue andando sin el logro"          "$M var idMetVar = ( idMetVar ) ; } } eof"

echo "--- Inicializacion de Arreglos ---"
probar SI arrinit "new int [ ] { 1 , 2 }"        "$M var idMetVar = new int [ ] { intLiteral , intLiteral } ; } } eof"
probar SI arrinit "new int [ ] { }  (vacio)"     "$M var idMetVar = new int [ ] { } ; } } eof"
probar SI ninguno "new int [ 3 ] sigue andando"  "$M var idMetVar = new int [ intLiteral ] ; } } eof"
probar SI ninguno "new int [ 3 ] [ 4 ]"          "$M var idMetVar = new int [ intLiteral ] [ intLiteral ] ; } } eof"
probar NO ninguno "new int [ ] { } sin el logro" "$M var idMetVar = new int [ ] { } ; } } eof"

echo "--- Genericidad Avanzada ---"
probar SI generico "List < List < Persona > > x ;  (anidado)" "$C idClase < idClase < idClase > > idMetVar ; } eof"
probar SI generico "new List < > ( )              (diamante)" "$M var idMetVar = new idClase < > ( ) ; } } eof"
probar NO generico "List < > x ;   diamante en posicion de TIPO" "$C idClase < > idMetVar ; } eof"
probar NO generico "new List < > [ 3 ]  diamante con arreglo"    "$M var idMetVar = new idClase < > [ intLiteral ] ; } } eof"
probar SI generico "new List < Persona > [ 3 ]"                  "$M var idMetVar = new idClase < idClase > [ intLiteral ] ; } } eof"
probar SI ninguno  "List < Persona > x ; sin el logro"           "$C idClase < idClase > idMetVar ; } eof"
probar NO ninguno  "anidado sin el logro"                        "$C idClase < idClase < idClase > > idMetVar ; } eof"

echo "--- float (carry-over Etapa 1) ---"
probar SI float   "float x ;"            "$C float idMetVar ; } eof"
probar SI float   "floatLiteral"         "$M idMetVar = floatLiteral ; } } eof"
probar NO ninguno "float sin el logro"   "$C float idMetVar ; } eof"

echo "--- combinaciones ---"
probar SI visibilidad,atrinit,varclasica,generico,arrinit "todo junto en un atributo" \
  "$C private idClase < idClase > idMetVar = new idClase < > ( ) ; } eof"
probar SI for,varclasica,posfijo,ternario "for + ternario + posfijo" \
  "$M for ( int idMetVar = intLiteral ; true ; idMetVar ++ ) idMetVar = true ? intLiteral : intLiteral ; } } eof"

echo "--- ambiguedades conocidas: la gramatica las acepta, la resolucion es voraz ---"
# No se puede comprobar CUAL arbol elige la gramatica —eso lo fija el orden de
# las ramas en el parser—, pero si que la cadena sea aceptada. El arbol elegido
# se fija con los casos del corpus cuando exista el analizador.
probar SI ninguno "dangling else: if(a) if(b) s; else t;" \
  "$M if ( true ) if ( false ) ; else ; } } eof"
probar SI ninguno "new int[3][4]  (heredada de la catedra)" \
  "$M var idMetVar = new int [ intLiteral ] [ intLiteral ] ; } } eof"
probar SI lambda           "x -> a + b     (cuerpo voraz)"  "$M var idMetVar = idMetVar -> idMetVar + idMetVar ; } } eof"
probar SI lambda           "x -> a.m()     (cuerpo voraz)"  "$M var idMetVar = idMetVar -> idMetVar . idMetVar ( ) ; } } eof"
probar SI lambda           "x -> a[0]      (cuerpo voraz)"  "$M var idMetVar = idMetVar -> idMetVar [ intLiteral ] ; } } eof"
probar SI lambda           "x -> a = b     (cuerpo voraz)"  "$M var idMetVar = idMetVar -> idMetVar = idMetVar ; } } eof"
probar SI lambda,ternario  "x -> a ? b : c (cuerpo voraz)"  "$M var idMetVar = idMetVar -> true ? intLiteral : intLiteral ; } } eof"
probar SI lambda,posfijo   "x -> a++       (cuerpo voraz)"  "$M var idMetVar = idMetVar -> idMetVar ++ ; } } eof"

echo "--- inicializacion de arreglos multidimensional (como en Java) ---"
probar SI arrinit "new int[]{1,2}"             "$M var idMetVar = new int [ ] { intLiteral , intLiteral } ; } } eof"
probar SI arrinit "new int[][]{{1},{2}}"       "$M var idMetVar = new int [ ] [ ] { { intLiteral } , { intLiteral } } ; } } eof"
probar SI arrinit "new int[][][]{{{1}}}"       "$M var idMetVar = new int [ ] [ ] [ ] { { { intLiteral } } } ; } } eof"
probar SI arrinit "new int[]{}   vacio"        "$M var idMetVar = new int [ ] { } ; } } eof"
probar NO ninguno "sin el logro"               "$M var idMetVar = new int [ ] { intLiteral } ; } } eof"

echo "--- orden de modificadores: fiel a la gramatica original ---"
probar SI visibilidad "public static void f(){}"  "$C public static void idMetVar ( ) { } } eof"
probar NO visibilidad "static public void f(){}"  "$C static public void idMetVar ( ) { } } eof"

echo
if [ "$fallos" -eq 0 ]; then echo "TODOS LOS CASOS PASAN."; else echo "$fallos CASO(S) FALLAN."; fi
exit "$fallos"
