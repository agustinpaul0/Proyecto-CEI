# Guía de lectura de la gramática de MiniJava

Recorrido regla por regla de la gramática original de la cátedra: **para qué sirve cada una, por
qué está escrita así, y dónde está rota**. Está pensada para leerse de corrido antes de mirar las
transformaciones, así que no supone nada previo más allá de saber qué es un terminal y un no
terminal.

- La gramática literal está en [`gramatica-base-minijava.md`](gramatica-base-minijava.md).
- Las correcciones, paso a paso, en [`transformacion-gramatica.md`](transformacion-gramatica.md).
- La gramática final LL(1) con los logros, en
  [`gramatica-minijava.md`](gramatica-minijava.md).

Los defectos van marcados así a medida que aparecen:

> ⚠️ **Conflicto LL(1)** — el parser no puede elegir producción con un token de lookahead. La
> cadena tiene **un solo** árbol; lo que falla es la predicción. Se arregla y el lenguaje no
> cambia.

> 🔴 **Ambigüedad** — hay cadenas con **dos árboles distintos**. Es un defecto del lenguaje
> descripto, no del parser. Hay que elegir cuál de los dos significados vale.

Son cosas distintas y conviene no mezclarlas: de los 16 conflictos de esta gramática, **sólo 2
son ambigüedades**.

---

## 1. La estructura de un programa

### `<Inicial> ::= <ListaClases> eof`

El punto de entrada. Un programa MiniJava es una lista de clases e interfaces seguida del fin de
archivo.

**Por qué está escrita así:** el `eof` explícito no es decoración. Es el `$` que la teoría pone
al final del símbolo inicial (Clase 5, slide 3) y cumple una función concreta: obliga al parser a
consumir **todo** el archivo. Sin él, un programa con basura después de la última clase se
aceptaría, porque `<ListaClases>` puede derivar a vacío y el parser terminaría contento dejando
tokens sin leer. En el léxico ya implementado, `eof` es un token real con lexema `$`.

### `<ListaClases> ::= <Clase> <ListaClases> | <Interfaz> <ListaClases> | ε`

Cero o más clases o interfaces, en cualquier orden.

**Por qué está escrita así:** es **recursión a derecha** (el `<ListaClases>` está al final), que
es la forma natural de escribir «cero o más» en una gramática pensada para análisis descendente.
La versión con recursión a izquierda (`<ListaClases> ::= <ListaClases> <Clase>`) generaría el
mismo lenguaje pero sería inservible para un parser recursivo. El `ε` permite el programa vacío.

Los Primeros son `{class}`, `{interface}` y `{ε}`: disjuntos. Esta regla está bien.

### `<Clase>` y `<Interfaz>`

```
<Clase>    ::= class idClase <GenericidadOpcional> <HerenciaOpcional> { <ListaMiembros> }
<Interfaz> ::= interface idClase <GenericidadOpcional> <ExtensionOpcional> { <ListaMetodosInterfaz> }
```

Casi simétricas, con tres diferencias deliberadas:

1. **La interfaz sólo puede `extends`, la clase puede `extends` o `implements`.** Por eso hay dos
   no terminales distintos (`<HerenciaOpcional>` y `<ExtensionOpcional>`) en vez de uno
   compartido: la restricción está impuesta *por la gramática*, no delegada al semántico.
2. **La interfaz sólo contiene métodos** (`<ListaMetodosInterfaz>`), la clase contiene miembros
   de los tres tipos.
3. Ambas usan `idClase`, o sea que léxicamente una interfaz se nombra igual que una clase.

**Nota:** `extends` acepta un `<TipoReferencia>` en los dos casos, así que sintácticamente
`class A extends UnaInterfaz` es válido. Que la clase padre sea realmente una clase es un chequeo
**semántico**.

### `<GenericidadOpcional> ::= < idGen > | ε`

El parámetro de tipo de una clase o interfaz genérica: `class Lista<T> { … }`.

**Por qué está escrita así:** un **único** `idGen`, no una lista. MiniJava limita a un solo
parámetro de tipo, y el logro *Genericidad Avanzada E2* lo dice explícitamente: *«una Clase o
Interfaz continúa limitada a un único Parámetro de Tipo»*. La restricción es de la gramática. El
`ε` cubre la clase no genérica.

### `<HerenciaOpcional> ::= extends <TipoReferencia> | implements <TipoReferencia> | ε`

**Por qué está escrita así:** una clase hereda de **a lo sumo una** cosa, y no puede hacer las
dos a la vez. En Java se puede `extends A implements B, C`; en MiniJava no. Nuevamente, la
restricción está en la forma de la regla.

### `<ListaMiembros>` y `<ListaMetodosInterfaz>`

Mismo patrón de «cero o más» con recursión a derecha. El `ε` permite la clase vacía.

---

## 2. Los miembros de una clase

Acá está el primer nudo real.

### `<Miembro> ::= <Atributo> | <Metodo> | <Constructor>`

Los tres tipos de miembro.

```
<Atributo>    ::= <Tipo> idMetVar ;
<Metodo>      ::= <ModificadorOpcional> <TipoMetodo> idMetVar <ArgsFormales> <Bloque>
<Constructor> ::= public idClase <ArgsFormales> <Bloque>
```

**Cómo leerlas:**

- Un **atributo** es un tipo, un nombre y un punto y coma: `int contador;`. Sin inicialización —
  eso es justamente el logro *Atributos Inicializados*.
- Un **método** puede ser `static`, tiene un tipo de retorno (que puede ser `void`), un nombre,
  argumentos y un cuerpo.
- Un **constructor** se reconoce porque arranca con `public` y su nombre es un `idClase`, no un
  `idMetVar`. Ese `public` **no es un modificador de visibilidad**: es lo que sintácticamente
  distingue al constructor de todo lo demás. Por eso ni el atributo ni el método lo tienen.

> ⚠️ **Conflicto LL(1) #1 — `<Atributo>` vs `<Metodo>`.**
> Los dos pueden empezar con un `<Tipo>`. Con `int x` en la entrada, el parser no sabe si sigue
> un `;` (atributo) o un `(` (método), y eso está **dos tokens más adelante**.
> `Primeros(<Atributo>) ∩ Primeros(<Metodo>) = {boolean, char, int, idClase, idGen}`.
> No es ambigüedad: `int x;` y `int x(){}` son cadenas distintas, cada una con un único árbol.
> Es un prefijo común, y se arregla factorizando (F6).

**Efecto del logro *Visibilidad Mejorada E2*:** al permitir `public`/`private` delante de
cualquier miembro, el `public` deja de identificar al constructor y pasa a ser un modificador
como cualquier otro. El constructor se vuelve `idClase ( … ) { … }` y **empieza a competir con el
atributo de tipo referencia**: `C(){}` contra `C x;`. Es un conflicto que el logro *crea*, y hay
que resolverlo también.

### `<ModificadorOpcional> ::= static | ε` y `<TipoMetodo> ::= <Tipo> | void`

Dos «opcionales» separados. `<TipoMetodo>` existe porque `void` es un tipo válido de retorno pero
no un tipo válido de variable: no se puede declarar `void x;`. Al tener `<Tipo>` y `<TipoMetodo>`
por separado, la gramática impide `void` como tipo de un atributo o de un argumento sin necesidad
de ningún chequeo semántico.

### `<MetodoInterfaz> ::= <TipoMetodo> idMetVar <ArgsFormales> ;`

La firma de un método de interfaz: **termina en `;`, no tiene cuerpo**. La diferencia con
`<Metodo>` está en el último símbolo, y como viven en no terminales distintos (uno se usa desde
la clase y el otro desde la interfaz), no hay conflicto. Tampoco lleva `static`.

---

## 3. Los tipos

### `<Tipo> ::= <TipoBase> <DimensionesOpcionales>`

Un tipo es un tipo base más cero o más pares de corchetes: `int`, `int[]`, `Persona[][]`.

> **Detalle de transcripción:** el PDF escribe `[]` como si fuera un símbolo, pero léxicamente
> son **dos** tokens (`corcheteA` y `corcheteC`) — «Aspectos Léxicos» no define ningún token
> `[]`. En los documentos derivados se escribe `[ ]`, que es lo que el parser va a matchear.

### `<TipoBase> ::= <TipoPrimitivo> | <TipoReferencia> | idGen`

Tres familias, y se distinguen con un solo token: `{boolean, char, int}`, `{idClase}`, `{idGen}`.
El léxico ya separa `idClase` de `idGen` (un `idGen` es **una única letra mayúscula**), y esa
decisión léxica es la que hace que esta regla sea LL(1) sin esfuerzo. Es un buen ejemplo de cómo
el léxico le simplifica la vida al sintáctico.

### `<DimensionesOpcionales> ::= [ ] <DimensionesOpcionales> | ε`

Cero o más `[]`, recursión a derecha. Sin tamaño: en la declaración de un tipo no se pone tamaño.

### `<TipoReferencia> ::= idClase <TipoGenericoOpcional>`

`Persona`, `Lista<Persona>`, `Lista<T>`.

### `<TipoGenericoOpcional> ::= < <InstanciadoOParametrico> > | ε`

### `<InstanciadoOParametrico> ::= idGen | idClase`

**Por qué el nombre:** dentro de los `< >` puede ir un `idClase` (*instanciado*: `Lista<Persona>`,
se fija el tipo concreto) o un `idGen` (*paramétrico*: `Lista<T>` dentro de una clase genérica,
se pasa el parámetro). Los dos casos se distinguen con un token, así que la regla está bien.

**Lo que NO permite:** anidamiento (`Lista<Lista<Persona>>`) ni el diamante (`new Lista<>()`).
Los dos los agrega el logro *Genericidad Avanzada E2*.

> 🟡 **Defecto latente.** `<TipoGenericoOpcional>` se usa desde dos lugares: `<TipoReferencia>`
> (posición de **tipo**: `Lista<Persona> x;`) y `<LlamadaConstructor>` (posición de
> **instanciación**: `new Lista<Persona>()`). Hoy son la misma construcción y compartir está
> bien. **Con el logro deja de estarlo**, porque el diamante `<>` sólo vale al instanciar. Si se
> comparte el no terminal, hay que aceptar `Lista<> x;` y rechazarlo después en el semántico. En
> la gramática final se separan en dos, y la restricción del logro queda impuesta por la forma de
> las reglas.

---

## 4. Los argumentos formales

```
<ArgsFormales>              ::= ( <ListaArgsFormalesOpcional> )
<ListaArgsFormalesOpcional> ::= <ListaArgsFormales> | ε
<ListaArgsFormales>         ::= <ArgFormal> | <ListaArgsFormales> , <ArgFormal>
<ArgFormal>                 ::= <Tipo> idMetVar
```

Los paréntesis, adentro cero o más argumentos separados por comas, cada uno con tipo y nombre.

**Por qué hay tres no terminales para una lista:** el `<…Opcional>` cubre el caso de **cero**
argumentos, y el `<Lista…>` cubre **uno o más**. Separarlos evita que la lista misma pueda ser
vacía, que es lo que permitiría `(,)` o `(int a,)`.

> ⚠️ **Conflicto LL(1) #2 — recursión a izquierda en `<ListaArgsFormales>`.**
> La segunda producción arranca con el propio `<ListaArgsFormales>`. Un parser recursivo que
> intentara seguirla se llamaría a sí mismo sin haber consumido ni un token: **recursión
> infinita**. Además ambas alternativas tienen los mismos Primeros. Se arregla dando vuelta la
> recursión (R1).
>
> Ojo: la recursión a izquierda **no es ambigüedad**. `a, b, c` tiene un solo árbol (asociado a
> izquierda). El problema es puramente de implementación.

---

## 5. Las sentencias

### `<Bloque> ::= { <ListaSentencias> }` y `<ListaSentencias>`

Llaves con cero o más sentencias adentro. Mismo patrón de lista de siempre.

### `<Sentencia>` — ocho alternativas

```
<Sentencia> ::= ;
<Sentencia> ::= <Asignacion> ;
<Sentencia> ::= <Llamada> ;
<Sentencia> ::= <VarLocal> ;
<Sentencia> ::= <Return> ;
<Sentencia> ::= <If>
<Sentencia> ::= <While>
<Sentencia> ::= <Bloque>
```

**Por qué las cuatro primeras llevan el `;` acá y no adentro:** para que el punto y coma sea
visible en la regla de la sentencia. Las tres últimas (`<If>`, `<While>`, `<Bloque>`) **no**
llevan `;` porque terminan en `}` o en otra sentencia. La primera alternativa, `;` sola, es la
sentencia vacía: `while (x) ;`.

> 🔴 **Ambigüedad #1 — `<Asignacion>` y `<Llamada>` son la misma regla.**
>
> ```
> <Asignacion> ::= <Expresion>
> <Llamada>    ::= <Expresion>
> ```
>
> Cuerpos **idénticos**. No es un prefijo común: es la misma producción escrita dos veces. Eso
> significa que **toda** sentencia-expresión tiene dos árboles de derivación — uno pasando por
> `<Asignacion>` y otro por `<Llamada>` — y la gramática no da ningún criterio para elegir.
>
> **Por qué la cátedra la escribió así:** casi seguro para documentar la *intención*: en una
> sentencia-expresión uno espera una asignación o una llamada, no `5 + 3;`. Pero la gramática no
> puede distinguirlas, porque sintácticamente `x = 5` y `f()` son las dos una `<Expresion>`.
>
> **Cómo se resuelve:** se colapsan en una sola alternativa. Decidir si la expresión es una
> asignación válida, una llamada válida, o ninguna (`5 = x;` pasa el sintáctico) es trabajo del
> **semántico**. Colapsarlas no achica el lenguaje: lo genera exactamente igual, con un árbol por
> sentencia en lugar de dos.

### `<VarLocal> ::= var idMetVar = <ExpresionCompuesta>`

Declaración de variable local, **sólo con `var`** y **siempre con inicialización**. No existe
`int x;` en la MiniJava base — eso es el logro *Variables locales Clásicas E2*.

**Por qué `<ExpresionCompuesta>` y no `<Expresion>`:** para que no se pueda escribir
`var x = y = 3;`. La regla es más restrictiva a propósito.

> **Efecto del logro *Variables locales Clásicas*:** al permitir `C x;`, una declaración local
> pasa a poder empezar con `idClase`… y una sentencia-expresión también (`C.m();`, por
> `<LlamadaMetodoEstatico>`). Es otro conflicto **creado por un logro**. Se resuelve factorizando
> el `idClase` en `<Sentencia>`: el token que sigue desempata (`.` es expresión, cualquier otro
> es declaración).

### `<Return> ::= return <ExpresionOpcional>` y `<ExpresionOpcional> ::= <Expresion> | ε`

`return;` y `return x;` con la misma regla. Que un método `int` no pueda hacer `return;` es
**semántico**: sintácticamente los dos son válidos.

### `<If>` — dos producciones

```
<If> ::= if ( <Expresion> ) <Sentencia>
<If> ::= if ( <Expresion> ) <Sentencia> else <Sentencia>
```

> ⚠️ **Conflicto LL(1) #3.** Prefijo común larguísimo: `if ( <Expresion> ) <Sentencia>`. El parser
> tendría que atravesar una condición y una sentencia entera antes de saber qué producción
> eligió. Se factoriza sacando el `else` a un `<ElseOpcional>`.

> 🔴 **Ambigüedad #2 — el *dangling else*.** Y esto la factorización **no** lo arregla. En
>
> ```java
> if (a) if (b) s1; else s2;
> ```
>
> el `else` puede ligar al `if` de adentro o al de afuera: dos árboles, misma cadena.
>
> **Cómo se resuelve:** con una **regla de implementación**, que es la segunda opción que da la
> cátedra (Clase 4, slide 49). El Esquema Simple toma la rama `else` apenas ve el token, y como
> el `if` que se está ejecutando en ese momento es el de adentro, el `else` liga al `if` más
> cercano — que es exactamente la regla de Java. No hace falta tocar la gramática: alcanza con
> documentar que el orden de las ramas de ese `if/else` en el código **es significativo**.

### `<While> ::= while ( <Expresion> ) <Sentencia>`

Sin defectos. El cuerpo es una `<Sentencia>`, así que puede ser un bloque o una sola sentencia,
como en Java.

---

## 6. Las expresiones

Ésta es la parte con más estructura, y conviene leerla como una **pila de capas**, de menor a
mayor precedencia:

```
<Expresion>           asignación
<ExpresionCompuesta>  operadores binarios
<ExpresionBasica>     operadores unarios prefijos
<Operando>            literal o referencia
```

Cada capa llama a la de abajo. Ese encadenamiento es lo que fija qué se agrupa con qué.

### `<Expresion>` — la capa de asignación

```
<Expresion> ::= <ExpresionCompuesta> <OperadorAsignacion> <ExpresionCompuesta>
<Expresion> ::= <ExpresionCompuesta>
<OperadorAsignacion> ::= =
```

**El detalle importantísimo:** el lado derecho del `=` es una `<ExpresionCompuesta>`, **no** una
`<Expresion>`. Consecuencia: **`a = b = c` NO es un programa MiniJava válido**. La asignación no
es asociativa.

Es fácil pasarlo por alto y «arreglarlo» al transformar, poniendo `<Expresion>` a la derecha
porque queda más prolijo. Eso **agrandaría el lenguaje** y ya no sería la misma gramática.

> ⚠️ **Conflicto LL(1) #4.** Prefijo común `<ExpresionCompuesta>`. Viendo `x`, el parser no sabe
> si va a aparecer un `=` — y puede estar arbitrariamente lejos. Se factoriza (F1).

### `<ExpresionCompuesta>` — los operadores binarios

```
<ExpresionCompuesta> ::= <ExpresionCompuesta> <OperadorBinario> <ExpresionBasica>
<ExpresionCompuesta> ::= <ExpresionBasica>
<OperadorBinario> ::= || | && | == | != | < | > | <= | >= | + | - | * | / | %
```

**El detalle que sorprende:** los 13 operadores están en **un solo nivel**. No hay jerarquía entre
`*` y `+`, ni entre `&&` y `||`. La gramática típica de un lenguaje mete un no terminal por nivel
de precedencia (como el ejemplo `E → E + T`, `T → T * F`, `F → id` de Clase 4, slide 49). Ésta no.

Consecuencia: **la precedencia de operadores no está en la gramática de MiniJava**. `a + b * c` y
`a * b + c` se derivan con la misma forma. Es una decisión de la cátedra, y hay que respetarla:
darle precedencia sería cambiar el lenguaje. El desambiguado queda para etapas posteriores.

Esto también significa que la eliminación de la recursión a izquierda **no pierde nada**: no hay
precedencia que perder.

> ⚠️ **Conflicto LL(1) #5 — recursión a izquierda.** Es el ejemplo textual de la clase
> (`E → E + T | T`). Se da vuelta (R2).

### `<ExpresionBasica>` y `<OperadorUnario> ::= + | - | !`

```
<ExpresionBasica> ::= <OperadorUnario> <Operando>
<ExpresionBasica> ::= <Operando>
```

Un operando con **a lo sumo un** operador unario adelante. `--x` no es válido en MiniJava base
(sería dos unarios seguidos), y `!!x` tampoco. Los Primeros son disjuntos (`{+,-,!}` contra los
del operando), así que la regla está bien.

> Los tokens `++` y `--` **existen en el léxico** («Aspectos Léxicos» §2.4.2 los lista) pero
> **no aparecen en ninguna parte de esta gramática**. Son tokens sin lugar. El logro *Operadores
> Posifijos E2* es el que se los da.

### `<Operando> ::= <Primitivo> | <Referencia>` y `<Primitivo>`

```
<Primitivo> ::= true | false | intLiteral | charLiteral | null
```

Un operando es un literal o algo que hay que ir a buscar. Los Primeros son disjuntos.

> `floatLiteral` **no está**, aunque el léxico de la Etapa 1 lo emite (era un logro de esa etapa).
> Si la gramática no se extiende, ese logro queda inutilizable en Etapa 2. Lo mismo con `float`
> en `<TipoPrimitivo>`.

---

## 7. Referencias y primarios

La parte más enredada de la gramática, y la que más conflictos concentra.

### `<Referencia>` — el encadenamiento

```
<Referencia> ::= <Primario>
<Referencia> ::= <Referencia> <VarEncadenada>
<Referencia> ::= <Referencia> <MetodoEncadenado>
<Referencia> ::= <Referencia> <AccesoArreglo>
```

**Qué modela:** una cadena como `this.lista.get(3).nombre[0]`. Se arranca de un `<Primario>` y se
le van colgando eslabones: `.campo`, `.metodo(…)`, `[índice]`. La recursión a izquierda expresa
que la cadena crece **hacia la derecha** y se asocia hacia la izquierda — `a.b.c` es `(a.b).c`,
que es lo correcto.

> ⚠️ **Conflictos LL(1) #6 a #11 — recursión a izquierda, tres alternativas.** Las cuatro
> producciones tienen los Primeros de `<Primario>`, lo que da 6 pares en conflicto. Se elimina
> con el esquema de siempre (R3).

### `<VarEncadenada>` y `<MetodoEncadenado>`

```
<VarEncadenada>    ::= . idMetVar
<MetodoEncadenado> ::= . idMetVar <ArgsActuales>
```

`obj.campo` contra `obj.metodo(...)`.

> ⚠️ **Conflicto LL(1) #12.** Comparten **todo** el prefijo: `. idMetVar`. Sólo se distinguen por
> si después viene un `(`.
>
> **Este par se confunde a menudo con una ambigüedad. No lo es.** `<VarEncadenada>` deriva
> exactamente la cadena `. idMetVar` — dos tokens, sin alternativas. `<MetodoEncadenado>` deriva
> `. idMetVar` más un `<ArgsActuales>`, que **nunca puede ser vacío** (siempre aporta al menos
> `(` y `)`), o sea al menos cuatro tokens. **Ninguna cadena puede estar en los dos lenguajes**,
> así que ninguna cadena tiene dos árboles. Es un prefijo común, se factoriza, y listo.
>
> El script lo verifica solo:
> `python3 herramientas/primeros.py gramatica/minijava-base.g --longitudes VarEncadenada MetodoEncadenado`
>
> El conflicto **no aparece hasta después de eliminar la recursión a izquierda de
> `<Referencia>`**, porque recién ahí las dos quedan como alternativas del mismo no terminal. Es
> el fenómeno que anticipa Clase 4, slide 38: eliminar recursión genera producciones sin
> factorizar. Por eso el orden es recursión primero, factorización después.

### `<Primario>` — las ocho formas de arrancar una referencia

```
<Primario> ::= this | stringLiteral | <AccesoVar> | <CrearArreglo>
             | <LlamadaConstructor> | <LlamadaMetodo> | <LlamadaMetodoEstatico>
             | <ExpresionParentizada>
```

**Por qué `stringLiteral` está acá y no en `<Primitivo>`:** porque de un String se pueden colgar
eslabones (`"hola".length()`), y de un `intLiteral` no. Ponerlo en `<Primario>` lo habilita para
el encadenamiento. Es una decisión fina y deliberada de la gramática.

**Por qué `<ExpresionParentizada>` está acá:** por lo mismo. `(a + b).metodo()` tiene que poder
encadenarse.

```
<AccesoVar>             ::= idMetVar
<LlamadaMetodo>         ::= idMetVar <ArgsActuales>
<LlamadaMetodoEstatico> ::= idClase . idMetVar <ArgsActuales>
<CrearArreglo>          ::= new <TipoBase> <DimensionesConTamanio>
<LlamadaConstructor>    ::= new <TipoReferencia> <ArgsActuales>
<ExpresionParentizada>  ::= ( <Expresion> )
```

> ⚠️ **Conflicto LL(1) #13 — `<AccesoVar>` vs `<LlamadaMetodo>`.** Exactamente el mismo caso que
> `<VarEncadenada>`/`<MetodoEncadenado>`, un nivel más arriba: `x` contra `x(...)`. Tampoco es
> ambiguo (1 token contra 3 o más). Se resuelve igual.

> ⚠️ **Conflicto LL(1) #14 — `<CrearArreglo>` vs `<LlamadaConstructor>`.** Los dos empiezan con
> `new`, y **el conflicto es a dos niveles**: no alcanza con factorizar el `new`, porque
> `<TipoBase>` puede derivar a `<TipoReferencia>`. O sea que `new C` sigue sin decidirse.
> El desempate está en el token que viene **después del tipo**: `[` es un arreglo, `(` es un
> constructor. Tampoco es ambiguo por la misma razón.
>
> Notar que `new int[3]` y `new T[3]` **sí** se deciden en el primer token después de `new`,
> porque un `<TipoPrimitivo>` o un `idGen` no pueden ser un `<TipoReferencia>`. Sólo el caso
> `new C…` necesita el segundo nivel.

`<LlamadaMetodoEstatico>` es la única forma de `<Primario>` que empieza con `idClase`, así que no
choca con nada. **Ningún `<Primario>` deriva un `idClase` suelto**: no existe la referencia a una
clase por sí sola, siempre tiene que ser `C.metodo(...)`.

### Argumentos actuales, dimensiones y acceso a arreglo

```
<ArgsActuales>          ::= ( <ListaExpsOpcional> )
<ListaExpsOpcional>     ::= <ListaExps> | ε
<ListaExps>             ::= <Expresion> | <Expresion> , <ListaExps>
<DimensionesConTamanio> ::= [ <Expresion> ] <DimensionesConTamanio> | [ <Expresion> ]
<AccesoArreglo>         ::= [ <Expresion> ]
```

`<ArgsActuales>` sigue el mismo patrón de tres niveles que `<ArgsFormales>`, por el mismo motivo:
permitir cero argumentos sin permitir comas sueltas.

> ⚠️ **Conflicto LL(1) #15 — `<DimensionesConTamanio>`.** Prefijo común `[ <Expresion> ]`.
> **Cuidado al arreglarlo:** la regla exige **al menos una** dimensión. La transformación
> «natural» a `<DimensionesConTamanio> ::= [ <Expresion> ] <DimensionesConTamanio> | ε`
> permitiría cero dimensiones y agrandaría el lenguaje. La parte repetible tiene que ir en un no
> terminal opcional aparte.

> ⚠️ **Conflicto LL(1) #16 — `<ListaExps>`.** Prefijo común `<Expresion>`, mismo caso que
> `<ListaArgsFormales>`.

**`<DimensionesConTamanio>` vs `<AccesoArreglo>`:** son parecidas pero viven en mundos distintos.
La primera se usa **sólo** bajo `new` (crear el arreglo, con tamaños), la segunda **sólo** en el
encadenamiento (indexar un arreglo existente). Nunca compiten.

---

## 8. Resumen de los defectos

| # | Dónde | Tipo | Cómo se resuelve |
|---|---|---|---|
| 1 | `<Miembro>`: Atributo vs Metodo | ⚠️ prefijo común profundo | factorización profunda (F6) |
| 2 | `<ListaArgsFormales>` | ⚠️ recursión a izquierda | R1 |
| 3 | `<Sentencia>`: Asignacion vs Llamada | 🔴 **ambigüedad** | colapsar en sentencia-expresión (F2) |
| 4 | `<If>` | ⚠️ prefijo común | factorizar el `else` (F3) |
| 4b | `<If>`: dangling else | 🔴 **ambigüedad** | regla de implementación (rama `else` primero) |
| 5 | `<Expresion>` | ⚠️ prefijo común | F1 |
| 6 | `<ExpresionCompuesta>` | ⚠️ recursión a izquierda | R2 |
| 7-12 | `<Referencia>` (3 alternativas) | ⚠️ recursión a izquierda | R3 |
| 12b | VarEncadenada vs MetodoEncadenado | ⚠️ prefijo común (**aparece tras R3**) | fusionar en `<Encadenado>` |
| 13 | `<Primario>`: AccesoVar vs LlamadaMetodo | ⚠️ prefijo común | F7 |
| 14 | `<Primario>`: CrearArreglo vs LlamadaConstructor | ⚠️ prefijo común a dos niveles | F8 |
| 15 | `<DimensionesConTamanio>` | ⚠️ prefijo común | F4 |
| 16 | `<ListaExps>` | ⚠️ prefijo común | F5 |

**Dos ambigüedades reales, catorce conflictos de predicción.** Las ambigüedades hay que
*decidirlas*; los conflictos se arreglan mecánicamente sin tocar el lenguaje.

### Y tres cosas que faltan, no que están mal

- `++` y `--` son tokens del léxico sin ninguna regla que los use.
- `float` y `floatLiteral` tampoco tienen lugar, y el léxico de la Etapa 1 los emite.
- `for`, `private`, `?` y `->` no están **ni en la gramática ni en el léxico**: los agregan cuatro
  de los logros de la Etapa 2.

## 9. Qué chequea y qué no chequea el analizador sintáctico

Sale de todo lo anterior, y define el alcance del trabajo.

**Sí chequea** — que los tokens estén en un orden que la gramática pueda generar: la estructura de
clases, miembros, bloques y sentencias; que los delimitadores abran y cierren; que una expresión
esté bien formada; que un método tenga cuerpo y una interfaz no.

**No chequea** — todo esto pasa el sintáctico y muere en el semántico:

| Pasa el sintáctico | Por qué es semántico |
|---|---|
| `int x = "hola";` | compatibilidad de tipos |
| `noDeclarada = 5;` | declaración y alcance de nombres |
| `5 = x;` | que el lado izquierdo sea asignable |
| `class A extends UnaInterfaz {}` | que el padre sea realmente una clase |
| `m(1,2,3)` con `m` de un argumento | aridad contra la declaración |
| `int x; int x;` | duplicados |
| `return;` en un método `int` | compatibilidad del retorno |
| una lambda que usa una variable de afuera | **captura de variables** |

El último caso merece atención: el logro *MiniLambdas E2* prohíbe la captura de variables locales
y parámetros, pero **eso no es decidible sintácticamente** — el parser no sabe qué nombres están
en alcance. Lo único que el sintáctico puede rechazar de ese logro es la **forma**: parámetros sin
tipo, cuerpo de una única expresión, sin llaves.
