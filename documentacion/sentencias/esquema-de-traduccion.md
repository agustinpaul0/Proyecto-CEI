# Esquema de traducción de los AST

Los AST se construyen mientras el sintáctico reconoce cada cuerpo (Pautas Etapa 4, slide 6). Igual
que la tabla de símbolos de la Etapa 3, el diseño es un **Esquema de Traducción (EDT)** sobre la
gramática LL(1) que implementa el parser
([`../sintactico/gramatica-ll1-final.md`](../sintactico/gramatica-ll1-final.md)), y no sobre la
original: las acciones van en los métodos de ese parser (Clase 09, slides 40-41).

La traducción al analizador descendente recursivo es la de la Clase 6, slides 20-21:
- un **atributo de salida** `X.nodo` es el valor de retorno del método de `X`;
- un **atributo de entrada** es un parámetro del método. Lo llevan los no terminales que la
  factorización y la eliminación de la recursión a izquierda dejaron sin su parte izquierda: el
  nodo ya armado les llega de su padre (Clase 11, slides 20-21).

`TS` es la tabla de símbolos. Las producciones de las declaraciones conservan las acciones de la
Etapa 3 ([`../semantico/esquema-de-traduccion.md`](../semantico/esquema-de-traduccion.md)).

## Dónde se cuelga el AST

```
CuerpoDeMetodo → ArgsFormales Bloque { TS.unidadActual().setCuerpo(Bloque.nodo) }
```

La unidad actual es el método o constructor que la acción anterior creó y registró con
`TS.setUnidadActual` (Etapa 3). Las interfaces no tienen cuerpos, y el constructor por defecto que
agrega `cerrar()` tampoco.

## Sentencias

```
Bloque → { { lista = [] } ListaSentencias(lista) } { Bloque.nodo = new NodoBloque(lista) }

ListaSentencias(lista) → Sentencia { lista.agregar(Sentencia.nodo) } ListaSentencias(lista)
ListaSentencias(lista) → ε

Sentencia → ;                    { Sentencia.nodo = new NodoVacia() }
Sentencia → VarLocal ;           { Sentencia.nodo = VarLocal.nodo }
Sentencia → Return ;             { Sentencia.nodo = Return.nodo }
Sentencia → If                   { Sentencia.nodo = If.nodo }
Sentencia → While                { Sentencia.nodo = While.nodo }
Sentencia → Bloque               { Sentencia.nodo = Bloque.nodo }
Sentencia → For                  { Sentencia.nodo = For.nodo }
Sentencia → ExpresionSinIdClase ;
            { Sentencia.nodo = new NodoSentenciaExpresion(ExpresionSinIdClase.nodo) }
Sentencia → TipoPrimitivo DimensionesOpcionales ListaDeclaradores ;
            { Sentencia.nodo = new SentenciaNoControlada(TipoPrimitivo) }
Sentencia → idGen DimensionesOpcionales ListaDeclaradores ;
            { Sentencia.nodo = new SentenciaNoControlada(idGen) }
Sentencia → idClase SentenciaDesdeIdClase(idClase)
            { Sentencia.nodo = SentenciaDesdeIdClase.nodo }

SentenciaDesdeIdClase(c) → TipoGenericoOpcional DimensionesOpcionales ListaDeclaradores ;
            { SentenciaDesdeIdClase.nodo = new SentenciaNoControlada(c) }
SentenciaDesdeIdClase(c) → . idMetVar ArgsActuales
            ColaDeExpresionDesdePrimario(new NodoLlamadaEstatica(c, ., idMetVar, (, ArgsActuales.lista)) ;
            { SentenciaDesdeIdClase.nodo =
                  new NodoSentenciaExpresion(ColaDeExpresionDesdePrimario.nodo) }

VarLocal → var idMetVar = ExpresionCondicional
            { VarLocal.nodo = new NodoVarLocal(idMetVar, ExpresionCondicional.nodo) }

Return → return ExpresionOpcional
            { Return.nodo = new NodoReturn(return, ExpresionOpcional.nodo) }
ExpresionOpcional → Expresion    { ExpresionOpcional.nodo = Expresion.nodo }
ExpresionOpcional → ε            { ExpresionOpcional.nodo = ninguno }

If → if ( Expresion ) Sentencia ElseOpcional
            { If.nodo = new NodoIf(Expresion.nodo, Sentencia.nodo, ElseOpcional.nodo) }
ElseOpcional → else Sentencia    { ElseOpcional.nodo = Sentencia.nodo }
ElseOpcional → ε                 { ElseOpcional.nodo = ninguno }

While → while ( Expresion ) Sentencia
            { While.nodo = new NodoWhile(Expresion.nodo, Sentencia.nodo) }

For → for ( EncabezadoFor ) Sentencia   { For.nodo = new SentenciaNoControlada(for) }
```

Las declaraciones con tipo y el `for` son de logros de la Etapa 2 que no se controlan: se reconocen
enteros, igual que en la Etapa 2, y el nodo los marca ([`decisiones-de-logica.md`](decisiones-de-logica.md) §1).

## Expresiones

Los binarios son la parte donde la gramática LL(1) más se aleja de la original. La original es
recursiva a izquierda,

```
ExpresionCompuesta → ExpresionCompuesta OperadorBinario ExpresionBasica | ExpresionBasica
```

y su árbol asocia a izquierda: `a - b - c` es `(a - b) - c`. La LL(1) la reescribe con recursión a
derecha, y la forma original se recupera pasando el nodo armado hasta acá como atributo de entrada,
como en la Clase 11, slide 21:

```
ExpresionCompuesta → ExpresionBasica RestoExpresionCompuesta(ExpresionBasica.nodo)
            { ExpresionCompuesta.nodo = RestoExpresionCompuesta.nodo }

RestoExpresionCompuesta(izq) → OperadorBinario ExpresionBasica
            RestoExpresionCompuesta(new NodoBinario(izq, OperadorBinario.token, ExpresionBasica.nodo))
            { RestoExpresionCompuesta.nodo = RestoExpresionCompuesta₂.nodo }
RestoExpresionCompuesta(izq) → ε { RestoExpresionCompuesta.nodo = izq }
```

Los trece operadores van en un mismo nivel, sin la precedencia de Java (documento §11, 2.º párrafo;
lo confirmó el profesor). Todo eso vive en este único método del parser.

```
Expresion → ExpresionCompuesta RestoExpresion(ExpresionCompuesta.nodo)
            { Expresion.nodo = RestoExpresion.nodo }
RestoExpresion(izq) → OperadorAsignacion ExpresionCondicional
            { RestoExpresion.nodo = new NodoAsignacion(izq, =, ExpresionCondicional.nodo) }
RestoExpresion(izq) → RestoCondicional(izq) { RestoExpresion.nodo = RestoCondicional.nodo }

ExpresionCondicional → ExpresionCompuesta RestoCondicional(ExpresionCompuesta.nodo)
            { ExpresionCondicional.nodo = RestoCondicional.nodo }
RestoCondicional(c) → ? Expresion : ExpresionCondicional
            { RestoCondicional.nodo = new ExpresionNoControlada(?) }
RestoCondicional(c) → ε { RestoCondicional.nodo = c }

ExpresionBasica → OperadorUnario Operando
            { ExpresionBasica.nodo = new NodoUnario(OperadorUnario.token, Operando.nodo) }
ExpresionBasica → Operando { ExpresionBasica.nodo = Operando.nodo }

Operando → Primitivo  { Operando.nodo = Primitivo.nodo }
Operando → Referencia PosfijoOpcional(Referencia.nodo) { Operando.nodo = PosfijoOpcional.nodo }
PosfijoOpcional(r) → ++ { PosfijoOpcional.nodo = new ExpresionNoControlada(++) }
PosfijoOpcional(r) → -- { PosfijoOpcional.nodo = new ExpresionNoControlada(--) }
PosfijoOpcional(r) → ε  { PosfijoOpcional.nodo = r }

Primitivo → true | false | intLiteral | charLiteral | null | floatLiteral
            { Primitivo.nodo = new NodoLiteral(el token) }
```

`ExpresionSinIdClase` repite las cabezas de `Expresion` para competir con la declaración local, y
comparte las colas:

```
ExpresionSinIdClase → OperadorUnario Operando ColaTrasOperando(new NodoUnario(OperadorUnario.token, Operando.nodo))
ExpresionSinIdClase → Primitivo ColaTrasOperando(Primitivo.nodo)
ExpresionSinIdClase → PrimarioSinIdClase ColaDeExpresionDesdePrimario(PrimarioSinIdClase.nodo)
            { en las tres, ExpresionSinIdClase.nodo = el nodo de la cola }

ColaTrasOperando(op) → RestoExpresionCompuesta(op) RestoExpresion(RestoExpresionCompuesta.nodo)
            { ColaTrasOperando.nodo = RestoExpresion.nodo }
ColaDeExpresionDesdePrimario(p) → RestoReferencia(p) PosfijoOpcional(RestoReferencia.nodo)
            ColaTrasOperando(PosfijoOpcional.nodo)
            { ColaDeExpresionDesdePrimario.nodo = ColaTrasOperando.nodo }
```

## Referencias y encadenados

Un encadenado envuelve a su receptor: cada `.x`, `.m(...)` o `[i]` recibe el nodo armado hasta acá.

```
Referencia → Primario RestoReferencia(Primario.nodo) { Referencia.nodo = RestoReferencia.nodo }

RestoReferencia(r) → Encadenado(r) RestoReferencia(Encadenado.nodo)
RestoReferencia(r) → AccesoArreglo(r) RestoReferencia(AccesoArreglo.nodo)
            { en las dos, RestoReferencia.nodo = RestoReferencia₂.nodo }
RestoReferencia(r) → ε { RestoReferencia.nodo = r }

Encadenado(r) → . idMetVar EncadenadoAtributoOMetodo(r, ., idMetVar)
            { Encadenado.nodo = EncadenadoAtributoOMetodo.nodo }
EncadenadoAtributoOMetodo(r, p, n) → ArgsActuales
            { EncadenadoAtributoOMetodo.nodo = new NodoLlamadaConReceptor(r, p, n, (, ArgsActuales.lista) }
EncadenadoAtributoOMetodo(r, p, n) → ε
            { EncadenadoAtributoOMetodo.nodo = new NodoAccesoAtributo(r, p, n) }

AccesoArreglo(r) → [ Expresion ] { AccesoArreglo.nodo = new NodoAccesoArreglo(r, [, Expresion.nodo) }

Primario → LlamadaMetodoEstatico { Primario.nodo = LlamadaMetodoEstatico.nodo }
Primario → PrimarioSinIdClase    { Primario.nodo = PrimarioSinIdClase.nodo }
LlamadaMetodoEstatico → idClase . idMetVar ArgsActuales
            { LlamadaMetodoEstatico.nodo = new NodoLlamadaEstatica(idClase, ., idMetVar, (, ArgsActuales.lista) }

PrimarioSinIdClase → this           { PrimarioSinIdClase.nodo = new NodoThis(this) }
PrimarioSinIdClase → stringLiteral  { PrimarioSinIdClase.nodo = new NodoLiteral(stringLiteral) }
PrimarioSinIdClase → ( Expresion )  { PrimarioSinIdClase.nodo = new NodoParentizada((, Expresion.nodo) }
PrimarioSinIdClase → idMetVar AccesoOLlamada(idMetVar) { PrimarioSinIdClase.nodo = AccesoOLlamada.nodo }
PrimarioSinIdClase → Creacion       { PrimarioSinIdClase.nodo = Creacion.nodo }

AccesoOLlamada(n) → ArgsActuales { AccesoOLlamada.nodo = new NodoLlamada(n, (, ArgsActuales.lista) }
AccesoOLlamada(n) → ε            { AccesoOLlamada.nodo = new NodoVariable(n) }
```

`(` es el paréntesis que abre los argumentos: es donde javac ubica el error de una llamada cuyo
resultado no sirve donde se usa.

## Argumentos

```
ArgsActuales → ( { lista = [] } ListaExpsOpcional(lista) ) { ArgsActuales.lista = lista }
ListaExpsOpcional(lista) → ListaExps(lista) | ε
ListaExps(lista) → Expresion { lista.agregar(Expresion.nodo) } RestoListaExps(lista)
RestoListaExps(lista) → , ListaExps(lista) | ε
```

## Creaciones

El `new` baja como atributo de entrada hasta que se sabe qué se crea.

```
Creacion → new ArregloOInstancia(new) { Creacion.nodo = ArregloOInstancia.nodo }

ArregloOInstancia(nw) → TipoPrimitivo CreacionDeArreglo(nw, TipoPrimitivo.tipo)
ArregloOInstancia(nw) → idGen CreacionDeArreglo(nw, VariableDeTipo(idGen))
ArregloOInstancia(nw) → idClase CreacionDesdeIdClase(nw, idClase)
            { en las tres, ArregloOInstancia.nodo = el nodo de la derecha }

CreacionDesdeIdClase(nw, c) → < GenericoDeCreacion(nw, c)
CreacionDesdeIdClase(nw, c) → ArregloOInstanciaDeClase(nw, TipoReferencia(c))
GenericoDeCreacion(nw, c) → > ArgsActuales { GenericoDeCreacion.nodo = new ExpresionNoControlada(nw) }
GenericoDeCreacion(nw, c) → InstanciadoOParametrico > ArregloOInstanciaDeClase(nw, TipoReferencia(c, InstanciadoOParametrico.tipo))

ArregloOInstanciaDeClase(nw, t) → CreacionDeArreglo(nw, t)
ArregloOInstanciaDeClase(nw, t) → ArgsActuales
            { ArregloOInstanciaDeClase.nodo = new NodoCreacionObjeto(nw, t, ArgsActuales.lista) }

CreacionDeArreglo(nw, b) → [ RestoCreacionDeArreglo(nw, b)
RestoCreacionDeArreglo(nw, b) → { tamanios = [] } Expresion { tamanios.agregar(Expresion.nodo) } ]
            DimensionesConTamanioOpcional(tamanios)
            { RestoCreacionDeArreglo.nodo = new NodoCreacionArreglo(nw, b, tamanios) }
RestoCreacionDeArreglo(nw, b) → ] DimensionesOpcionales InicializadorDeArreglo
            { RestoCreacionDeArreglo.nodo = new ExpresionNoControlada(nw) }

DimensionesConTamanio(tamanios) → [ Expresion { tamanios.agregar(Expresion.nodo) } ]
            DimensionesConTamanioOpcional(tamanios)
DimensionesConTamanioOpcional(tamanios) → DimensionesConTamanio(tamanios) | ε
```

El diamante y el inicializador de arreglo son de logros que no se controlan.

## Cómo se verifica

`EsquemaDeTraduccionDeSentenciasTest` parsea un cuerpo por cada producción y compara su árbol,
impreso como expresión S, con el esperado. Por ejemplo, `x = a < b == c && d || e;` da
`(sentencia (= x (|| (&& (== (< a b) c) d) e)))`.
