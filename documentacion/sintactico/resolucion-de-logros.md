# Resolución de los logros de la Etapa 2

Los logros que se persiguen, qué exige cada uno textualmente, cómo se resolvió, y cómo verificarlo.

> **Estado.** Los nueve están **implementados y verificados**, modo pánico incluido. Cómo se
> comprueba cada uno está en [`README.md`](README.md).

## Cómo se verifica cualquiera de estos

Los logros **no son configurables**: la gramática es una sola. Para probar la
sintaxis de uno se le da al simulador la cadena de tokens que ese logro habilita:

```bash
cd compilador
python3 herramientas/primeros.py gramatica/minijava-ll1-final.g \
        --aceptar "class idClase { void idMetVar ( ) { idMetVar = true ? intLiteral : intLiteral ; } } eof"
bash herramientas/casos-gramatica.sh          # los 67 casos de aceptacion y rechazo
```

---

## 1. Variables locales Clásicas E2

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

## 2. Visibilidad Mejorada E2

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

## 3. Fors! E2

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

## 4. Operador Ternario E2

> *«El compilador permite el uso del operador condicional ternario dentro de las expresiones como
> en Java (condición ? expr_verdadera : expr_falsa)»*

Una capa nueva, `<ExpresionCondicional>`, entre la asignación y los operadores binarios, con la
precedencia de Java y asociatividad a derecha. Alcanza a los seis lados derechos de un `=`; el lado
izquierdo es una `<ExpresionCompuesta>`, como en la gramática original, así que `a ? b : c = d` no
deriva. El detalle está en [`decisiones-de-logica.md` §3](decisiones-de-logica.md).

**Requiere el token `?`**, que en el léxico de la Etapa 1 era un error.

## 5. Operadores Posifijos E2

> *«El Analizador Sintáctico permite usar los operadores unarios postfios de incremento (++) y
> decremento (--).»*

`<PosfijoOpcional>` después de `<Referencia>` y **no** de `<Operando>`: `5++` no tiene sentido.

Es el **único logro que no requiere tocar el léxico**: `++` y `--` ya son tokens desde la Etapa 1
—están en «Aspectos Léxicos» §2.4.2— y no tenían ninguna regla de la gramática que los usara. Este
logro es el que se los da.

## 6. Atributos Inicializados

> *«El compilador permite realizar inicializaciones de los atributos cuando son declarados, al
> igual que en Java»*

`<InicializacionDeAtributo>` en la cola del atributo. Es un no terminal **dedicado**, distinto del
de las variables locales, porque son logros distintos y el semántico va a querer distinguirlos.

## 7. Inicializacion de Arreglos E2

> *«El Analizador Sintáctico permite la inicialización de Arreglos al momento de su construcción
> como en Java. Solo se permite como parte de una expresión de creación utilizando `new`.»*

Como en Java: con inicializador ninguna dimensión lleva tamaño, así que `new int[][]{{1,2},{3,4}}`
es válido y `new int[2][2]{{1,2},{3,4}}` no. El inicializador es **anidable** y admite la coma final
(`{1, 2,}`). Sin inicializador rige la creación de MiniJava base, con toda dimensión con su tamaño.
Que el inicializador sólo valga bajo `new` queda impuesto por la gramática, porque sólo es
alcanzable desde `<Creacion>`. El detalle está en [`decisiones-de-logica.md` §7](decisiones-de-logica.md).

## 8. Genericidad Avanzada E2

> *«El Analizador Sintáctico permite Tipos Genéricos Anidados y la notación Diamante, de forma
> similar a Java. Aun así, una Clase o Interfaz continúa limitada a un único Parámetro de Tipo. La
> notación Diamante solo puede utilizarse al instanciar una Clase Genérica.»*

Anidamiento por una sola recursión. El diamante **restringido por gramática** a la posición de
instanciación, no delegado al semántico. La tabla de qué acepta y qué rechaza está en
[`decisiones-de-logica.md` §8](decisiones-de-logica.md).

## 9. Recuperación de Errores Modo Pánico

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
[`decisiones-de-deteccion-de-errores.md`](decisiones-de-deteccion-de-errores.md) §4.

## 10. Floats — carry-over de la Etapa 1

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
| `:` `++` `--` | ternario, for-each, posfijos | **ya existían** |

Los tres primeros **no figuran en «Aspectos Léxicos de MiniJava»**: la lista de palabras clave
de §2.1 llega hasta `false` y la de operadores de §2.4.2 hasta `++ --`. Se agregan al léxico sin
condición: la columna de la derecha describe lo que pasaba en la Etapa 1, no una configuración
alcanzable hoy.

## Ver también

- [`../lexico/resolucion-de-logros.md`](../lexico/resolucion-de-logros.md) — los 8 de la Etapa 1.
- [`decisiones-de-logica.md`](decisiones-de-logica.md) — el detalle de cada decisión de lenguaje.
- [`decisiones-de-diseno.md`](decisiones-de-diseno.md) — las decisiones cerradas de la etapa.
