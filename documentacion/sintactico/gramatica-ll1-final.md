> Gramática LL(1) FINAL de MiniJava.

> python3 herramientas/primeros.py gramatica/minijava-ll1-logros.g

> Ésta es la ÚNICA gramática del compilador. No hay producciones que aparezcan
> y desaparezcan según una configuración: lo que está escrito acá es
> exactamente lo que el analizador reconoce, siempre.

> El núcleo sin logros vive aparte, en minijava-ll1.g, y no lo consume el
> compilador: existe sólo para que equivalencia_de_lenguajes.py demuestre que
> genera el mismo lenguaje que la gramática de la cátedra (minijava-base.g).

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

<InicializacionDeAtributo> ::= `=` <ExpresionCondicional>  
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

<TipoGenericoOpcional> ::= `<` <InstanciadoOParametrico> `>` | ε  

> la recursión por <TipoGenericoOpcional> es la que habilita el
> anidamiento `List<List<Persona>>`.

<InstanciadoOParametrico> ::= **idGen**  
<InstanciadoOParametrico> ::= **idClase** <TipoGenericoOpcional>  

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

<VarLocal> ::= **var** **idMetVar** `=` <ExpresionCondicional>  

> Varias variables por sentencia, cada una con inicialización opcional:
> `int x,y,z = 10;`

<ListaDeclaradores> ::= **idMetVar** <InicializacionDeDeclarador> <RestoListaDeclaradores>  
<RestoListaDeclaradores> ::= `,` **idMetVar** <InicializacionDeDeclarador> <RestoListaDeclaradores>  
<RestoListaDeclaradores> ::= ε  

<InicializacionDeDeclarador> ::= `=` <ExpresionCondicional> | ε  

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
<EncabezadoForTrasVar> ::= `=` <ExpresionCondicional> <ClausulasFinalesFor>  

<EncabezadoForTrasDeclarador> ::= `:` <Expresion>  
<EncabezadoForTrasDeclarador> ::= <InicializacionDeDeclaradorFor> <ClausulasFinalesFor>  

<InicializacionDeDeclaradorFor> ::= `=` <ExpresionCondicional> | ε  

> Una sola expresión por separador.

<ClausulasFinalesFor> ::= `;` <ExpresionOpcional> `;` <ExpresionOpcional>  

> EXPRESIONES
> Las expresiones se organizan en capas, de menor a mayor precedencia:
> <Expresion> es la asignación (no asociativa, como en la gramática original);
> <ExpresionCondicional> el ternario; <ExpresionCompuesta> los operadores
> binarios, en un solo nivel como en el original; <ExpresionBasica> los
> operadores unarios prefijos; y <Operando> un literal, o una referencia con
> posfijo opcional.

> La regla original es `Expresion -> ExpresionCompuesta = ExpresionCompuesta |
> ExpresionCompuesta`, factorizada. El lado izquierdo del `=` es una
> <ExpresionCompuesta>, como en la base: `a ? b : c = d` no deriva, igual que
> en Java.

<Expresion> ::= <ExpresionCompuesta> <RestoExpresion>  

> El lado derecho es ExpresionCondicional y NO Expresion: la gramática original
> pone <ExpresionCompuesta> ahí, o sea que `a = b = c` NO es válido. Sin `=`, lo
> que puede seguir es el ternario.

<RestoExpresion> ::= <OperadorAsignacion> <ExpresionCondicional>  
<RestoExpresion> ::= <RestoCondicional>  

<OperadorAsignacion> ::= `=`  

> Todo lado derecho de un `=` —el de la asignación y el de los cinco
> inicializadores— es una <ExpresionCondicional>: admite el ternario y no admite
> otro `=`.

<ExpresionCondicional> ::= <ExpresionCompuesta> <RestoCondicional>  

> Asociativo a derecha, como en Java.

> El operando del MEDIO es una <Expresion> y el de la derecha una
> <ExpresionCondicional>, tal cual el JLS 15.25: en Java `a ? b = 1 : c` es
> válido y `a ? b : c = d` no lo es. No rompe LL(1) porque Primeros(Expresion)
> no contiene `:`.

<RestoCondicional> ::= `?` <Expresion> `:` <ExpresionCondicional>  
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

<PosfijoOpcional> ::= `++`  
<PosfijoOpcional> ::= `--`  
<PosfijoOpcional> ::= ε  

<Primitivo> ::= **true** | **false** | **intLiteral** | **charLiteral** | **null**  
<Primitivo> ::= **floatLiteral**  

> --- Expresión que no puede empezar con idClase ----------------------------
> Se usa donde una expresión compite con una declaración de variable local
> (sentencia y encabezado del for). Sólo duplica las *cabezas*: todas las colas
> se comparten con la jerarquía normal, así que no hay lógica repetida.

<ExpresionSinIdClase> ::= <OperadorUnario> <Operando> <ColaTrasOperando>  
<ExpresionSinIdClase> ::= <Primitivo> <ColaTrasOperando>  
<ExpresionSinIdClase> ::= <PrimarioSinIdClase> <ColaDeExpresionDesdePrimario>  

<ColaTrasOperando> ::= <RestoExpresionCompuesta> <RestoExpresion>  

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
<PrimarioSinIdClase> ::= <ExpresionParentizada>  
<PrimarioSinIdClase> ::= <AccesoVarOLlamadaMetodo>  
<PrimarioSinIdClase> ::= <Creacion>  

<ExpresionParentizada> ::= `(` <Expresion> `)`  

> AccesoVar (idMetVar) y LlamadaMetodo (idMetVar ArgsActuales) comparten todo el
> prefijo.

<AccesoVarOLlamadaMetodo> ::= **idMetVar** <AccesoOLlamada>  

> Dedicado a este contexto: vacío = era <AccesoVar>, con argumentos = era
> <LlamadaMetodo>.

<AccesoOLlamada> ::= <ArgsActuales> | ε  

> CREACIÓN

<Creacion> ::= **new** <ArregloOInstancia>  

<ArregloOInstancia> ::= <TipoPrimitivo> <CreacionDeArreglo>  
<ArregloOInstancia> ::= **idGen** <CreacionDeArreglo>  
<ArregloOInstancia> ::= **idClase** <CreacionDesdeIdClase>  

<CreacionDesdeIdClase> ::= `<` <GenericoDeCreacion>  
<CreacionDesdeIdClase> ::= <ArregloOInstanciaDeClase>  

> el diamante `<>` sólo se admite acá, en la rama que va a
> ArgsActuales: `new C<>()` es válido y `new C<>[3]` no. La restricción queda
> impuesta por la gramática, no delegada al semántico.

<GenericoDeCreacion> ::= `>` <ArgsActuales>  
<GenericoDeCreacion> ::= <InstanciadoOParametrico> `>` <ArregloOInstanciaDeClase>  

<ArregloOInstanciaDeClase> ::= <CreacionDeArreglo>  
<ArregloOInstanciaDeClase> ::= <ArgsActuales>  

> Con tamaño es la creación de MiniJava base: `CrearArreglo -> new TipoBase
> DimensionesConTamanio`, con toda dimensión con su `[ Expresion ]`. Sin tamaño es
> la forma de Java con inicializador (JLS 15.10.1): ninguna dimensión lleva tamaño,
> así que `new int[][]{{1,2},{3,4}}` es válido y `new int[2][2]{{1,2},{3,4}}` no.

<CreacionDeArreglo> ::= `[` <RestoCreacionDeArreglo>  

<RestoCreacionDeArreglo> ::= <Expresion> `]` <DimensionesConTamanioOpcional>  
<RestoCreacionDeArreglo> ::= `]` <DimensionesOpcionales> <InicializadorDeArreglo>  

<DimensionesConTamanio> ::= `[` <Expresion> `]` <DimensionesConTamanioOpcional>  
<DimensionesConTamanioOpcional> ::= <DimensionesConTamanio> | ε  

> El inicializador es anidable: un elemento puede ser una expresion o, a su vez,
> otro inicializador. Solo es alcanzable desde <Creacion>, que es lo unico que el
> logro restringe sobre donde puede aparecer. Admite la coma final, como en Java
> (JLS 10.6): `{1, 2,}` y `{,}` son validos.

<InicializadorDeArreglo> ::= `{` <ListaElementosOpcional> `}`  
<ListaElementosOpcional> ::= <ListaElementos> | `,` | ε  
<ListaElementos> ::= <ElementoInicializador> <RestoListaElementos>  
<RestoListaElementos> ::= `,` <ElementosTrasComa> | ε  
<ElementosTrasComa> ::= <ElementoInicializador> <RestoListaElementos> | ε  
<ElementoInicializador> ::= <Expresion> | <InicializadorDeArreglo>  

> ARGUMENTOS ACTUALES
> Sección propia y no parte de la creación: la lista de argumentos de una
> llamada la usan por igual el `new`, el encadenado, la llamada estática y el
> encabezado del for.

<ArgsActuales> ::= `(` <ListaExpsOpcional> `)`  

<ListaExpsOpcional> ::= <ListaExps> | ε  

<ListaExps> ::= <Expresion> <RestoListaExps>  
<RestoListaExps> ::= `,` <Expresion> <RestoListaExps> | ε  
