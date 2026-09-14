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

**Decisión:** se colapsan en una única alternativa, `<SentenciaExpresion>`. No achica el lenguaje
—lo genera exactamente igual, con un árbol por sentencia en lugar de dos— y distinguir una
asignación de una llamada requiere conocer el significado de la expresión, que es análisis
semántico. `5 = x;` atraviesa el analizador sintáctico, igual que en la gramática original.

### 1.2. El *dangling else*

`if (a) if (b) s1; else s2;` — el `else` puede ligar al `if` interno o al externo.

**Decisión:** regla de implementación (Clase 4, slide 49), no reescritura. El Esquema Simple toma
la rama `else` apenas ve el token, y como el `if` que se está ejecutando es el interno, liga al más
cercano — **que es la regla de Java**.

## 2. Ambigüedades que quedan en la gramática final

Además de las dos anteriores, hay cinco más. Ninguna es un defecto oculto: están detectadas,
declaradas con su regla de resolución, y el verificador **falla** si aparece una que no lo esté.

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

### 2.2. Dónde vive la lambda, y la ambigüedad que le queda

La ubicación que se impone sola al leer el logro es `<Primario>`: la lambda aparece donde aparece
un valor, y `<Primario>` es el no terminal de los valores. **Es la equivocada**, y trae dos
problemas de distinto peso.

**El grave: el lenguaje queda más grande que el de Java.** `<Primario>` es la ranura de un
*operando*. Si la lambda es un `<Primario>`, entonces es un operando válido de cualquier operador,
y estas cinco formas pasan el análisis:

| Forma | Con la lambda en `<Primario>` | Java (`javac`) |
|---|---|---|
| `1 + x -> x` | acepta como `1 + (x -> x)` | `lambda expression not expected here` |
| `a == x -> x` | acepta | rechaza |
| `- x -> x`, `! x -> x` | acepta | rechaza |
| `(x -> x) + 1` | acepta | rechaza |
| `(x -> x).m()` | acepta | rechaza |

El JLS define `Expression: LambdaExpression | AssignmentExpression`: la lambda es alternativa del
nivel **más alto** de la jerarquía, nunca un `Primary`. Dicho de otra forma, `->` no tiene
precedencia contra los operadores binarios ni unarios **porque no compite con ellos** — vive arriba
de todos. Por la regla «lo no especificado es igual a Java», eso decide la ubicación.

**El menor: la ambigüedad del cuerpo.** `x -> a + b` tiene dos derivaciones si la lambda es un
operando —`x -> (a + b)` y `(x -> a) + b`—, porque la cola de la expresión que la contiene compite
con la cola de su cuerpo. Contagia a los cinco no terminales de cola: `<RestoExpresion>` (`=`),
`<RestoExpresionCompuesta>` (los 13 binarios), `<RestoReferencia>` (`.` y `[`),
`<RestoCondicional>` (`?`) y `<PosfijoOpcional>` (`++`, `--`).

**Decisión: la lambda se produce en el nivel de `<Expresion>`.** Cuesta factorizar las dos cabezas
que comparte con una expresión normal —`idMetVar` y `(`— contra la cola de expresión, con la misma
técnica que `<ExpresionSinIdClase>`: catorce producciones nuevas y ninguna lógica duplicada, porque
todas las colas se comparten. A cambio:

- el lenguaje reconocido pasa a ser el de Java en los cinco casos de la tabla;
- **cuatro de las cinco ambigüedades desaparecen de raíz**, en vez de resolverse por regla.

La que sobrevive es la del `=`: `x -> a = b` es `x -> (a = b)`, porque `<RestoExpresion>` es la
única cola externa que todavía compite con el cuerpo. Se resuelve **voraz**, que es lo que hace
Java y lo que un descendente recursivo hace por construcción. Clase 4, slide 49 autoriza
expresamente resolver una ambigüedad con una regla de implementación, que es la vía que el proyecto
ya usa para el `else`.

Queda un hueco que la gramática no alcanza a cerrar y que cierra el analizador: el contenido de un
paréntesis **sí** puede ser una lambda —`f((x -> x))` es válido en Java—, así que `(x -> x).m()`
atraviesa la gramática. El detalle, con la regla de la raíz y su límite declarado, está en
[`decisiones-de-deteccion-de-errores.md`](decisiones-de-deteccion-de-errores.md).

## 3. Alcance del operador ternario

La gramática original usa `<ExpresionCompuesta>` como lado derecho de un `=` en **los seis lugares**
donde hay uno: la asignación y los cinco inicializadores —variable local con `var`, atributo,
declarador clásico, y los dos del encabezado del `for`—. Lo hace para impedir la asignación
encadenada.

Al insertar la capa del ternario *por encima* de `<ExpresionCompuesta>`, esos seis quedarían sin
acceso al operador.

**Decisión:** los seis pasan a `<ExpresionCondicional>`, exactamente un escalón más arriba. Sigue
sin admitir un `=`, así que `a = b = c` se sigue rechazando; pero admite el ternario, así que
`var x = c ? a : b;` es válido igual que `x = c ? a : b;`. Y como con el logro apagado
`<ExpresionCondicional>` es un alias de `<ExpresionCompuesta>`, **el lenguaje base queda idéntico
al de la cátedra**.

El ternario es **asociativo a derecha**, como en Java: `a ? b : c ? d : e` es `a ? b : (c ? d : e)`.

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

## 6. Orden de los modificadores: fiel a la gramática original

Con el logro de visibilidad, `public static void f(){}` se acepta y `static public void f(){}` se
rechaza. Java admite los dos órdenes.

**Decisión:** se mantiene la restricción. La gramática original pone `<ModificadorOpcional>`
*dentro* de `<Metodo>`, después de la posición donde el logro coloca la visibilidad; invertir el
orden sería agrandar el lenguaje más allá de lo que el logro pide, y el logro sólo habla de *poder
indicar* la visibilidad, no de dónde puede ir respecto de `static`.

## 7. Inicialización de arreglos: multidimensional

El logro dice *«permite la inicialización de Arreglos al momento de su construcción **como en
Java**. Sólo se permite como parte de una expresión de creación utilizando `new`»*. La única
restricción que enuncia es **dónde**, no cuántas dimensiones.

**Decisión:** el inicializador es anidable, como en Java. Un elemento puede ser una expresión o, a
su vez, otro inicializador.

```
new int[]{1, 2, 3}              válido
new int[][]{{1,2}, {3,4}}       válido
new int[][][]{{{1}}}            válido
new int[]{}                     válido
```

La restricción de que sólo valga bajo `new` queda impuesta **por la gramática**: el inicializador
sólo es alcanzable desde `<Creacion>`.

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

## 9. Permisividad declarada de las lambdas

En `(a, b) -> x`, el primer elemento se deriva como `<Expresion>`, no como parámetro. En
consecuencia `(a+1) -> x` y `(a+1, b) -> x` **atraviesan el analizador**.

Está forzado por LL(1): `(a)` como expresión parentizada y `(a)` como lista de parámetros son
indistinguibles hasta ver el token *después* del `)`, y para entonces `a` ya se parseó como
expresión. Cerrarlo en la gramática exigiría duplicar la cadena de colas de expresión en su
variante «no vacía», unos siete no terminales clonados.

Del mismo origen sale la segunda: ese contenido entre paréntesis **puede** ser una lambda, porque
`f((x -> x))` es válido en Java, así que `(x -> x).m()` y `(x -> x) + 1` también atraviesan la
gramática aunque `javac` los rechace.

**Decisión:** las dos se cierran con un chequeo en el analizador —que lo parseado entre paréntesis
haya sido exactamente un `idMetVar`, y que una lambda parentizada no lleve cola— y no en la
gramática. El detalle está en
[`decisiones-de-deteccion-de-errores.md`](decisiones-de-deteccion-de-errores.md).

Del segundo parámetro en adelante la gramática **sí** exige identificador: `(a, b+1) -> x` y
`(a, int b) -> x` se rechazan.

## 10. `float` y `floatLiteral`: carry-over de la Etapa 1

El léxico emite `pr_float` y `floatLiteral` desde la Etapa 1, por un logro de aquella etapa, pero
la gramática de la cátedra no los tiene en `<TipoPrimitivo>` ni en `<Primitivo>`. Sin extenderla,
ese logro quedaría inutilizable en la Etapa 2. Se agregan detrás de su propio interruptor.

## Ver también

- [`defectos-de-la-gramatica-original.md`](defectos-de-la-gramatica-original.md) — la gramática de
  la cátedra explicada regla por regla, con sus defectos.
- [`transformacion-a-ll1-paso-a-paso.md`](transformacion-a-ll1-paso-a-paso.md) — cada corrección,
  en cuatro pasos.
- [`resolucion-de-logros.md`](resolucion-de-logros.md) — qué exige cada logro y cómo se resolvió.
- [`../lexico/decisiones-de-logica.md`](../lexico/decisiones-de-logica.md) — las equivalentes de la
  Etapa 1.
