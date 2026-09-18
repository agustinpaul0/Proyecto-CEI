# Gramática base de MiniJava (transcripción fiel)

Transcripción literal de `documentacion/minijava/Reglas de Sintaxis MiniJava 2026.pdf`,
leído como imágenes. **Este documento no corrige nada**: es la fuente de verdad contra la cual se
verifica cada paso de [`transformacion-a-ll1-paso-a-paso.md`](transformacion-a-ll1-paso-a-paso.md). Si algo acá
no coincide con el PDF, es un error de transcripción y hay que arreglarlo acá, no río abajo.

## Notación (sección 2 del PDF)

| Elemento | Significado |
|---|---|
| **terminal** | símbolo terminal (en el PDF va en negrita/color) |
| `<Clase>` | símbolo no terminal (primera letra mayúscula) |
| `ε` | la cadena vacía |
| `<X> ::= α` | una producción, con α una secuencia de terminales y no terminales |
| `<X> ::= α \| β` | abreviación de `<X> ::= α` y `<X> ::= β` |

No terminal inicial: `<Inicial>`.

## Producciones

### Estructura del programa

```
<Inicial> ::= <ListaClases> eof

<ListaClases> ::= <Clase> <ListaClases> | <Interfaz> <ListaClases> | ε

<Clase> ::= class idClase <GenericidadOpcional> <HerenciaOpcional> { <ListaMiembros> }

<Interfaz> ::= interface idClase <GenericidadOpcional> <ExtensionOpcional> { <ListaMetodosInterfaz> }

<GenericidadOpcional> ::= < idGen > | ε

<HerenciaOpcional> ::= extends <TipoReferencia> | implements <TipoReferencia> | ε

<ExtensionOpcional> ::= extends <TipoReferencia> | ε

<ListaMiembros> ::= <Miembro> <ListaMiembros> | ε

<ListaMetodosInterfaz> ::= <MetodoInterfaz> <ListaMetodosInterfaz> | ε
```

### Miembros

```
<Miembro> ::= <Atributo> | <Metodo> | <Constructor>

<Atributo> ::= <Tipo> idMetVar ;

<Metodo> ::= <ModificadorOpcional> <TipoMetodo> idMetVar <ArgsFormales> <Bloque>

<MetodoInterfaz> ::= <TipoMetodo> idMetVar <ArgsFormales> ;

<Constructor> ::= public idClase <ArgsFormales> <Bloque>

<ModificadorOpcional> ::= static | ε

<TipoMetodo> ::= <Tipo> | void
```

### Tipos

```
<Tipo> ::= <TipoBase> <DimensionesOpcionales>

<TipoBase> ::= <TipoPrimitivo> | <TipoReferencia> | idGen

<DimensionesOpcionales> ::= [ ] <DimensionesOpcionales> | ε

<TipoReferencia> ::= idClase <TipoGenericoOpcional>

<TipoPrimitivo> ::= boolean | char | int

<TipoGenericoOpcional> ::= < <InstanciadoOParametrico> > | ε

<InstanciadoOParametrico> ::= idGen | idClase
```

### Argumentos formales

```
<ArgsFormales> ::= ( <ListaArgsFormalesOpcional> )

<ListaArgsFormalesOpcional> ::= <ListaArgsFormales> | ε

<ListaArgsFormales> ::= <ArgFormal>
<ListaArgsFormales> ::= <ListaArgsFormales> , <ArgFormal>

<ArgFormal> ::= <Tipo> idMetVar
```

### Sentencias

```
<Bloque> ::= { <ListaSentencias> }

<ListaSentencias> ::= <Sentencia> <ListaSentencias> | ε

<Sentencia> ::= ;
<Sentencia> ::= <Asignacion> ;
<Sentencia> ::= <Llamada> ;
<Sentencia> ::= <VarLocal> ;
<Sentencia> ::= <Return> ;
<Sentencia> ::= <If>
<Sentencia> ::= <While>
<Sentencia> ::= <Bloque>

<Asignacion> ::= <Expresion>

<Llamada> ::= <Expresion>

<VarLocal> ::= var idMetVar = <ExpresionCompuesta>

<Return> ::= return <ExpresionOpcional>

<ExpresionOpcional> ::= <Expresion> | ε

<If> ::= if ( <Expresion> ) <Sentencia>
<If> ::= if ( <Expresion> ) <Sentencia> else <Sentencia>

<While> ::= while ( <Expresion> ) <Sentencia>
```

### Expresiones

```
<Expresion> ::= <ExpresionCompuesta> <OperadorAsignacion> <ExpresionCompuesta>
<Expresion> ::= <ExpresionCompuesta>

<OperadorAsignacion> ::= =

<ExpresionCompuesta> ::= <ExpresionCompuesta> <OperadorBinario> <ExpresionBasica>
<ExpresionCompuesta> ::= <ExpresionBasica>

<OperadorBinario> ::= || | && | == | != | < | > | <= | >= | + | - | * | / | %

<ExpresionBasica> ::= <OperadorUnario> <Operando>
<ExpresionBasica> ::= <Operando>

<OperadorUnario> ::= + | - | !

<Operando> ::= <Primitivo>
<Operando> ::= <Referencia>

<Primitivo> ::= true | false | intLiteral | charLiteral | null
```

### Referencias y primarios

```
<Referencia> ::= <Primario>
<Referencia> ::= <Referencia> <VarEncadenada>
<Referencia> ::= <Referencia> <MetodoEncadenado>
<Referencia> ::= <Referencia> <AccesoArreglo>

<Primario> ::= this
<Primario> ::= stringLiteral
<Primario> ::= <AccesoVar>
<Primario> ::= <CrearArreglo>
<Primario> ::= <LlamadaConstructor>
<Primario> ::= <LlamadaMetodo>
<Primario> ::= <LlamadaMetodoEstatico>
<Primario> ::= <ExpresionParentizada>

<AccesoVar> ::= idMetVar

<CrearArreglo> ::= new <TipoBase> <DimensionesConTamanio>

<LlamadaConstructor> ::= new <TipoReferencia> <ArgsActuales>

<ExpresionParentizada> ::= ( <Expresion> )

<LlamadaMetodo> ::= idMetVar <ArgsActuales>

<LlamadaMetodoEstatico> ::= idClase . idMetVar <ArgsActuales>

<DimensionesConTamanio> ::= [ <Expresion> ] <DimensionesConTamanio> | [ <Expresion> ]

<ArgsActuales> ::= ( <ListaExpsOpcional> )

<ListaExpsOpcional> ::= <ListaExps> | ε

<ListaExps> ::= <Expresion>
<ListaExps> ::= <Expresion> , <ListaExps>

<VarEncadenada> ::= . idMetVar

<MetodoEncadenado> ::= . idMetVar <ArgsActuales>

<AccesoArreglo> ::= [ <Expresion> ]
```

## Inventario

- **48 no terminales**, **~70 producciones** (contando cada alternativa de un `|` por separado).
- **Terminales usados**: `eof`, `class`, `interface`, `extends`, `implements`, `static`, `void`,
  `public`, `boolean`, `char`, `int`, `var`, `return`, `if`, `else`, `while`, `this`, `new`,
  `null`, `true`, `false`, `idClase`, `idGen`, `idMetVar`, `intLiteral`, `charLiteral`,
  `stringLiteral`, `( ) { } [ ] ; , . =`, y los operadores `|| && == != < > <= >= + - * / % !`.

## Observaciones sobre la gramática original

Se registran acá, no se corrigen acá.

1. **`<Asignacion>` y `<Llamada>` son la misma producción.** Ambas son `::= <Expresion>`, y
   `<Sentencia>` las usa en dos alternativas idénticas (`<Asignacion> ;` y `<Llamada> ;`). La
   gramática es ambigua en ese punto: toda sentencia-expresión tiene dos árboles de derivación.
2. **El *dangling else* está presente**, en la forma clásica de las dos producciones de `<If>`.
   Junto con el punto 1, son las **únicas dos ambigüedades reales** de la gramática. Otros pares
   comparten prefijo izquierdo — `<VarEncadenada>`/`<MetodoEncadenado>`,
   `<AccesoVar>`/`<LlamadaMetodo>`, `<CrearArreglo>`/`<LlamadaConstructor>`,
   `<Atributo>`/`<Metodo>` — pero derivan lenguajes **disjuntos**, así que rompen LL(1) sin ser
   ambiguos. La demostración está en
   [`transformacion-a-ll1-paso-a-paso.md` §0.4](transformacion-a-ll1-paso-a-paso.md).
3. **No hay niveles de precedencia entre operadores binarios.** `<OperadorBinario>` es una única
   alternancia plana de los 13 operadores, así que `a + b * c` y `a * b + c` se derivan con la
   misma forma. La precedencia y la asociatividad no están en la gramática: son responsabilidad
   de etapas posteriores.
4. **Los operadores `++` y `--` existen en el léxico pero no en esta gramática.** `<OperadorUnario>`
   es sólo `+ | - | !`. El logro *Operadores Posifijos E2* es el que les da un lugar.
5. **`float` y `floatLiteral` tampoco están.** El léxico de la Etapa 1 los emite (logro de
   Etapa 1); si la gramática no se extiende, ese logro queda inutilizable en Etapa 2.
6. **El PDF escribe `[]` como si fuera un solo símbolo** en `<DimensionesOpcionales>`, pero
   léxicamente son dos tokens (`corcheteA` y `corcheteC`): «Aspectos Léxicos MiniJava 2026» no
   define ningún token `[]`. Acá se transcribe como `[ ]`, que es lo que el parser va a tener
   que matchear. Es la única desviación deliberada de la literalidad del PDF en este documento.
7. **No hay `for`, ni `private`, ni `?`**, coherente con «Aspectos Léxicos MiniJava 2026», que
   tampoco los tiene. Los agregan tres de los logros de Etapa 2.
