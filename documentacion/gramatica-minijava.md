# Gramática LL(1) final de MiniJava

Gramática corregida y **verificada** de MiniJava, en la notación BNF-extendida del PDF de la
cátedra. Incluye el núcleo más los 9 logros de la Etapa 2 y el carry-over de `float` de la
Etapa 1.

- El origen, sin tocar, está en [`gramatica-base-minijava.md`](gramatica-base-minijava.md).
- Para entender qué hace cada regla y qué estaba roto: [`guia-de-la-gramatica.md`](guia-de-la-gramatica.md).
- El paso a paso de cada corrección: [`transformacion-gramatica.md`](transformacion-gramatica.md).

**Este documento se genera** desde `compilador/gramatica/minijava-ll1-logros.g`, que es el mismo
archivo que verifica el script — no se edita a mano, para que no puedan divergir:

```bash
cd compilador
python3 herramientas/gramatica_a_markdown.py gramatica/minijava-ll1-logros.g --sin-cabecera
```

## Notación

| Elemento | Significado |
|---|---|
| **terminal** | símbolo terminal (palabra clave o categoría léxica) |
| `` `símbolo` `` | símbolo terminal de puntuación u operador |
| `<Clase>` | símbolo no terminal |
| `ε` | la cadena vacía |
| *[sólo con `logro`]* | la producción existe únicamente con ese logro **prendido** |
| *[sólo sin `logro`]* | la producción existe únicamente con ese logro **apagado** |

No terminal inicial: `<Inicial>`.

## Verificación

```bash
cd compilador
python3 herramientas/primeros.py gramatica/minijava-ll1-logros.g                  # ES LL(1)
python3 herramientas/primeros.py gramatica/minijava-ll1-logros.g --combinatoria   # las 1024 config.
bash herramientas/casos-gramatica.sh                                              # 59 casos
```

Estado actual:

- **98 no terminales, 204 producciones, 60 terminales.**
- **LL(1) en las 1024 combinaciones de logros**, incluida la de cero logros. Ningún logro depende
  de que otro esté prendido: eso es lo que hace que se puedan prender y apagar de a uno.
- **59 casos de aceptación y rechazo** pasan, cubriendo cada logro con su propia sintaxis y con
  la sintaxis que debe rechazar cuando está apagado.

## Producciones

<Inicial> ::= <ListaClases> **eof**  

<ListaClases> ::= <Clase> <ListaClases> | <Interfaz> <ListaClases> | ε  

<Clase> ::= **class** **idClase** <GenericidadOpcional> <HerenciaOpcional> `{` <ListaMiembros> `}`  

<Interfaz> ::= **interface** **idClase** <GenericidadOpcional> <ExtensionOpcional> `{` <ListaMetodosInterfaz> `}`  

> Una Clase o Interfaz sigue limitada a UN único parámetro de tipo, también con
> el logro de genericidad avanzada.

<GenericidadOpcional> ::= `<` **idGen** `>` | ε  

<HerenciaOpcional> ::= **extends** <TipoReferencia> | **implements** <TipoReferencia> | ε  

<ExtensionOpcional> ::= **extends** <TipoReferencia> | ε  

<ListaMiembros> ::= <Miembro> <ListaMiembros> | ε  

<ListaMetodosInterfaz> ::= <MetodoInterfaz> <ListaMetodosInterfaz> | ε  

> MIEMBROS
> La visibilidad se factoriza al frente de los tres tipos de miembro. Eso hace
> que el `public` del <Constructor> original quede absorbido por
> <VisibilidadOpcional>, y que <Constructor> pase a competir con <Tipo> por el
> token idClase: `C(){}` (constructor) vs `C x;` (atributo de tipo C).

<Miembro> ::= <VisibilidadOpcional> <MiembroSinVisibilidad>  *[sólo con `visibilidad`]*  
<Miembro> ::= <MiembroSinVisibilidad>  *[sólo sin `visibilidad`]*  
<Miembro> ::= <Constructor>  *[sólo sin `visibilidad`]*  

<VisibilidadOpcional> ::= **public** | **private** | ε  *[sólo con `visibilidad`]*  

> Sin el logro, el `public` sólo aparece en el constructor y es lo que lo
> distingue del resto de los miembros: es la regla original de la cátedra.

<Constructor> ::= **public** **idClase** <CuerpoDeMetodo>  *[sólo sin `visibilidad`]*  

<MiembroSinVisibilidad> ::= **static** <TipoMetodo> <Metodo>  
<MiembroSinVisibilidad> ::= **void** <Metodo>  
<MiembroSinVisibilidad> ::= <TipoPrimitivo> <DimensionesOpcionales> **idMetVar** <AtributoOMetodo>  
<MiembroSinVisibilidad> ::= **idGen** <DimensionesOpcionales> **idMetVar** <AtributoOMetodo>  
<MiembroSinVisibilidad> ::= **idClase** <MiembroDesdeIdClase>  

> El NT insertado que decide entre constructor y miembro con tipo de referencia.
> Con visibilidad el constructor ya no lleva `public` propio, así que compite
> con el atributo/método de tipo referencia por el token idClase.

<MiembroDesdeIdClase> ::= <CuerpoDeMetodo>  *[sólo con `visibilidad`]*  
<MiembroDesdeIdClase> ::= <TipoGenericoOpcional> <DimensionesOpcionales> **idMetVar** <AtributoOMetodo>  

> El NT insertado que decide entre atributo y método.

<AtributoOMetodo> ::= <InicializacionDeAtributo> `;`  
<AtributoOMetodo> ::= <CuerpoDeMetodo>  

> NT dedicado: la inicialización de un ATRIBUTO es un logro distinto que la de
> una variable local, y tienen que poder prenderse por separado.

<InicializacionDeAtributo> ::= `=` <ExpresionCompuesta>  *[sólo con `atrinit`]*  
<InicializacionDeAtributo> ::= ε  

> <Metodo> sobrevive como no terminal propio y lo comparten la rama static y la
> rama void, así que el semántico tiene UN solo lugar donde construir el nodo de
> método para esas dos formas. La tercera forma (tipo de referencia) ya consumió
> el idMetVar al factorizar contra el atributo, y llega por AtributoOMetodo.

<Metodo> ::= **idMetVar** <CuerpoDeMetodo>  

> Se comparte a propósito: es la misma construcción en método, método static y
> constructor.

<CuerpoDeMetodo> ::= <ArgsFormales> <Bloque>  

> Los métodos de interfaz admiten `public` pero no `private`, que es lo que
> hace Java: verificado con javac, un método de interfaz `private` exige cuerpo
> («missing method body, or declare abstract»), y MiniJava no tiene forma de
> escribir ese cuerpo —<MetodoInterfaz> termina en `;` en la especificación
> original y ningún logro lo cambia—. O sea que un `private` acá sería siempre
> la forma que Java rechaza.

<MetodoInterfaz> ::= <PublicOpcional> <TipoMetodo> **idMetVar** <ArgsFormales> `;`  
<PublicOpcional> ::= **public**  *[sólo con `visibilidad`]*  
<PublicOpcional> ::= ε  

<TipoMetodo> ::= <Tipo> | **void**  

> TIPOS

<Tipo> ::= <TipoBase> <DimensionesOpcionales>  

<TipoBase> ::= <TipoPrimitivo> | <TipoReferencia> | **idGen**  

<DimensionesOpcionales> ::= `[` `]` <DimensionesOpcionales> | ε  

<TipoReferencia> ::= **idClase** <TipoGenericoOpcional>  

> `float` es token del léxico desde la Etapa 1 pero no estaba en la
> gramática; sin esta alternativa el logro de Etapa 1 queda inutilizable.

<TipoPrimitivo> ::= **boolean** | **char** | **int**  
<TipoPrimitivo> ::= **float**  *[sólo con `float`]*  

> En POSICIÓN DE TIPO no se admite el diamante: `List<> x;` es inválido.

<TipoGenericoOpcional> ::= `<` <ArgumentoDeTipo> `>` | ε  

> la recursión por <TipoGenericoOpcional> es la que habilita el
> anidamiento `List<List<Persona>>`. Sin el logro: `ArgumentoDeTipo -> idGen | idClase`.

<ArgumentoDeTipo> ::= **idGen**  
<ArgumentoDeTipo> ::= **idClase** <TipoGenericoOpcional>  *[sólo con `generico`]*  
<ArgumentoDeTipo> ::= **idClase**  *[sólo sin `generico`]*  

<ArgsFormales> ::= `(` <ListaArgsFormalesOpcional> `)`  

<ListaArgsFormalesOpcional> ::= <ListaArgsFormales> | ε  

<ListaArgsFormales> ::= <ArgFormal> <RestoListaArgsFormales>  
<RestoListaArgsFormales> ::= `,` <ArgFormal> <RestoListaArgsFormales> | ε  

<ArgFormal> ::= <Tipo> **idMetVar**  

> SENTENCIAS
> Con el logro de variables locales clásicas una declaración local puede empezar con idClase (`C x;`), y
> una sentencia-expresión también (`C.m();`). Se factoriza ese idClase y el
> desempate lo hace el token siguiente: `.` es expresión, cualquier otro es
> declaración. La rama de expresión reusa <ColaDeExpresionDesdePrimario> para no
> duplicar la jerarquía de expresiones.

<Bloque> ::= `{` <ListaSentencias> `}`  

<ListaSentencias> ::= <Sentencia> <ListaSentencias> | ε  

<Sentencia> ::= `;`  
<Sentencia> ::= <VarLocal> `;`  
<Sentencia> ::= <Return> `;`  
<Sentencia> ::= <If>  
<Sentencia> ::= <While>  
<Sentencia> ::= <Bloque>  
<Sentencia> ::= <For>  *[sólo con `for`]*  

> Sin variables locales clásicas, una sentencia-expresión puede empezar con
> idClase sin competir con nada: es la regla original.

<Sentencia> ::= <SentenciaExpresion> `;`  *[sólo sin `varclasica`]*  
<SentenciaExpresion> ::= <Expresion>  *[sólo sin `varclasica`]*  

> Con el logro, `C x;` y `C.m();` compiten por idClase y hay que factorizarlo.

<Sentencia> ::= <ExpresionSinIdClase> `;`  *[sólo con `varclasica`]*  
<Sentencia> ::= <TipoPrimitivo> <DimensionesOpcionales> <ListaDeclaradores> `;`  *[sólo con `varclasica`]*  
<Sentencia> ::= **idGen** <DimensionesOpcionales> <ListaDeclaradores> `;`  *[sólo con `varclasica`]*  
<Sentencia> ::= **idClase** <SentenciaDesdeIdClase>  *[sólo con `varclasica`]*  

<SentenciaDesdeIdClase> ::= <TipoGenericoOpcional> <DimensionesOpcionales> <ListaDeclaradores> `;`  *[sólo con `varclasica`]*  
<SentenciaDesdeIdClase> ::= `.` **idMetVar** <ArgsActuales> <ColaDeExpresionDesdePrimario> `;`  *[sólo con `varclasica`]*  

<VarLocal> ::= **var** **idMetVar** `=` <ExpresionCompuesta>  

> Varias variables por sentencia, cada una con inicialización opcional:
> `int x,y,z = 10;`

<ListaDeclaradores> ::= **idMetVar** <InicializacionDeDeclarador> <RestoListaDeclaradores>  *[sólo con `varclasica`]*  
<RestoListaDeclaradores> ::= `,` **idMetVar** <InicializacionDeDeclarador> <RestoListaDeclaradores>  *[sólo con `varclasica`]*  
<RestoListaDeclaradores> ::= ε  *[sólo con `varclasica`]*  

<InicializacionDeDeclarador> ::= `=` <ExpresionCompuesta> | ε  *[sólo con `varclasica`]*  

<Return> ::= **return** <ExpresionOpcional>  

<ExpresionOpcional> ::= <Expresion> | ε  

<If> ::= **if** `(` <Expresion> `)` <Sentencia> <ElseOpcional>  
<ElseOpcional> ::= **else** <Sentencia> | ε  

<While> ::= **while** `(` <Expresion> `)` <Sentencia>  

> Fors! E2
> Una sola sentencia/expresión por separador. El `:` distingue el for de
> iteradores del estándar, y aparece justo después del declarador, así que el
> desempate es de un solo token.

<For> ::= **for** `(` <EncabezadoFor> `)` <Sentencia>  *[sólo con `for`]*  

<EncabezadoFor> ::= `;` <ExpresionOpcional> `;` <ExpresionOpcional>  *[sólo con `for`]*  
<EncabezadoFor> ::= **var** **idMetVar** <EncabezadoForTrasVar>  *[sólo con `for`]*  
<EncabezadoFor> ::= <TipoPrimitivo> <DimensionesOpcionales> **idMetVar** <EncabezadoForTrasDeclarador>  *[sólo con `for`]*  
<EncabezadoFor> ::= **idGen** <DimensionesOpcionales> **idMetVar** <EncabezadoForTrasDeclarador>  *[sólo con `for`]*  
<EncabezadoFor> ::= **idClase** <EncabezadoForDesdeIdClase>  *[sólo con `for`]*  
<EncabezadoFor> ::= <ExpresionSinIdClase> <ClausulasFinalesFor>  *[sólo con `for`]*  

<EncabezadoForDesdeIdClase> ::= <TipoGenericoOpcional> <DimensionesOpcionales> **idMetVar** <EncabezadoForTrasDeclarador>  *[sólo con `for`]*  
<EncabezadoForDesdeIdClase> ::= `.` **idMetVar** <ArgsActuales> <ColaDeExpresionDesdePrimario> <ClausulasFinalesFor>  *[sólo con `for`]*  

> El `:` distingue el for de iteradores del estándar, y está a un solo token.

<EncabezadoForTrasVar> ::= `:` <Expresion>  *[sólo con `for`]*  
<EncabezadoForTrasVar> ::= `=` <ExpresionCompuesta> <ClausulasFinalesFor>  *[sólo con `for`]*  

<EncabezadoForTrasDeclarador> ::= `:` <Expresion>  *[sólo con `for`]*  
<EncabezadoForTrasDeclarador> ::= <InicializacionDeDeclaradorFor> <ClausulasFinalesFor>  *[sólo con `for`]*  

<InicializacionDeDeclaradorFor> ::= `=` <ExpresionCompuesta> | ε  *[sólo con `for`]*  

> Una sola expresión por separador, como pide el logro.

<ClausulasFinalesFor> ::= `;` <ExpresionOpcional> `;` <ExpresionOpcional>  *[sólo con `for`]*  

> EXPRESIONES
> Las expresiones se organizan en capas, de menor a mayor precedencia:
> <Expresion> es la asignación (no asociativa, como en la gramática original);
> <ExpresionCondicional> el ternario; <ExpresionCompuesta> los operadores
> binarios, en un solo nivel como en el original; <ExpresionBasica> los
> operadores unarios prefijos; y <Operando> un literal, o una referencia con
> posfijo opcional.

<Expresion> ::= <ExpresionCondicional> <RestoExpresion>  

> El lado derecho es ExpresionCondicional y NO Expresion: la gramática original
> pone <ExpresionCompuesta> ahí, o sea que `a = b = c` NO es válido en MiniJava.

<RestoExpresion> ::= <OperadorAsignacion> <ExpresionCondicional> | ε  

<OperadorAsignacion> ::= `=`  

<ExpresionCondicional> ::= <ExpresionCompuesta> <RestoCondicional>  

> Con el logro apagado <RestoCondicional> queda en solo ε, o sea que
> <ExpresionCondicional> se vuelve un alias de <ExpresionCompuesta> y la capa
> desaparece sin dejar rastro. Asociativo a derecha, como en Java.

<RestoCondicional> ::= `?` <ExpresionCondicional> `:` <ExpresionCondicional>  *[sólo con `ternario`]*  
<RestoCondicional> ::= ε  

<ExpresionCompuesta> ::= <ExpresionBasica> <RestoExpresionCompuesta>  
<RestoExpresionCompuesta> ::= <OperadorBinario> <ExpresionBasica> <RestoExpresionCompuesta> | ε  

<OperadorBinario> ::= `||` | `&&` | `==` | `!=` | `<` | `>` | `<=` | `>=` | `+` | `-` | `*` | `/` | `%`  

<ExpresionBasica> ::= <OperadorUnario> <Operando>  
<ExpresionBasica> ::= <Operando>  

<OperadorUnario> ::= `+` | `-` | `!`  

<Operando> ::= <Primitivo>  
<Operando> ::= <Referencia> <PosfijoOpcional>  

> Va sobre <Referencia> y no sobre <Operando>: `5++` no tiene sentido.

<PosfijoOpcional> ::= `++`  *[sólo con `posfijo`]*  
<PosfijoOpcional> ::= `--`  *[sólo con `posfijo`]*  
<PosfijoOpcional> ::= ε  

> floatLiteral

<Primitivo> ::= **true** | **false** | **intLiteral** | **charLiteral** | **null**  
<Primitivo> ::= **floatLiteral**  *[sólo con `float`]*  

> --- Expresión que no puede empezar con idClase ----------------------------
> Se usa donde una expresión compite con una declaración de variable local
> (sentencia y encabezado del for). Sólo duplica las *cabezas*: todas las colas
> se comparten con la jerarquía normal, así que no hay lógica repetida.

<ExpresionSinIdClase> ::= <OperadorUnario> <Operando> <ColaTrasOperando>  
<ExpresionSinIdClase> ::= <Primitivo> <ColaTrasOperando>  
<ExpresionSinIdClase> ::= <PrimarioSinIdClase> <ColaDeExpresionDesdePrimario>  

<ColaTrasOperando> ::= <RestoExpresionCompuesta> <RestoCondicional> <RestoExpresion>  

<ColaDeExpresionDesdePrimario> ::= <RestoReferencia> <PosfijoOpcional> <ColaTrasOperando>  

> REFERENCIAS Y PRIMARIOS

<Referencia> ::= <Primario> <RestoReferencia>  

<RestoReferencia> ::= <Encadenado> <RestoReferencia>  
<RestoReferencia> ::= <AccesoArreglo> <RestoReferencia>  
<RestoReferencia> ::= ε  

<Encadenado> ::= `.` **idMetVar** <EncadenadoAtributoOMetodo>  

> Dedicado a este contexto: vacío = era <VarEncadenada>, con argumentos = era
> <MetodoEncadenado>.

<EncadenadoAtributoOMetodo> ::= <ArgsActuales> | ε  

<AccesoArreglo> ::= `[` <Expresion> `]`  

<Primario> ::= <LlamadaMetodoEstatico>  
<Primario> ::= <PrimarioSinIdClase>  

<LlamadaMetodoEstatico> ::= **idClase** `.` **idMetVar** <ArgsActuales>  

<PrimarioSinIdClase> ::= **this**  
<PrimarioSinIdClase> ::= **stringLiteral**  
<PrimarioSinIdClase> ::= <ParentizadaOLambda>  
<PrimarioSinIdClase> ::= <AccesoVarLlamadaOLambda>  
<PrimarioSinIdClase> ::= <Creacion>  

> Dedicado a este contexto: vacío = era <AccesoVar>, con argumentos = era
> <LlamadaMetodo>, con flecha = lambda de un parámetro sin paréntesis.

<AccesoVarLlamadaOLambda> ::= **idMetVar** <AccesoLlamadaOLambda>  
<AccesoLlamadaOLambda> ::= <ArgsActuales>  
<AccesoLlamadaOLambda> ::= `->` <CuerpoLambda>  *[sólo con `lambda`]*  
<AccesoLlamadaOLambda> ::= ε  

> MiniLambdas E2
> `(` abre tanto una expresión parentizada como la lista de parámetros de una
> lambda. Parece necesitar lookahead ilimitado (`(a,b,c) ->` vs `(a+b)`), pero
> NO lo necesita:
> - `( )`      sólo puede ser una lambda de cero parámetros;
> - `( e , …`  sólo puede ser una lambda: MiniJava no tiene operador coma;
> - `( e )`    se decide con el token que sigue al `)`: `->` es lambda,
> cualquier otro es una expresión parentizada.
> Con eso alcanza un token de lookahead en cada punto.

<ParentizadaOLambda> ::= `(` <ContenidoDeParentesis>  

<ContenidoDeParentesis> ::= `)` `->` <CuerpoLambda>  *[sólo con `lambda`]*  
<ContenidoDeParentesis> ::= <Expresion> <RestoDeParentesis>  

<RestoDeParentesis> ::= `)` <FinDeParentizadaOLambda>  
<RestoDeParentesis> ::= `,` <ListaParametrosLambda> `)` `->` <CuerpoLambda>  *[sólo con `lambda`]*  

<FinDeParentizadaOLambda> ::= `->` <CuerpoLambda>  *[sólo con `lambda`]*  
<FinDeParentizadaOLambda> ::= ε  

<ListaParametrosLambda> ::= **idMetVar** <RestoListaParametrosLambda>  *[sólo con `lambda`]*  
<RestoListaParametrosLambda> ::= `,` **idMetVar** <RestoListaParametrosLambda> | ε  *[sólo con `lambda`]*  

> Una única expresión, sin llaves.

<CuerpoLambda> ::= <Expresion>  *[sólo con `lambda`]*  

> CREACIÓN

<Creacion> ::= **new** <ArregloOInstancia>  

<ArregloOInstancia> ::= <TipoPrimitivo> <CreacionDeArreglo>  
<ArregloOInstancia> ::= **idGen** <CreacionDeArreglo>  
<ArregloOInstancia> ::= **idClase** <CreacionDesdeIdClase>  

<CreacionDesdeIdClase> ::= `<` <GenericoDeCreacion>  
<CreacionDesdeIdClase> ::= <CreacionDeArreglo>  
<CreacionDesdeIdClase> ::= <ArgsActuales>  

> el diamante `<>` sólo se admite acá, en la rama que va a
> ArgsActuales: `new C<>()` es válido y `new C<>[3]` no. La restricción del
> logro queda impuesta por la gramática, no delegada al semántico.

<GenericoDeCreacion> ::= `>` <ArgsActuales>  *[sólo con `generico`]*  
<GenericoDeCreacion> ::= <ArgumentoDeTipo> `>` <ArregloOInstanciaDeClase>  

<ArregloOInstanciaDeClase> ::= <CreacionDeArreglo>  
<ArregloOInstanciaDeClase> ::= <ArgsActuales>  

> `new int[]{1,2,3}`. Sin el logro:
> CreacionDeArreglo -> [ Expresion ] DimensionesConTamanioOpcional

<CreacionDeArreglo> ::= `[` <RestoCreacionDeArreglo>  
<RestoCreacionDeArreglo> ::= <Expresion> `]` <DimensionesConTamanioOpcional>  
<RestoCreacionDeArreglo> ::= `]` `{` <ListaExpsOpcional> `}`  *[sólo con `arrinit`]*  

<DimensionesConTamanio> ::= `[` <Expresion> `]` <DimensionesConTamanioOpcional>  
<DimensionesConTamanioOpcional> ::= <DimensionesConTamanio> | ε  

<ArgsActuales> ::= `(` <ListaExpsOpcional> `)`  

<ListaExpsOpcional> ::= <ListaExps> | ε  

<ListaExps> ::= <Expresion> <RestoListaExps>  
<RestoListaExps> ::= `,` <Expresion> <RestoListaExps> | ε  
