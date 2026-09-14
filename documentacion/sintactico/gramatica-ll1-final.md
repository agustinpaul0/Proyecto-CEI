# Gramática LL(1) final de MiniJava

Gramática corregida y **verificada** de MiniJava, en la notación BNF-extendida del PDF de la
cátedra. Incluye el núcleo más los 9 logros de la Etapa 2 y el carry-over de `float` de la
Etapa 1.

- El origen, sin tocar, está en [`gramatica-base-minijava.md`](gramatica-original-de-la-catedra.md).
- Para entender qué hace cada regla y qué estaba roto: [`guia-de-la-gramatica.md`](defectos-de-la-gramatica-original.md).
- El paso a paso de cada corrección: [`transformacion-gramatica.md`](transformacion-a-ll1-paso-a-paso.md).

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
bash herramientas/casos-gramatica.sh                                              # 69 casos
```

Estado actual:

- **111 no terminales, 228 producciones, 60 terminales.**
- **LL(1) en las 1024 combinaciones de logros**, incluida la de cero logros. Ningún logro depende
  de que otro esté prendido: eso es lo que hace que se puedan prender y apagar de a uno.
- **87 casos de aceptación y rechazo** pasan, cubriendo cada logro con su propia sintaxis y con
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

<InicializacionDeAtributo> ::= `=` <ExpresionCondicionalOLambda>  *[sólo con `atrinit`]*  
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
<SentenciaExpresion> ::= <ExpresionSinLambda>  *[sólo sin `varclasica`]*  

> Con el logro, `C x;` y `C.m();` compiten por idClase y hay que factorizarlo.

<Sentencia> ::= <ExpresionSinIdClase> `;`  *[sólo con `varclasica`]*  
<Sentencia> ::= <TipoPrimitivo> <DimensionesOpcionales> <ListaDeclaradores> `;`  *[sólo con `varclasica`]*  
<Sentencia> ::= **idGen** <DimensionesOpcionales> <ListaDeclaradores> `;`  *[sólo con `varclasica`]*  
<Sentencia> ::= **idClase** <SentenciaDesdeIdClase>  *[sólo con `varclasica`]*  

<SentenciaDesdeIdClase> ::= <TipoGenericoOpcional> <DimensionesOpcionales> <ListaDeclaradores> `;`  *[sólo con `varclasica`]*  
<SentenciaDesdeIdClase> ::= `.` **idMetVar** <ArgsActuales> <ColaDeExpresionDesdePrimario> `;`  *[sólo con `varclasica`]*  

<VarLocal> ::= **var** **idMetVar** `=` <ExpresionCondicionalOLambda>  

> Varias variables por sentencia, cada una con inicialización opcional:
> `int x,y,z = 10;`

<ListaDeclaradores> ::= **idMetVar** <InicializacionDeDeclarador> <RestoListaDeclaradores>  *[sólo con `varclasica`]*  
<RestoListaDeclaradores> ::= `,` **idMetVar** <InicializacionDeDeclarador> <RestoListaDeclaradores>  *[sólo con `varclasica`]*  
<RestoListaDeclaradores> ::= ε  *[sólo con `varclasica`]*  

<InicializacionDeDeclarador> ::= `=` <ExpresionCondicionalOLambda> | ε  *[sólo con `varclasica`]*  

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
<EncabezadoForTrasVar> ::= `=` <ExpresionCondicionalOLambda> <ClausulasFinalesFor>  *[sólo con `for`]*  

<EncabezadoForTrasDeclarador> ::= `:` <Expresion>  *[sólo con `for`]*  
<EncabezadoForTrasDeclarador> ::= <InicializacionDeDeclaradorFor> <ClausulasFinalesFor>  *[sólo con `for`]*  

<InicializacionDeDeclaradorFor> ::= `=` <ExpresionCondicionalOLambda> | ε  *[sólo con `for`]*  

> Una sola expresión por separador, como pide el logro.

<ClausulasFinalesFor> ::= `;` <ExpresionOpcional> `;` <ExpresionOpcional>  *[sólo con `for`]*  

> EXPRESIONES
> Las expresiones se organizan en capas, de menor a mayor precedencia:
> <Expresion> es la asignación (no asociativa, como en la gramática original);
> <ExpresionCondicional> el ternario; <ExpresionCompuesta> los operadores
> binarios, en un solo nivel como en el original; <ExpresionBasica> los
> operadores unarios prefijos; y <Operando> un literal, o una referencia con
> posfijo opcional.

> La lambda NO es una capa de esta jerarquía ni un <Primario>: es una
> alternativa del nivel MÁS ALTO, igual que en el JLS, que define
> `Expression: LambdaExpression | AssignmentExpression`. Por eso `->` no tiene
> precedencia contra ningún operador —no compite con ellos, vive arriba de
> todos—, y `1 + x -> x`, `- x -> x` y `a == x -> x` son inválidos, como en
> Java. Las dos cabezas que una lambda comparte con una expresión normal,
> `idMetVar` y `(`, se factorizan en <ExpresionDesdePrimarioOLambda>.

> Todo lado derecho de un `=` —el de la asignación y el de los cinco
> inicializadores— es una <ExpresionCondicionalOLambda> y no una <Expresion>.
> Eso preserva lo que la regla original conseguía usando <ExpresionCompuesta>:
> que `a = b = c` no sea válido. Y como con los logros de ternario y de lambda
> apagados <ExpresionCondicionalOLambda> es un alias de <ExpresionCompuesta>, el
> lenguaje base queda idéntico al de la cátedra.

<Expresion> ::= <ExpresionCondicionalOLambda> <RestoExpresion>  

> La <Expresion> de la gramática original: la que NO admite una lambda en la
> raíz. Se usa en los dos lugares donde Java tampoco la admite: adentro de una
> expresión parentizada que después va a ser operando, y como sentencia-
> expresión (`x -> x;` es «not a statement» en Java).

<ExpresionSinLambda> ::= <ExpresionCondicional> <RestoExpresion>  

> El lado derecho es ExpresionCondicionalOLambda y NO Expresion: la gramática
> original pone <ExpresionCompuesta> ahí, o sea que `a = b = c` NO es válido.
> Admite lambda porque el JLS define `Assignment: LeftHandSide = Expression`.

<RestoExpresion> ::= <OperadorAsignacion> <ExpresionCondicionalOLambda> | ε  

<OperadorAsignacion> ::= `=`  

> ---------------------------------------------------------------------------
> El nivel del JLS `LambdaExpression | ConditionalExpression`: admite lambda,
> no admite `=` en la raíz. Es el operando de los seis `=` y el tercer operando
> del ternario.

> Las cabezas que NO pueden empezar una lambda delegan en la jerarquía de
> siempre; las dos que sí —`idMetVar` y `(`— se factorizan. La cola se comparte
> con esa jerarquía, así que no hay lógica duplicada: sólo las cabezas.

<ExpresionCondicionalOLambda> ::= <OperadorUnario> <Operando> <ColaTrasOperandoSinAsignacion>  
<ExpresionCondicionalOLambda> ::= <Primitivo> <ColaTrasOperandoSinAsignacion>  
<ExpresionCondicionalOLambda> ::= <ExpresionDesdePrimarioOLambda>  

<ExpresionDesdePrimarioOLambda> ::= **idClase** `.` **idMetVar** <ArgsActuales> <ColaDesdePrimarioSinAsignacion>  
<ExpresionDesdePrimarioOLambda> ::= **this** <ColaDesdePrimarioSinAsignacion>  
<ExpresionDesdePrimarioOLambda> ::= **stringLiteral** <ColaDesdePrimarioSinAsignacion>  
<ExpresionDesdePrimarioOLambda> ::= <Creacion> <ColaDesdePrimarioSinAsignacion>  
<ExpresionDesdePrimarioOLambda> ::= **idMetVar** <AccesoLlamadaOLambda>  
<ExpresionDesdePrimarioOLambda> ::= <ParentizadaOLambda>  

<ExpresionCondicional> ::= <ExpresionCompuesta> <RestoCondicional>  

> Con el logro apagado <RestoCondicional> queda en solo ε, o sea que
> <ExpresionCondicional> se vuelve un alias de <ExpresionCompuesta> y la capa
> desaparece sin dejar rastro. Asociativo a derecha, como en Java.

> El operando del MEDIO es una <Expresion> y el de la derecha una
> <ExpresionCondicionalOLambda>, tal cual el JLS 15.25: en Java `a ? b = 1 : c`
> es válido, `a ? b : c = d` no lo es, y `a ? b : x -> x` sí —la producción del
> JLS es `? Expression : LambdaExpression`—. No rompe LL(1) porque
> Primeros(Expresion) no contiene `:`.

<RestoCondicional> ::= `?` <Expresion> `:` <ExpresionCondicionalOLambda>  *[sólo con `ternario`]*  
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

> NO admite lambda en la raíz, por la misma razón que <ExpresionSinLambda>: los
> dos contextos donde se usa son sentencia-expresión e inicialización del for,
> y en Java los dos exigen un StatementExpression, que nunca es una lambda.

<ExpresionSinIdClase> ::= <OperadorUnario> <Operando> <ColaTrasOperando>  
<ExpresionSinIdClase> ::= <Primitivo> <ColaTrasOperando>  
<ExpresionSinIdClase> ::= <PrimarioSinIdClase> <ColaDeExpresionDesdePrimario>  

> La cola se parte en dos para que el nivel <ExpresionCondicionalOLambda> pueda
> reusarla sin arrastrar el `=`, que ahí no corresponde.

<ColaTrasOperando> ::= <ColaTrasOperandoSinAsignacion> <RestoExpresion>  
<ColaTrasOperandoSinAsignacion> ::= <RestoExpresionCompuesta> <RestoCondicional>  

<ColaDeExpresionDesdePrimario> ::= <RestoReferencia> <PosfijoOpcional> <ColaTrasOperando>  
<ColaDesdePrimarioSinAsignacion> ::= <RestoReferencia> <PosfijoOpcional> <ColaTrasOperandoSinAsignacion>  

> REFERENCIAS Y PRIMARIOS
> Ningún <Primario> puede ser una lambda: ésa es exactamente la corrección de
> precedencia. Un <Primario> es un operando, y una lambda no puede serlo.

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
<PrimarioSinIdClase> ::= <Parentizada>  
<PrimarioSinIdClase> ::= <AccesoVarOLlamada>  
<PrimarioSinIdClase> ::= <Creacion>  

> El contenido es una <ExpresionSinLambda>: `(x -> x)` como OPERANDO no existe
> en Java —`(x -> x) + 1` y `(x -> x).m()` son «lambda expression not expected
> here»—. La lambda parentizada sí es válida cuando ella misma es toda la
> expresión, y ese caso lo cubre <ParentizadaOLambda>.

<Parentizada> ::= `(` <ExpresionSinLambda> `)`  

> Dedicado a este contexto: vacío = era <AccesoVar>, con argumentos = era
> <LlamadaMetodo>.

<AccesoVarOLlamada> ::= **idMetVar** <AccesoLlamada>  
<AccesoLlamada> ::= <ArgsActuales> | ε  

> MiniLambdas E2
> Las dos cabezas ambiguas, resueltas en el nivel más alto de la jerarquía.

> `(` abre tanto una expresión parentizada como la lista de parámetros de una
> lambda. Parece necesitar lookahead ilimitado (`(a,b,c) ->` vs `(a+b)`), pero
> NO lo necesita:
> - `( )`      sólo puede ser una lambda de cero parámetros;
> - `( e , …`  sólo puede ser una lambda: MiniJava no tiene operador coma;
> - `( e )`    se decide con el token que sigue al `)`: `->` es lambda,
> cualquier otro es una expresión parentizada.
> Con eso alcanza un token de lookahead en cada punto.

> Con flecha = lambda de un parámetro sin paréntesis; si no, es el <AccesoVar> o
> la <LlamadaMetodo> de siempre, con la cola de expresión que le corresponde.

<AccesoLlamadaOLambda> ::= `->` <CuerpoLambda>  *[sólo con `lambda`]*  
<AccesoLlamadaOLambda> ::= <AccesoLlamada> <ColaDesdePrimarioSinAsignacion>  

<ParentizadaOLambda> ::= `(` <ContenidoDeParentesis>  

<ContenidoDeParentesis> ::= `)` `->` <CuerpoLambda>  *[sólo con `lambda`]*  
<ContenidoDeParentesis> ::= <Expresion> <RestoDeParentesis>  

<RestoDeParentesis> ::= `)` <FinDeParentizadaOLambda>  
<RestoDeParentesis> ::= `,` <ListaParametrosLambda> `)` `->` <CuerpoLambda>  *[sólo con `lambda`]*  

<FinDeParentizadaOLambda> ::= `->` <CuerpoLambda>  *[sólo con `lambda`]*  
<FinDeParentizadaOLambda> ::= <ColaDesdePrimarioSinAsignacion>  

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

> El logro dice «como en Java» y sólo restringe DÓNDE (dentro de un `new`), no
> cuántas dimensiones. Java admite `new int[][]{{1,2},{3,4}}`, así que el
> inicializador es anidable: un elemento puede ser una expresión o, a su vez,
> otro inicializador.

<RestoCreacionDeArreglo> ::= `]` <DimensionesVaciasOpcional> <InicializadorDeArreglo>  *[sólo con `arrinit`]*  
<DimensionesVaciasOpcional> ::= `[` `]` <DimensionesVaciasOpcional> | ε  *[sólo con `arrinit`]*  
<InicializadorDeArreglo> ::= `{` <ListaElementosOpcional> `}`  *[sólo con `arrinit`]*  
<ListaElementosOpcional> ::= <ListaElementos> | ε  *[sólo con `arrinit`]*  
<ListaElementos> ::= <ElementoInicializador> <RestoListaElementos>  *[sólo con `arrinit`]*  
<RestoListaElementos> ::= `,` <ElementoInicializador> <RestoListaElementos> | ε  *[sólo con `arrinit`]*  
<ElementoInicializador> ::= <Expresion> | <InicializadorDeArreglo>  *[sólo con `arrinit`]*  

<DimensionesConTamanio> ::= `[` <Expresion> `]` <DimensionesConTamanioOpcional>  
<DimensionesConTamanioOpcional> ::= <DimensionesConTamanio> | ε  

> ARGUMENTOS ACTUALES
> Sección propia y no parte de la creación: la lista de argumentos de una
> llamada la usan por igual el `new`, el encadenado, la llamada estática y el
> encabezado del for.

<ArgsActuales> ::= `(` <ListaExpsOpcional> `)`  

<ListaExpsOpcional> ::= <ListaExps> | ε  

<ListaExps> ::= <Expresion> <RestoListaExps>  
<RestoListaExps> ::= `,` <Expresion> <RestoListaExps> | ε  
