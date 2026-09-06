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

### 2.2. El cuerpo de la lambda — introducida por el logro

`x -> a + b` tiene dos derivaciones:

- `x -> (a + b)` — el cuerpo se come el `+ b`;
- `(x -> a) + b` — el cuerpo es sólo `a`, y el `+ b` pertenece a la expresión que contiene a la
  lambda.

La causa es dónde vive la lambda: se produce a nivel `<Primario>`, o sea que ocupa una ranura de
operando, y entonces la cola de la expresión que la contiene compite con la cola de su cuerpo.
Contagia a cinco no terminales, todos con la misma raíz: `<RestoExpresion>` (`=`),
`<RestoExpresionCompuesta>` (los 13 binarios), `<RestoReferencia>` (`.` y `[`),
`<RestoCondicional>` (`?`) y `<PosfijoOpcional>` (`++`, `--`).

Java no la tiene porque el JLS pone la lambda como alternativa de `AssignmentExpression`, nunca
como `Primary`: al vivir en el nivel más alto de la jerarquía, no hay cola externa con la que
competir.

**Decisión:** resolución voraz y declarada, no reestructuración. Los motivos:

- Las dos salidas producen **el mismo analizador**: un descendente recursivo que llame a
  `Expresion()` para el cuerpo consume todo lo que puede, o sea voraz, que además es lo que hace
  Java.
- Subir la lambda al nivel de `<Expresion>` como hace el JLS eliminaría la ambigüedad de raíz, pero
  exigiría factorizar `idMetVar` y `(` contra toda la cola de expresión — unas quince producciones
  nuevas, con la misma técnica que `<ExpresionSinIdClase>` — para no cambiar nada de lo que el
  analizador hace.
- Clase 4, slide 49 autoriza expresamente resolver una ambigüedad con una regla de implementación,
  que es la vía que el proyecto ya usa para el `else`.

En la práctica: `x -> a + b` es `x -> (a + b)`, `x -> a.m()` es `x -> (a.m())`, y así con los seis
testigos. Para forzar la otra lectura hay que parentizar: `(x -> a) + b`.

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

**Decisión:** se cierra con un chequeo en el analizador —verificar que lo parseado entre paréntesis
fue exactamente un `idMetVar`— y no en la gramática. Está en el backlog de la Sesión 3 y detallado
en [`decisiones-de-deteccion-de-errores.md`](decisiones-de-deteccion-de-errores.md).

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
