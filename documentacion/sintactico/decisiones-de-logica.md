# Decisiones de lógica del Analizador Sintáctico

Qué lenguaje reconoce exactamente el analizador. Cada caso donde la especificación admitía más de
una lectura, cuál se eligió, con qué fundamento, y qué programas quedan adentro y afuera como
consecuencia.

La regla que gobierna todo lo que sigue es la de la cátedra: **todo lo que no esté especificado
para MiniJava se comporta igual que en Java**, y «como en Java» nunca se resuelve de memoria — se
verifica contra la especificación o ejecutando `javac`.

## 1. Las dos ambigüedades de la gramática original

De los dieciséis conflictos que tiene la gramática de la cátedra, **catorce son de predicción** —se
arreglan factorizando y el lenguaje no cambia— y **dos son ambigüedades reales**, donde hubo que
elegir un significado.

### 1.1. `<Asignacion>` y `<Llamada>` son la misma producción

```
<Asignacion> ::= <Expresion>
<Llamada>    ::= <Expresion>
```

Cuerpos idénticos, y `<Sentencia>` los usa en dos alternativas también idénticas: **toda**
sentencia-expresión tiene dos árboles de derivación.

**Decisión:** se colapsan en una única alternativa. No achica el lenguaje
—lo genera exactamente igual, con un árbol por sentencia en lugar de dos— y distinguir una
asignación de una llamada requiere conocer el significado de la expresión, que es análisis
semántico. `5 = x;` atraviesa el analizador sintáctico, igual que en la gramática original.

### 1.2. El *dangling else*

`if (a) if (b) s1; else s2;` — el `else` puede ligar al `if` interno o al externo.

**Decisión:** regla de implementación (Clase 4, slide 49), no reescritura. El Esquema Simple toma
la rama `else` apenas ve el token, y como el `if` que se está ejecutando es el interno, liga al más
cercano — **que es la regla de Java**.

## 2. Ambigüedades que quedan en la gramática final

Además del *dangling else*, queda una. Las dos están detectadas, declaradas con su regla de
resolución, y el verificador **falla** si aparece una que no lo esté.

### 2.1. `new int[3][4]` — heredada de la cátedra

Después de crear un arreglo, un `[` puede ser **otra dimensión de la creación** o **un acceso al
arreglo recién creado**. Las dos derivaciones existen ya en la gramática original, con producciones
que están todas en el PDF:

```
Creación 2-D:                          Creación 1-D, después indexada:
  CrearArreglo → new TipoBase Dim        Referencia → Referencia AccesoArreglo
  Dim → [ Expresion ] Dim                Referencia → Primario → CrearArreglo → new TipoBase Dim
  Dim → [ Expresion ]                    Dim → [ Expresion ]
  ⇒ new int [3] [4]                      AccesoArreglo → [ Expresion ]
                                         ⇒ new int [3]  [4]
```

**Decisión:** resolución voraz — el `[` se consume como otra dimensión. `new int[3][4]` es una
creación bidimensional, que es lo que hace Java. Un descendente recursivo lo resuelve así por
construcción.

Es una ambigüedad **de la gramática de la cátedra**, no introducida por la transformación.

## 3. Alcance del operador ternario

La gramática original usa `<ExpresionCompuesta>` como lado derecho de un `=` para impedir la
asignación encadenada, y los logros que agregan un inicializador siguen el mismo patrón. Son **cinco
lugares**: la asignación y los cuatro inicializadores —variable local con `var`, atributo,
declarador clásico y la variable de control del `for`—.

Al insertar la capa del ternario *por encima* de `<ExpresionCompuesta>`, esos cinco quedarían sin
acceso al operador.

**Decisión:** los cinco lados derechos pasan a `<ExpresionCondicional>`, exactamente un escalón más
arriba. No admite un `=`, así que `a = b = c` no deriva; pero admite el ternario, así que
`var x = c ? a : b;` es válido igual que `x = c ? a : b;`.

El profesor confirmó el criterio para el declarador de Variables Locales Clásicas: es el mismo del
`var` de la gramática base, que no admite asignaciones anidadas salvo entre paréntesis. Un paréntesis
es un primario y adentro lleva una `<Expresion>` completa, así que:

```
int x = y = 2;          INVÁLIDO    (el error es el segundo `=`)
int x, y = z = 2;       INVÁLIDO
var x = y = 2;          INVÁLIDO
int x = (y = 2);        válido
int x, y = (z = 2);     válido
var x = (y = 2);        válido
int x = (y = z = 2);    INVÁLIDO    (adentro del paréntesis rige la misma regla)
```

El **lado izquierdo** del `=` es una `<ExpresionCompuesta>`, como en la gramática original: un
ternario no lo es, así que `a ? b : c = d` no deriva —`javac` tampoco lo acepta: *«unexpected type,
required: variable»*—. Entre paréntesis el ternario es un primario, y por lo tanto una
`<ExpresionCompuesta>`: `(a ? b : c) = d` deriva, igual que `a + b = c` en la gramática original.
Que el lado izquierdo sea una variable lo controla el semántico.

La capa nueva no le saca nada al lenguaje de la cátedra: sólo le agrega el ternario.

Los operandos son los del JLS §15.25: el del medio es una `<Expresion>` completa y el de la derecha
una `<ExpresionCondicional>`, así que `a ? b = 1 : c` es válido y `a ? b : c = d` no. El ternario es
**asociativo a derecha**, como en Java: `a ? b : c ? d : e` es `a ? b : (c ? d : e)`.

## 4. Precedencia de operadores binarios: no hay

`<OperadorBinario>` es una alternancia plana de los trece operadores, sin la jerarquía habitual de
un no terminal por nivel. En consecuencia `a + b * c` y `a * b + c` se derivan con la misma forma.

**No es una decisión del proyecto: es así en la gramática de la cátedra.** Darle precedencia sería
cambiar el lenguaje. Queda para etapas posteriores. Corolario útil: eliminar la recursión a
izquierda de `<ExpresionCompuesta>` no perdió nada, porque no había precedencia que perder.

## 5. Visibilidad en métodos de interfaz: `public` sí, `private` no

El logro habla de «Métodos, Atributos y Constructores», que son los `<Miembro>` de una clase;
`<MetodoInterfaz>` es un no terminal aparte, así que el caso cae bajo «como en Java». Verificado
con `javac`:

| En Java | Resultado |
|---|---|
| `interface A { int m(); }` | compila |
| `interface B { public int m(); }` | compila — redundante, pero legal |
| `interface C { private int m(); }` | **error**: *missing method body, or declare abstract* |
| `interface D { private int m() {…} }` | compila |

O sea que en Java un método de interfaz `private` **exige cuerpo**. Y en MiniJava no hay forma de
escribirlo: `<MetodoInterfaz>` termina en `;` en la especificación original y **ningún logro de
esta etapa le agrega cuerpo**, así que los métodos de interfaz son siempre abstractos. Un `private`
ahí sería, por construcción, siempre la forma que Java rechaza.

La asimetría no es una preferencia: es una consecuencia.

## 6. Orden de los modificadores: los dos, como en Java

Java admite `public static` y `static public`. MiniJava **no define nada** sobre el orden: el logro
de visibilidad habla de *poder indicar* la visibilidad, no de dónde va respecto de `static`.

**Decisión:** se aceptan los dos órdenes, y lo mismo con `private`. Es la regla del proyecto —lo
que MiniJava no define se comporta como en Java— aplicada tal cual, e indicación explícita del
profesor. `protected` **no** se implementa: no está en MiniJava y agregarlo sí sería ampliar el
lenguaje.

La forma en que la gramática lo expresa hace además que el modificador no se pueda repetir, sin
ningún chequeo fuera de ella:

```
<Miembro> ::= <Visibilidad> <MiembroTrasVisibilidad>
            | static <VisibilidadOpcional> <TipoMetodo> <Metodo>
            | <MiembroSinModificadores>
<MiembroTrasVisibilidad> ::= static <TipoMetodo> <Metodo> | <MiembroSinModificadores>
```

La visibilidad aparece a **un solo lado** del `static`, así que `public private void m(){}`,
`static static void m(){}` y `public static private void m(){}` no derivan. Y `static` aplica
sólo a métodos, como en MiniJava base: no hay atributos estáticos, y el profesor lo confirmó.

## 7. Inicialización de arreglos: como en Java, y anidable

El logro dice *«permite la inicialización de Arreglos al momento de su construcción **como en
Java**. Sólo se permite como parte de una expresión de creación utilizando `new`»*.

MiniJava base define la creación **sin** inicializador, con toda dimensión con su tamaño:

```
CrearArreglo ::= new TipoBase DimensionesConTamanio
DimensionesConTamanio ::= [ Expresion ] DimensionesConTamanio | [ Expresion ]
```

La creación **con** inicializador no la define, así que es la de Java (JLS §15.10.1): ninguna
dimensión lleva tamaño —verificado con `javac`: *«array creation with both dimension expression and
initialization is illegal»*—. El profesor lo confirmó: se hace igual que en Java.

**Decisión:** las dos formas empiezan con `[` y se factorizan. Con una expresión adentro es la
creación de la base; con el corchete vacío, la de Java con inicializador. El inicializador es
anidable —un elemento puede ser una expresión o, a su vez, otro inicializador— y admite la coma
final, como en Java (JLS §10.6).

```
<CreacionDeArreglo> ::= [ <RestoCreacionDeArreglo>
<RestoCreacionDeArreglo> ::= <Expresion> ] <DimensionesConTamanioOpcional>
                           | ] <DimensionesOpcionales> <InicializadorDeArreglo>
```

```
new int[3]                    válido      (MiniJava base)
new int[3][4]                 válido
new int[]{1, 2, 3}            válido
new int[][]{{1,2}, {3,4}}     válido
new int[]{}                   válido
new int[]{1, 2,}              válido      (coma final)
new int[]{,}                  válido

new int[2]{1, 2}              INVÁLIDO    (tamaño e inicializador juntos)
new int[2][2]{{1,2}, {3,4}}   INVÁLIDO
new int[]                     INVÁLIDO    (ni tamaño ni inicializador)
new int[2][]                  INVÁLIDO    (sin inicializador, toda dimensión con tamaño: la base)
new int[]{1,,}                INVÁLIDO
new int[][]{{}{}}             INVÁLIDO    (sin coma entre los elementos, como en Java)
```

**`new int[2][]` es válido en Java, y acá no, a sabiendas.** El JLS lo admite
(`new PrimitiveType DimExprs [Dims]`, §15.10.1) y `javac` lo compila. La consulta al profesor fue
sobre la creación **con** inicializador, y su respuesta —como en Java— se aplica a eso. La creación
**sin** inicializador la define la gramática base, con toda dimensión con su tamaño, y se mantiene:
este proyecto se apega a la respuesta del profesor y no la extiende a lo que no se preguntó.

Las listas vacías valen en cualquier nivel: `new int[][]{}`, `new int[][]{{}}` y
`new int[][]{{}, {}}` son válidos, como en Java. Que el anidamiento del inicializador coincida con
la cantidad de dimensiones **no** es sintáctico: `new int[]{{}}` pasa el parser de `javac` y el
error —*«illegal initializer for int»*— lo da el análisis de tipos, así que acá también queda para
el semántico.

La restricción de que el inicializador sólo valga bajo `new` queda impuesta **por la gramática**:
sólo es alcanzable desde `<Creacion>`. `int[] x = {1, 2};`, que Java acepta en una declaración, no es
válido: el logro lo prohíbe.

## 8. Genericidad: el diamante sólo al instanciar

El logro dice que la notación diamante *«sólo puede utilizarse al instanciar una Clase Genérica»*.
Se impone **por gramática** separando el genérico en posición de tipo del genérico bajo `new`, en
lugar de aceptarlo en todos lados y rechazarlo después en el semántico.

| Entrada | Veredicto |
|---|---|
| `List<Persona> x;` | acepta |
| `List<List<Persona>> x;` | acepta — anidamiento |
| `new List<>()` | acepta — diamante al instanciar |
| `new List<Persona>[3]` | acepta |
| `List<> x;` | **rechaza** — diamante en posición de tipo |
| `new List<>[3]` | **rechaza** — diamante con arreglo |

Una clase o interfaz sigue limitada a **un único** parámetro de tipo, como dice el logro.

`List<List<Persona>>` cierra con dos `>` separados: el léxico no define ningún token `>>`, así que
*maximal munch* entrega dos `>` y no hay nada que resolver del lado sintáctico.

## 9. `float` y `floatLiteral`: carry-over de la Etapa 1

El léxico emite `pr_float` y `floatLiteral` desde la Etapa 1, por un logro de aquella etapa, pero
la gramática de la cátedra no los tiene en `<TipoPrimitivo>` ni en `<Primitivo>`. Sin extenderla,
ese logro quedaría inutilizable en la Etapa 2, así que se agregan las dos alternativas.

## 10. El `for`: la variable de control con `var`, y ningún hueco vacío

El logro pide un `for` *«similar a como es en java»*, restringido a *«una sola sentencia/expresión
por separador»* (Pautas, slide 41). El profesor fijó las dos lecturas que ese texto deja abiertas:

- **Una sola, y obligatoria.** Cada hueco del `for` estándar lleva exactamente una expresión: la
  inicialización, la condición y la actualización. Ninguno queda vacío, así que `for(;;)` no es
  válido, y tampoco un encabezado con alguna parte vacía. Java las hace opcionales (JLS §14.14.1),
  pero la Pauta restringe: habla de *una* expresión por separador, no de *a lo sumo una*.
- **La variable de control se declara con `var` y nada más**, porque los logros se piensan lo más
  independientes posible entre sí, y el declarador tipado es del logro de Variables Locales
  Clásicas.

```
<EncabezadoFor> ::= var idMetVar <EncabezadoForTrasVar>
                  | <Expresion> <ClausulasFinalesFor>
<ClausulasFinalesFor> ::= ; <Expresion> ; <Expresion>
```

El resto es Java (JLS §14.14.2): el `for` de iteradores lleva una variable declarada, un `:` y una
expresión.

```
for (var i = 0; i < n; i++) ;     válido
for (var e : lista) ;             válido
for (i = 0; i < n; i++) ;         válido      (una expresión como inicialización)

for (;;) ;                        INVÁLIDO    (el error es el primer `;`)
for (; i < n; i++) ;              INVÁLIDO    (sin inicialización)
for (var i = 0; ; i++) ;          INVÁLIDO    (sin condición)
for (var i = 0; i < n; ) ;        INVÁLIDO    (sin actualización)
for (int i = 0; i < n; i++) ;     INVÁLIDO    (declarador tipado)
for (Persona p : lista) ;         INVÁLIDO
for (var i; i < n; i++) ;         INVÁLIDO    (var sin inicialización, como la variable local)
for (var i = 0, j = 0; a; b) ;    INVÁLIDO    (una sola por separador)
for (i = 0; a; i++, j++) ;        INVÁLIDO
```

## 11. Declaradores múltiples: cada uno con su inicialización

El logro de Variables Locales Clásicas pide *«declarar varias variables e inicializarlas (por
ejemplo `int x,y,z = 10;`)»*. La forma es la de Java (JLS §14.4): cada `VariableDeclarator` lleva su
propio `= VariableInitializer` opcional, así que `int x = 10, y = 11, z = 12;` e `int x, y, z = 10;`
valen los dos.

Qué inicializa cada una es semántico: en Java, `int x, y, z = 10;` inicializa **sólo** `z`
—verificado con `javac`: usar `x` después da *«variable x might not have been initialized»*—. El
ejemplo del logro no aclara si la cátedra lo lee igual, así que queda para confirmar antes del
semántico.

## 12. Un atributo por declaración

La gramática base declara un atributo por `<Atributo>`: `<Atributo> ::= <Tipo> idMetVar ;`. Java
admite varios en una misma declaración —`int a, b = 0;` compila con `javac`—, pero acá no:

- El logro de Variables Locales Clásicas habla de *«variables locales»* y de declarar varias *«en
  una misma sentencia»*. Un atributo no es una variable local ni se declara en una sentencia: el
  JLS los separa en dos construcciones distintas, `FieldDeclaration` (§8.3) y
  `LocalVariableDeclarationStatement` (§14.4).
- El logro de Atributos Inicializados agrega la **inicialización** del atributo *«al igual que en
  Java»*, no la lista de declaradores.

Como ningún logro lo agrega, rige la base. **Se sabe que Java lo admite**; es una decisión de apego
a la gramática de la cátedra, no un olvido.

```
int a;                  válido
int a = 0;              válido      (Atributos Inicializados)
int a, b;               INVÁLIDO    (el error es la `,`)
int a = 0, b;           INVÁLIDO
```

## Ver también

- [`defectos-de-la-gramatica-original.md`](defectos-de-la-gramatica-original.md) — la gramática de
  la cátedra explicada regla por regla, con sus defectos.
- [`transformacion-a-ll1-paso-a-paso.md`](transformacion-a-ll1-paso-a-paso.md) — cada corrección,
  en cuatro pasos.
- [`resolucion-de-logros.md`](resolucion-de-logros.md) — qué exige cada logro y cómo se resolvió.
- [`../lexico/decisiones-de-logica.md`](../lexico/decisiones-de-logica.md) — las equivalentes de la
  Etapa 1.
