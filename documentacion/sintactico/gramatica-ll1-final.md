# Gramática LL(1) final de MiniJava

Gramática corregida y **verificada** de MiniJava, en la notación BNF-extendida del PDF de la
cátedra. Es la **única** gramática del compilador, y describe MiniJava con todos los logros de la
Etapa 1 y de la Etapa 2 incorporados: ninguna producción aparece o desaparece según una
configuración.

- El origen, sin tocar, está en [`gramatica-base-minijava.md`](gramatica-original-de-la-catedra.md).
- Para entender qué hace cada regla y qué estaba roto: [`guia-de-la-gramatica.md`](defectos-de-la-gramatica-original.md).
- El paso a paso de cada corrección: [`transformacion-gramatica.md`](transformacion-a-ll1-paso-a-paso.md).

**Este documento se genera** desde `compilador/gramatica/minijava-ll1-final.g`, que es el mismo
archivo que verifica el script — no se edita a mano, para que no puedan divergir:

```bash
cd compilador
python3 herramientas/gramatica_a_markdown.py gramatica/minijava-ll1-final.g --sin-cabecera
```
<Inicial> ::= <ListaClases> **eof**  

<ListaClases> ::= <Clase> <ListaClases> | <Interfaz> <ListaClases> | ε  

<Clase> ::= **class** **idClase** <GenericidadOpcional> <HerenciaOpcional> `{` <ListaMiembros> `}`  

<Interfaz> ::= **interface** **idClase** <GenericidadOpcional> <ExtensionOpcional> `{` <ListaMetodosInterfaz> `}`  

> Una Clase o Interfaz sigue limitada a UN único parámetro de tipo, también con
> la genericidad avanzada.

<GenericidadOpcional> ::= `<` **idGen** `>` | ε  

<HerenciaOpcional> ::= **extends** <TipoReferencia> | **implements** <TipoReferencia> | ε  

<ExtensionOpcional> ::= **extends** <TipoReferencia> | ε  

<ListaMiembros> ::= <Miembro> <ListaMiembros> | ε  

<ListaMetodosInterfaz> ::= <MetodoInterfaz> <ListaMetodosInterfaz> | ε  

> MIEMBROS
> La visibilidad se factoriza al frente de los tres tipos de miembro. Eso hace
> que el `public` del <Constructor> original quede absorbido por <Visibilidad>, y
> que <Constructor> pase a competir con <Tipo> por el token idClase: `C(){}`
> (constructor) vs `C x;` (atributo de tipo C).

<Miembro> ::= <Visibilidad> <MiembroTrasVisibilidad>  
<Miembro> ::= **static** <VisibilidadOpcional> <TipoMetodo> <Metodo>  
<Miembro> ::= <MiembroSinModificadores>  

> Los dos ordenes valen, como en Java: `public static` y `static public`. Que la
> visibilidad aparezca a un solo lado de `static` es lo que impide repetir el
> modificador —`public private void m(){}` no deriva—, sin necesitar un chequeo
> fuera de la gramatica.

<MiembroTrasVisibilidad> ::= **static** <TipoMetodo> <Metodo>  
<MiembroTrasVisibilidad> ::= <MiembroSinModificadores>  

<Visibilidad> ::= **public** | **private**  

<VisibilidadOpcional> ::= <Visibilidad> | ε  

<MiembroSinModificadores> ::= **void** <Metodo>  
<MiembroSinModificadores> ::= <TipoPrimitivo> <DimensionesOpcionales> **idMetVar** <AtributoOMetodo>  
<MiembroSinModificadores> ::= **idGen** <DimensionesOpcionales> **idMetVar** <AtributoOMetodo>  
<MiembroSinModificadores> ::= **idClase** <MiembroDesdeIdClase>  

> El NT insertado que decide entre constructor y miembro con tipo de referencia.
> Con visibilidad el constructor ya no lleva `public` propio, así que compite
> con el atributo/método de tipo referencia por el token idClase.

<MiembroDesdeIdClase> ::= <CuerpoDeMetodo>  
<MiembroDesdeIdClase> ::= <TipoGenericoOpcional> <DimensionesOpcionales> **idMetVar** <AtributoOMetodo>  

> El NT insertado que decide entre atributo y método.

<AtributoOMetodo> ::= <InicializacionDeAtributo> `;`  
<AtributoOMetodo> ::= <CuerpoDeMetodo>  

> NT dedicado y no compartido con la inicialización de una variable local: son
> construcciones distintas y el semántico las trata distinto.

<InicializacionDeAtributo> ::= `=` <ExpresionCondicionalOLambda>  
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
> original—. O sea que un `private` acá sería siempre
> la forma que Java rechaza.

<MetodoInterfaz> ::= <PublicOpcional> <TipoMetodo> **idMetVar** <ArgsFormales> `;`  
<PublicOpcional> ::= **public**  
<PublicOpcional> ::= ε  

<TipoMetodo> ::= <Tipo> | **void**  

> TIPOS

<Tipo> ::= <TipoBase> <DimensionesOpcionales>  

<TipoBase> ::= <TipoPrimitivo> | <TipoReferencia> | **idGen**  

<DimensionesOpcionales> ::= `[` `]` <DimensionesOpcionales> | ε  

<TipoReferencia> ::= **idClase** <TipoGenericoOpcional>  

> `float` es token del léxico desde la Etapa 1 pero no estaba en la
> gramática; sin esta alternativa el `float` del léxico queda inutilizable.

<TipoPrimitivo> ::= **boolean** | **char** | **int**  
<TipoPrimitivo> ::= **float**  

> En POSICIÓN DE TIPO no se admite el diamante: `List<> x;` es inválido.

<TipoGenericoOpcional> ::= `<` <ArgumentoDeTipo> `>` | ε  

> la recursión por <TipoGenericoOpcional> es la que habilita el
> anidamiento `List<List<Persona>>`.

<ArgumentoDeTipo> ::= **idGen**  
<ArgumentoDeTipo> ::= **idClase** <TipoGenericoOpcional>  

<ArgsFormales> ::= `(` <ListaArgsFormalesOpcional> `)`  

<ListaArgsFormalesOpcional> ::= <ListaArgsFormales> | ε  

<ListaArgsFormales> ::= <ArgFormal> <RestoListaArgsFormales>  
<RestoListaArgsFormales> ::= `,` <ArgFormal> <RestoListaArgsFormales> | ε  

<ArgFormal> ::= <Tipo> **idMetVar**  

> SENTENCIAS
> Una declaración local puede empezar con idClase (`C x;`), y una
> sentencia-expresión también (`C.m();`). Se factoriza ese idClase y el
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
<Sentencia> ::= <For>  

> Sin variables locales clásicas, una sentencia-expresión puede empezar con
> idClase sin competir con nada: es la regla original.

<Sentencia> ::= <ExpresionSinIdClase> `;`  
<Sentencia> ::= <TipoPrimitivo> <DimensionesOpcionales> <ListaDeclaradores> `;`  
<Sentencia> ::= **idGen** <DimensionesOpcionales> <ListaDeclaradores> `;`  
<Sentencia> ::= **idClase** <SentenciaDesdeIdClase>  

<SentenciaDesdeIdClase> ::= <TipoGenericoOpcional> <DimensionesOpcionales> <ListaDeclaradores> `;`  
<SentenciaDesdeIdClase> ::= `.` **idMetVar** <ArgsActuales> <ColaDeExpresionDesdePrimario> `;`  

<VarLocal> ::= **var** **idMetVar** `=` <ExpresionCondicionalOLambda>  

> Varias variables por sentencia, cada una con inicialización opcional:
> `int x,y,z = 10;`

<ListaDeclaradores> ::= **idMetVar** <InicializacionDeDeclarador> <RestoListaDeclaradores>  
<RestoListaDeclaradores> ::= `,` **idMetVar** <InicializacionDeDeclarador> <RestoListaDeclaradores>  
<RestoListaDeclaradores> ::= ε  

<InicializacionDeDeclarador> ::= `=` <ExpresionCondicionalOLambda> | ε  

<Return> ::= **return** <ExpresionOpcional>  

<ExpresionOpcional> ::= <Expresion> | ε  

<If> ::= **if** `(` <Expresion> `)` <Sentencia> <ElseOpcional>  
<ElseOpcional> ::= **else** <Sentencia> | ε  

<While> ::= **while** `(` <Expresion> `)` <Sentencia>  

> Fors! E2
> Una sola sentencia/expresión por separador. El `:` distingue el for de
> iteradores del estándar, y aparece justo después del declarador, así que el
> desempate es de un solo token.

<For> ::= **for** `(` <EncabezadoFor> `)` <Sentencia>  

<EncabezadoFor> ::= `;` <ExpresionOpcional> `;` <ExpresionOpcional>  
<EncabezadoFor> ::= **var** **idMetVar** <EncabezadoForTrasVar>  
<EncabezadoFor> ::= <TipoPrimitivo> <DimensionesOpcionales> **idMetVar** <EncabezadoForTrasDeclarador>  
<EncabezadoFor> ::= **idGen** <DimensionesOpcionales> **idMetVar** <EncabezadoForTrasDeclarador>  
<EncabezadoFor> ::= **idClase** <EncabezadoForDesdeIdClase>  
<EncabezadoFor> ::= <ExpresionSinIdClase> <ClausulasFinalesFor>  

<EncabezadoForDesdeIdClase> ::= <TipoGenericoOpcional> <DimensionesOpcionales> **idMetVar** <EncabezadoForTrasDeclarador>  
<EncabezadoForDesdeIdClase> ::= `.` **idMetVar** <ArgsActuales> <ColaDeExpresionDesdePrimario> <ClausulasFinalesFor>  

> El `:` distingue el for de iteradores del estándar, y está a un solo token.

<EncabezadoForTrasVar> ::= `:` <Expresion>  
<EncabezadoForTrasVar> ::= `=` <ExpresionCondicionalOLambda> <ClausulasFinalesFor>  

<EncabezadoForTrasDeclarador> ::= `:` <Expresion>  
<EncabezadoForTrasDeclarador> ::= <InicializacionDeDeclaradorFor> <ClausulasFinalesFor>  

<InicializacionDeDeclaradorFor> ::= `=` <ExpresionCondicionalOLambda> | ε  

> Una sola expresión por separador.

<ClausulasFinalesFor> ::= `;` <ExpresionOpcional> `;` <ExpresionOpcional>  

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
> que `a = b = c` no sea válido.

<Expresion> ::= <ExpresionCondicionalOLambda> <RestoExpresion>  

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

> Asociativo a derecha, como en Java.

> El operando del MEDIO es una <Expresion> y el de la derecha una
> <ExpresionCondicionalOLambda>, tal cual el JLS 15.25: en Java `a ? b = 1 : c`
> es válido, `a ? b : c = d` no lo es, y `a ? b : x -> x` sí —la producción del
> JLS es `? Expression : LambdaExpression`—. No rompe LL(1) porque
> Primeros(Expresion) no contiene `:`.

<RestoCondicional> ::= `?` <Expresion> `:` <ExpresionCondicionalOLambda>  
<RestoCondicional> ::= ε  

<RestoExpresionCompuesta> ::= <OperadorBinario> <ExpresionBasica> <RestoExpresionCompuesta> | ε  

<OperadorBinario> ::= `||` | `&&` | `==` | `!=` | `<` | `>` | `<=` | `>=` | `+` | `-` | `*` | `/` | `%`  

<ExpresionBasica> ::= <OperadorUnario> <Operando>  
<ExpresionBasica> ::= <Operando>  

<OperadorUnario> ::= `+` | `-` | `!`  

<Operando> ::= <Primitivo>  
<Operando> ::= <Referencia> <PosfijoOpcional>  

> Va sobre <Referencia> y no sobre <Operando>: `5++` no tiene sentido.

<PosfijoOpcional> ::= `++`  
<PosfijoOpcional> ::= `--`  
<PosfijoOpcional> ::= ε  

<Primitivo> ::= **true** | **false** | **intLiteral** | **charLiteral** | **null**  
<Primitivo> ::= **floatLiteral**  

> --- Expresión que no puede empezar con idClase ----------------------------
> Se usa donde una expresión compite con una declaración de variable local
> (sentencia y encabezado del for). Sólo duplica las *cabezas*: todas las colas
> se comparten con la jerarquía normal, así que no hay lógica repetida.

> NO admite lambda en la RAIZ: los dos contextos donde se usa son sentencia-
> expresion e inicializacion del for, y MiniJava base restringe la sentencia a
> `<Asignacion> | <Llamada>`, no a una <Expresion> cualquiera. Una lambda sí
> puede aparecer adentro, via `( Expresion )`, igual que en la base.

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

> El contenido es una <Expresion> completa, lambda incluida, tal cual la regla
> original: `ExpresionParentizada -> ( Expresion )`, y `Primario ->
> ExpresionParentizada`. MiniJava base ya define que cualquier expresion
> parentizada es un primario y que un primario arrastra cadena, asi que
> `(x -> y).m()` y `(x -> y) + 1` se derivan solos.

> DIVERGE de Java, que los rechaza con «lambda expression not expected here»
> porque ahi la lambda es alternativa de Expression y nunca un Primary. Se
> eligio respetar el molde de la gramatica base antes que la forma de Java: la
> base define esta estructura, y el criterio del proyecto es no despegarse de
> lo que la base ya dictamino.

<Parentizada> ::= `(` <Expresion> `)`  

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

<AccesoLlamadaOLambda> ::= `->` <CuerpoLambda>  
<AccesoLlamadaOLambda> ::= <AccesoLlamada> <ColaDesdePrimarioSinAsignacion>  

<ParentizadaOLambda> ::= `(` <ContenidoDeParentesis>  

<ContenidoDeParentesis> ::= `)` `->` <CuerpoLambda>  
<ContenidoDeParentesis> ::= <Expresion> <RestoDeParentesis>  

<RestoDeParentesis> ::= `)` <FinDeParentizadaOLambda>  
<RestoDeParentesis> ::= `,` <ListaParametrosLambda> `)` `->` <CuerpoLambda>  

<FinDeParentizadaOLambda> ::= `->` <CuerpoLambda>  
<FinDeParentizadaOLambda> ::= <ColaDesdePrimarioSinAsignacion>  

<ListaParametrosLambda> ::= **idMetVar** <RestoListaParametrosLambda>  
<RestoListaParametrosLambda> ::= `,` **idMetVar** <RestoListaParametrosLambda> | ε  

> Una única expresión, sin llaves.

<CuerpoLambda> ::= <Expresion>  

> CREACIÓN

<Creacion> ::= **new** <ArregloOInstancia>  

<ArregloOInstancia> ::= <TipoPrimitivo> <CreacionDeArreglo>  
<ArregloOInstancia> ::= **idGen** <CreacionDeArreglo>  
<ArregloOInstancia> ::= **idClase** <CreacionDesdeIdClase>  

<CreacionDesdeIdClase> ::= `<` <GenericoDeCreacion>  
<CreacionDesdeIdClase> ::= <CreacionDeArreglo>  
<CreacionDesdeIdClase> ::= <ArgsActuales>  

> el diamante `<>` sólo se admite acá, en la rama que va a
> ArgsActuales: `new C<>()` es válido y `new C<>[3]` no. La restricción queda
> impuesta por la gramática, no delegada al semántico.

<GenericoDeCreacion> ::= `>` <ArgsActuales>  
<GenericoDeCreacion> ::= <ArgumentoDeTipo> `>` <ArregloOInstanciaDeClase>  

<ArregloOInstanciaDeClase> ::= <CreacionDeArreglo>  
<ArregloOInstanciaDeClase> ::= <ArgsActuales>  

> Toda dimension de una creacion conserva su `[ Expresion ]`, que es la forma de
> MiniJava base: `CrearArreglo -> new TipoBase DimensionesConTamanio` con
> `DimensionesConTamanio -> [ Expresion ] DimensionesConTamanio | [ Expresion ]`.
> El logro de inicializacion NO afloja esa exigencia: le cuelga el inicializador
> al final. Asi `new int[2][2]{{1,2},{3,4}}` es valido y `new int[]{1,2,3}` no.

> Esto DIVERGE de Java, que prohibe tamanio e inicializador juntos (JLS 15.10.1,
> verificado con javac: «array creation with both dimension expression and
> initialization is illegal»). Es una decision deliberada: se prefirio no ampliar
> la forma de las dimensiones que MiniJava ya tenia definida. Revertirla es
> cambiar CreacionDeArreglo y nada mas.

<CreacionDeArreglo> ::= <DimensionesConTamanio> <InicializadorDeArregloOpcional>  

<DimensionesConTamanio> ::= `[` <Expresion> `]` <DimensionesConTamanioOpcional>  
<DimensionesConTamanioOpcional> ::= <DimensionesConTamanio> | ε  

> El inicializador es anidable: un elemento puede ser una expresion o, a su vez,
> otro inicializador. Solo es alcanzable desde <Creacion>, que es lo unico que el
> logro restringe sobre donde puede aparecer.

<InicializadorDeArregloOpcional> ::= <InicializadorDeArreglo> | ε  
<InicializadorDeArreglo> ::= `{` <ListaElementosOpcional> `}`  
<ListaElementosOpcional> ::= <ListaElementos> | ε  
<ListaElementos> ::= <ElementoInicializador> <RestoListaElementos>  
<RestoListaElementos> ::= `,` <ElementoInicializador> <RestoListaElementos> | ε  
<ElementoInicializador> ::= <Expresion> | <InicializadorDeArreglo>  

> ARGUMENTOS ACTUALES
> Sección propia y no parte de la creación: la lista de argumentos de una
> llamada la usan por igual el `new`, el encadenado, la llamada estática y el
> encabezado del for.

<ArgsActuales> ::= `(` <ListaExpsOpcional> `)`  

<ListaExpsOpcional> ::= <ListaExps> | ε  

<ListaExps> ::= <Expresion> <RestoListaExps>  
<RestoListaExps> ::= `,` <Expresion> <RestoListaExps> | ε  
