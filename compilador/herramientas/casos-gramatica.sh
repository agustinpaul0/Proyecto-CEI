#!/usr/bin/env bash
# Casos de aceptación/rechazo de la gramática de MiniJava.
#
# Simula el parser LL(1) (Esquema Simple) sobre secuencias de tokens, sin
# necesidad de que exista todavía el analizador sintáctico en Java. Verifica
# que la gramática acepte lo que MiniJava admite y rechace lo que no.
#
# Uso, desde compilador/:
#     bash herramientas/casos-gramatica.sh
set -uo pipefail
cd "$(dirname "$0")/.."

G=gramatica/minijava-ll1-final.g
fallos=0

probar() { # $1=esperado(SI/NO) $2=descripcion $3=tokens
  out=$(python3 herramientas/primeros.py "$G" --aceptar "$3" 2>&1 | tail -1)
  case "$out" in ACEPTADA*) got=SI;; *) got=NO;; esac
  if [ "$got" = "$1" ]; then printf "  ok    %s\n" "$2"
  else printf "  FALLA %s\n        esperado=%s obtuvo=%s -- %s\n" "$2" "$1" "$got" "$out"
       fallos=$((fallos+1)); fi
}

C="class idClase {"
C="class idClase {"
M="$C void idMetVar ( ) {"
echo "--- nucleo ---"
probar SI "clase vacia"                 "class idClase { } eof"
probar SI "atributo"                    "$C int idMetVar ; } eof"
probar SI "metodo con cuerpo"           "$C void idMetVar ( ) { } } eof"
probar SI "constructor"                 "$C public idClase ( ) { } } eof"
probar SI "var local + llamada estatica" "$C void idMetVar ( ) { var idMetVar = intLiteral ; idClase . idMetVar ( ) ; } } eof"
probar SI "if/else anidado"             "$C void idMetVar ( ) { if ( true ) if ( false ) ; else ; } } eof"
probar SI "encadenamiento"              "$C void idMetVar ( ) { this . idMetVar . idMetVar ( ) [ intLiteral ] ; } } eof"

echo "--- cada construccion contra su propia sintaxis ---"
probar SI "int x , y = 10 ;"         "$C void idMetVar ( ) { int idMetVar , idMetVar = intLiteral ; } } eof"
probar SI "C x ;  (decl de referencia)" "$C void idMetVar ( ) { idClase idMetVar ; } } eof"
probar SI "C.m() ; sigue andando"    "$C void idMetVar ( ) { idClase . idMetVar ( ) ; } } eof"
probar SI "private int x ;"         "$C private int idMetVar ; } eof"
probar SI "constructor sin public"  "$C idClase ( ) { } } eof"
probar SI "interfaz: public int m() ;"      "interface idClase { public int idMetVar ( ) ; } eof"
probar NO "interfaz: private int m() ;  (Java lo rechaza sin cuerpo)" \
                                                        "interface idClase { private int idMetVar ( ) ; } eof"
probar NO "interfaz: metodo con cuerpo (MiniJava no los tiene)" \
                                                        "interface idClase { int idMetVar ( ) { } } eof"
probar SI "interfaz: firma sola" "interface idClase { int idMetVar ( ) ; } eof"
probar SI "int x = 5 ;"             "$C int idMetVar = intLiteral ; } eof"
probar SI "for estandar"            "$C void idMetVar ( ) { for ( var idMetVar = intLiteral ; true ; idMetVar ) ; } } eof"
probar SI "for con i++"             "$C void idMetVar ( ) { for ( var idMetVar = intLiteral ; true ; idMetVar ++ ) ; } } eof"
probar SI "for de iteradores"       "$C void idMetVar ( ) { for ( var idMetVar : idMetVar ) ; } } eof"
probar SI "for ( ; ; )"              "$C void idMetVar ( ) { for ( ; ; ) ; } } eof"
probar NO "for ( int i = 0 ; ; )  (la variable de control se declara con var)" \
  "$C void idMetVar ( ) { for ( int idMetVar = intLiteral ; ; ) ; } } eof"
probar NO "for ( C x : l )  (la variable de control se declara con var)" \
  "$C void idMetVar ( ) { for ( idClase idMetVar : idMetVar ) ; } } eof"
probar SI "c ? a : b"               "$C void idMetVar ( ) { idMetVar = true ? intLiteral : intLiteral ; } } eof"
probar SI "ternario anidado a derecha" "$C void idMetVar ( ) { idMetVar = true ? intLiteral : false ? intLiteral : intLiteral ; } } eof"
# JLS 15.25: el operando del MEDIO es una Expression completa y el de la derecha
# una ConditionalExpression, o sea que en Java `a ? b = 1 : c` es valido y
# `a ? b : c = d` no lo es. Regla «lo no especificado es igual a Java».
probar SI "asignacion en el operando del medio"  "$C void idMetVar ( ) { idMetVar = true ? idMetVar = intLiteral : intLiteral ; } } eof"
probar NO "asignacion en el operando derecho"    "$C void idMetVar ( ) { idMetVar = true ? intLiteral : idMetVar = intLiteral ; } } eof"
# El lado izquierdo del '=' es una ExpresionCompuesta, como en la gramatica
# original: un ternario no lo es, y entre parentesis si.
probar NO "c ? a : b = d ;  ternario como lado izquierdo"  "$M true ? idMetVar : idMetVar = intLiteral ; } } eof"
probar SI "( c ? a : b ) = d ;  parentizado es primario"   "$M ( true ? idMetVar : idMetVar ) = intLiteral ; } } eof"
probar SI "el medio admite otro ternario"        "$C void idMetVar ( ) { idMetVar = true ? false ? intLiteral : intLiteral : intLiteral ; } } eof"
# El ternario tiene que llegar a TODO lado derecho de un '=', no solo al de la
# asignacion: los cinco inicializadores tambien.
probar SI "var x = c ? a : b ;"        "$C void idMetVar ( ) { var idMetVar = true ? intLiteral : intLiteral ; } } eof"
probar SI "atributo: int x = c ? a : b ;"  "$C int idMetVar = true ? intLiteral : intLiteral ; } eof"
probar SI "local clasica: int x = c ? a : b ;" "$C void idMetVar ( ) { int idMetVar = true ? intLiteral : intLiteral ; } } eof"
probar SI "for ( var i = c ? a : b ; ; )"      "$C void idMetVar ( ) { for ( var idMetVar = true ? intLiteral : intLiteral ; ; ) ; } } eof"
probar SI "x ++"                    "$C void idMetVar ( ) { idMetVar ++ ; } } eof"

echo "--- Inicializacion de Arreglos ---"
probar SI "new int [ ] { 1 , 2 }"        "$M var idMetVar = new int [ ] { intLiteral , intLiteral } ; } } eof"
probar SI "new int [ ] { }  (vacio)"     "$M var idMetVar = new int [ ] { } ; } } eof"
probar NO "new int [ 2 ] { 1 , 2 }  (tamanio con inicializador)" "$M var idMetVar = new int [ intLiteral ] { intLiteral , intLiteral } ; } } eof"
probar NO "new int [ ]  (sin tamanio ni inicializador)"        "$M var idMetVar = new int [ ] ; } } eof"
probar SI "new int [ ] { 1 , }  (coma final, como en Java)"   "$M var idMetVar = new int [ ] { intLiteral , } ; } } eof"
probar SI "new int [ ] { , }"            "$M var idMetVar = new int [ ] { , } ; } } eof"
probar NO "new int [ ] { 1 , , }"        "$M var idMetVar = new int [ ] { intLiteral , , } ; } } eof"
probar SI "new int [ 3 ]  (base)"         "$M var idMetVar = new int [ intLiteral ] ; } } eof"
probar SI "new int [ 3 ] [ 4 ]"          "$M var idMetVar = new int [ intLiteral ] [ intLiteral ] ; } } eof"

echo "--- Genericidad Avanzada ---"
probar SI "List < List < Persona > > x ;  (anidado)" "$C idClase < idClase < idClase > > idMetVar ; } eof"
probar SI "new List < > ( )              (diamante)" "$M var idMetVar = new idClase < > ( ) ; } } eof"
probar NO "List < > x ;   diamante en posicion de TIPO" "$C idClase < > idMetVar ; } eof"
probar NO "new List < > [ 3 ]  diamante con arreglo"    "$M var idMetVar = new idClase < > [ intLiteral ] ; } } eof"
probar SI "new List < Persona > [ 3 ]"                  "$M var idMetVar = new idClase < idClase > [ intLiteral ] ; } } eof"
probar SI "List < Persona > x ;"           "$C idClase < idClase > idMetVar ; } eof"

echo "--- float (carry-over Etapa 1) ---"
probar SI "float x ;"            "$C float idMetVar ; } eof"
probar SI "floatLiteral"         "$M idMetVar = floatLiteral ; } } eof"

echo "--- combinaciones ---"
probar SI "todo junto en un atributo" \
  "$C private idClase < idClase > idMetVar = new idClase < > ( ) ; } eof"
probar SI "for + ternario + posfijo" \
  "$M for ( var idMetVar = intLiteral ; true ; idMetVar ++ ) idMetVar = true ? intLiteral : intLiteral ; } } eof"

echo "--- ambiguedades conocidas: la gramatica las acepta, la resolucion es voraz ---"
# No se puede comprobar CUAL arbol elige la gramatica —eso lo fija el orden de
# las ramas en el parser—, pero si que la cadena sea aceptada. El arbol elegido
# se fija con los casos del corpus cuando exista el analizador.
probar SI "dangling else: if(a) if(b) s; else t;" \
  "$M if ( true ) if ( false ) ; else ; } } eof"
probar SI "new int[3][4]  (heredada de la catedra)" \
  "$M var idMetVar = new int [ intLiteral ] [ intLiteral ] ; } } eof"

echo "--- inicializacion de arreglos multidimensional (como en Java) ---"
probar SI "new int[]{1,2}"             "$M var idMetVar = new int [ ] { intLiteral , intLiteral } ; } } eof"
probar SI "new int[][]{{1},{2}}"       "$M var idMetVar = new int [ ] [ ] { { intLiteral } , { intLiteral } } ; } } eof"
probar SI "new int[][][]{{{1}}}"       "$M var idMetVar = new int [ ] [ ] [ ] { { { intLiteral } } } ; } } eof"
probar NO "new int[2][2]{{1},{2}}  con tamanio" "$M var idMetVar = new int [ intLiteral ] [ intLiteral ] { { intLiteral } , { intLiteral } } ; } } eof"
probar SI "new int[][]{}"                "$M var idMetVar = new int [ ] [ ] { } ; } } eof"
probar SI "new int[][]{{}}"              "$M var idMetVar = new int [ ] [ ] { { } } ; } } eof"
probar NO "new int[][]{{}{}}  sin coma"  "$M var idMetVar = new int [ ] [ ] { { } { } } ; } } eof"
probar NO "new int[2][]  dimension vacia sin inicializador" "$M var idMetVar = new int [ intLiteral ] [ ] ; } } eof"
probar NO "new int[][2]{}"             "$M var idMetVar = new int [ ] [ intLiteral ] { } ; } } eof"

echo "--- orden de modificadores: los dos, como en Java ---"
probar SI "public static void f(){}"   "$C public static void idMetVar ( ) { } } eof"
probar SI "static public void f(){}"   "$C static public void idMetVar ( ) { } } eof"
probar SI "private static int f(){}"   "$C private static int idMetVar ( ) { } } eof"
probar SI "static private int f(){}"   "$C static private int idMetVar ( ) { } } eof"
probar NO "public private void f(){}"  "$C public private void idMetVar ( ) { } } eof"
probar NO "static static void f(){}"   "$C static static void idMetVar ( ) { } } eof"
probar NO "public static private void f(){}" "$C public static private void idMetVar ( ) { } } eof"

echo
if [ "$fallos" -eq 0 ]; then echo "TODOS LOS CASOS PASAN."; else echo "$fallos CASO(S) FALLAN."; fi
exit "$fallos"
