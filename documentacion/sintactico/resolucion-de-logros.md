# Resolución de los logros de la Etapa 2

Los logros que se persiguen, qué exige cada uno textualmente, cómo se resolvió, y cómo verificarlo.

Se persiguen **todos los alcanzables por código**: los nueve que tocan la gramática, más la
recuperación en modo pánico, más el *carry-over* de floats de la Etapa 1. Los tres restantes
—*Entrega Anticipada*, *Imbatibilidad* y *Well Done!*— no dependen de la implementación.

> **Estado.** Los diez están **implementados y verificados**, modo pánico incluido. Cómo se
> comprueba cada uno está en [`README.md`](README.md).

## Cómo se verifica cualquiera de estos

Los logros **no son configurables**: la gramática es una sola y los tiene todos. Para probar la
sintaxis de uno se le da al simulador la cadena de tokens que ese logro habilita:

```bash
cd compilador
python3 herramientas/primeros.py gramatica/minijava-ll1-final.g \
        --aceptar "class idClase { void idMetVar ( ) { idMetVar = true ? intLiteral : intLiteral ; } } eof"
bash herramientas/casos-gramatica.sh          # los 81 casos de aceptacion y rechazo
```

---

## 1. MiniLambdas E2

> *«El Analizador Sintáctico permite expresiones Lambda, con una sintaxis similar a la utilizada en
> Java. A diferencia de Java, los parámetros de una Lambda no pueden declarar tipo explícitamente y
> el cuerpo de una Lambda debe estar formado por una única expresión. No se permiten cuerpos
> delimitados por llaves. No se permite la captura de variables locales, parámetros, ni el uso de
> `this` dentro de una Lambda. Se permiten Lambdas con cualquier cantidad de parámetros, incluyendo
> ninguno.»*

| Cláusula | Dónde se resuelve |
|---|---|
| forma `params -> expr` | gramática |
| dónde puede aparecer una lambda | gramática — se produce en `<Expresion>`, no en `<Primario>` |
| parámetros sin tipo | gramática — no hay producción que genere `(int x) -> e` |
| cuerpo de una única expresión, sin llaves | gramática — no hay alternativa con `{` |
| no captura de variables | **analizador semántico** (Etapa 3) |
| no `this` | **analizador semántico** (Etapa 3) |
| parámetros sin nombre repetido | **analizador semántico** (Etapa 3) |
| el parámetro es un identificador sin tipo | **analizador**, con lo que acaba de leer |
| lambda parentizada sin cola | **analizador**, con lo que acaba de leer |

Las cuatro formas de parámetros funcionan: `() -> e`, `x -> e`, `(x) -> e`, `(a, b, c) -> e`.

**El caso difícil de verdad: dónde vive la lambda.** *«Sintaxis similar a la utilizada en Java»*
incluye **dónde** puede aparecer, y ahí Java no la trata como un valor más: el JLS define
`Expression: LambdaExpression | AssignmentExpression`, o sea que la lambda es alternativa del nivel
más alto de la jerarquía de expresiones y nunca un `Primary`. Producirla en `<Primario>` —que es la
ubicación que se impone sola— haría válidos `1 + x -> x`, `- x -> x`, `a == x -> x`,
`(x -> x) + 1` y `(x -> x).m()`, que `javac` rechaza con *«lambda expression not expected here»*.
Se produce entonces en el nivel de `<Expresion>`, factorizando las dos cabezas que comparte con una
expresión normal. El análisis completo, con la ambigüedad que eso elimina, está en
[`decisiones-de-logica.md` §2.2](decisiones-de-logica.md).

**El otro caso difícil, y por qué no hizo falta lookahead ilimitado.** El `(` abre tanto una expresión
parentizada como una lista de parámetros, y `(a, b, c) -> x` contra `(a + b)` parece exigir escanear
hasta el `)`. No lo exige: `( )` sólo puede ser una lambda de cero parámetros; `( e ,` sólo puede
ser una lambda porque MiniJava no tiene operador coma; y `( e )` lo decide el token
**inmediatamente** posterior al `)`. En los tres casos alcanza un token.

Las partes que resuelve el analizador y no la gramática están detalladas en
[`decisiones-de-deteccion-de-errores.md` §3](decisiones-de-deteccion-de-errores.md). La
permisividad del primer parámetro está en
[`decisiones-de-logica.md` §9](decisiones-de-logica.md).

**Lo que se difiere al semántico, por indicación del profesor.** El `this` —explícito e implícito—,
la captura de variables locales y de parámetros, y que los parámetros no se repitan **no los
controla esta etapa**: son restricciones de significado, el logro no las pide, y con la tabla de
símbolos del semántico se resuelven mucho más directo. `x -> this.campo`, `x -> x + n`,
`x -> y -> x` y `(a, a) -> a` se aceptan.

El motivo técnico es que ninguna es decidible con información local: `x -> campo` y `x -> n` son la
misma forma sintáctica. El desarrollo completo, con la frontera exacta y los ejemplos, está en
[`decisiones-de-deteccion-de-errores.md` §3](decisiones-de-deteccion-de-errores.md) — acá no se
repite.

**Requiere el token `->`**, que no existe en el léxico de la Etapa 1.

## 2. Variables locales Clásicas E2

> *«El compilador permite declarar variables locales de la forma clásica de Java, es decir, sin
> usar `var` e indicando su tipo (por ej `int x;`). Además permite en una misma sentencia declarar
> varias variables e inicializarlas (por ejemplo `int x,y,z = 10;`)»*

Resuelto en la gramática con `<ListaDeclaradores>`, con una `<InicializacionDeDeclarador>` por
declarador.

**El conflicto que crea:** una declaración local pasa a poder empezar con `idClase` (`C x;`), y una
sentencia-expresión también (`C.m();`). Se factoriza el `idClase` en `<Sentencia>`: `.` significa
expresión, cualquier otro token significa declaración. La rama de expresión reusa tres no
terminales de cola —`<ExpresionSinIdClase>`, `<ColaTrasOperando>`,
`<ColaDeExpresionDesdePrimario>`— para no duplicar la jerarquía de expresiones.

## 3. Visibilidad Mejorada E2

> *«El analizador sintáctico permite indicar implícita y explícitamente la visibilidad de Métodos,
> Atributos y Constructores. De esta manera al declararse uno de los miembros se podrá obviar la
> visibilidad o indicarse explícitamente que es public o private.»*

`<VisibilidadOpcional> ::= public | private | ε` al frente de los tres tipos de miembro.

**El conflicto que crea:** el `public` del constructor deja de ser lo que lo distingue, y el
constructor pasa a competir con el atributo o método de tipo referencia por el token `idClase`. Se
factoriza: `(` es constructor, cualquier otro token abre un tipo.

Los métodos de interfaz admiten `public` y no `private` — el razonamiento está en
[`decisiones-de-logica.md` §5](decisiones-de-logica.md). El orden respecto de `static` se mantiene
fiel a la gramática original (§6 del mismo documento).

**Requiere el token `private`.**

## 4. Fors! E2

> *«El compilador permite declarar sentencias de tipo `for` similar a como es en java. Se restringe
> a una sola sentencia/expresión por separador del for (`;` en el caso del standard y `:` en el for
> de iteradores)»*

Las dos formas están. El desempate entre el `for` estándar y el de iteradores es de **un solo
token** y está justo después del declarador: `:` es de iteradores, `=` o `;` es estándar. La
restricción de «una sola por separador» queda en la forma de `<ClausulasFinalesFor>`, que admite una
`<ExpresionOpcional>` por hueco.

El encabezado tiene el mismo conflicto de `idClase` que la sentencia y se resuelve igual,
reutilizando los mismos no terminales de cola.

**Requiere el token `for`.**

## 5. Operador Ternario E2

> *«El compilador permite el uso del operador condicional ternario dentro de las expresiones como
> en Java (condición ? expr_verdadera : expr_falsa)»*

Una capa nueva, `<ExpresionCondicional>`, entre la asignación y los operadores binarios, con la
precedencia de Java y asociatividad a derecha. Alcanza a los seis lados derechos de un `=`; el
detalle está en [`decisiones-de-logica.md` §3](decisiones-de-logica.md).

**Requiere el token `?`**, que en el léxico de la Etapa 1 era un error.

## 6. Operadores Posifijos E2

> *«El Analizador Sintáctico permite usar los operadores unarios postfios de incremento (++) y
> decremento (--).»*

`<PosfijoOpcional>` después de `<Referencia>` y **no** de `<Operando>`: `5++` no tiene sentido.

Es el **único logro que no requiere tocar el léxico**: `++` y `--` ya son tokens desde la Etapa 1
—están en «Aspectos Léxicos» §2.4.2— y no tenían ninguna regla de la gramática que los usara. Este
logro es el que se los da.

## 7. Atributos Inicializados

> *«El compilador permite realizar inicializaciones de los atributos cuando son declarados, al
> igual que en Java»*

`<InicializacionDeAtributo>` en la cola del atributo. Es un no terminal **dedicado**, distinto del
de las variables locales, porque son logros distintos y tienen que poder prenderse por separado.

## 8. Inicializacion de Arreglos E2

> *«El Analizador Sintáctico permite la inicialización de Arreglos al momento de su construcción
> como en Java. Solo se permite como parte de una expresión de creación utilizando `new`.»*

El inicializador es **anidable**, porque el logro dice «como en Java» y la única restricción que
enuncia es dónde: `new int[][]{{1,2},{3,4}}` es válido. Que sólo valga bajo `new` queda impuesto por
la gramática, porque el inicializador sólo es alcanzable desde `<Creacion>`.

## 9. Genericidad Avanzada E2

> *«El Analizador Sintáctico permite Tipos Genéricos Anidados y la notación Diamante, de forma
> similar a Java. Aun así, una Clase o Interfaz continúa limitada a un único Parámetro de Tipo. La
> notación Diamante solo puede utilizarse al instanciar una Clase Genérica.»*

Anidamiento por una sola recursión. El diamante **restringido por gramática** a la posición de
instanciación, no delegado al semántico. La tabla de qué acepta y qué rechaza está en
[`decisiones-de-logica.md` §8](decisiones-de-logica.md).

## 10. Recuperación de Errores Modo Pánico

> *«El compilador no finaliza la ejecución ante el primer error sintáctico, sino que se recupera
> (Modo Pánico) y es capaz de reportar otros errores. Para ello, al encontrar un error, el
> analizador descarta la entrada hasta encontrar un token de sincronización que le permita reanudar
> el análisis. Se espera que se sincronice con el siguiente punto y coma, llave de cierre, o llave
> que abre segun el contexto.»*

No toca la gramática: es enteramente implementación, y está siempre activa.

**Implementado.** Tres puntos de recuperación, y sólo tres: las tres listas de la gramática que
repiten un elemento (`ListaClases`, `ListaMiembros` y `ListaSentencias`). Sincroniza con los tokens
que nombra la consigna **más los Primeros del elemento de la lista** —sin eso, un `int x` al que le
falta el `;` se lleva puesto el método que viene después, porque el análisis falla justo en el
`void` con el que empieza el miembro siguiente y correcto— y cuenta las llaves mientras descarta,
para saltear entero el cuerpo de un método cuya firma está rota.

El **primer error reportado es exactamente el que reportaría un analizador que cortara ahí**, que
es la convención base de las Pautas slide 25: la recuperación agrega errores después del primero,
nunca altera cuál es el primero. El detalle está en
[`decisiones-de-deteccion-de-errores.md`](decisiones-de-deteccion-de-errores.md) §5.

## 11. Floats — carry-over de la Etapa 1

El léxico emite `pr_float` y `floatLiteral` desde la Etapa 1 pero la gramática de la cátedra no los
tiene. Dos alternativas nuevas, cero conflictos. Sin esto, el logro de la etapa anterior quedaría
inutilizable en ésta.

---

## Resumen del impacto sobre el léxico

| Token | Logro | Estado en la Etapa 1 |
|---|---|---|
| `for` | Fors! E2 | se reconoce como `idMetVar` |
| `private` | Visibilidad Mejorada E2 | se reconoce como `idMetVar` |
| `?` | Operador Ternario E2 | es un error léxico |
| `->` | MiniLambdas E2 | se reconoce como `-` seguido de `>` |
| `:` `++` `--` | ternario, for-each, posfijos | **ya existían** |

Los cuatro primeros **no figuran en «Aspectos Léxicos de MiniJava»**: la lista de palabras clave
de §2.1 llega hasta `false` y la de operadores de §2.4.2 hasta `++ --`. Se agregan al léxico sin
condición: el compilador reconoce MiniJava con todos los logros incorporados, así que la columna de
la derecha describe lo que pasaba en la Etapa 1, no una configuración alcanzable hoy.

## Ver también

- [`../lexico/resolucion-de-logros.md`](../lexico/resolucion-de-logros.md) — los 8 de la Etapa 1.
- [`decisiones-de-logica.md`](decisiones-de-logica.md) — el detalle de cada decisión de lenguaje.
- [`decisiones-de-diseno.md`](decisiones-de-diseno.md) — las decisiones cerradas de la etapa.
