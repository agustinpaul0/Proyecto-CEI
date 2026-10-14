# Decisiones de detección de errores

## El reporte

Es el de las Pautas Etapa 4, slides 30-31, igual al de las etapas anteriores: el mensaje, un
renglón en blanco y el código.

```
Error Semantico en linea 3: El operador + no admite los tipos int y boolean

[Error:+|3]
```

**El primer error termina el análisis.** Seguir después de un error en una sentencia es el logro
Multi-Detección Errores Semánticos en Sentencias, que no se persigue. Las sentencias se chequean
sólo si las declaraciones están bien (Pautas Etapa 4, slide 22): un error de declaración se reporta
aunque aparezca en el archivo después de un error de sentencia.

## El orden

Cada nodo chequea primero a sus hijos, de izquierda a derecha, y después a sí mismo (Clase 10,
slides 25-28). El primer error que aparece es el que se reporta:

- las clases, en el orden en que se declaran; en cada clase, sus constructores y métodos en el orden
  del fuente, como `javac`;
- en una binaria, el operando izquierdo, el derecho y el operador;
- en una llamada, primero se elige el método, y recién después se chequean los argumentos, en orden
  (documento §10.2, último párrafo, y §11.6);
- en una asignación, los dos lados, la conformidad y por último si el lado izquierdo es asignable,
  como el pseudocódigo de `NodoExpAsig` en la Clase 11 (slides 26, 31 y 36);
- en una sentencia-expresión, la expresión antes que su forma: «Cada expresión chequea sus
  subexpresiones antes de determinar su resultado» (documento §11, 1.er párrafo);
- en un `if` o un `while`, que el cuerpo no sea una declaración antes que el cuerpo, como `javac`.

## Qué token se reporta

Las Pautas Etapa 4, slide 30, piden «el lexema del token correspondiente al nodo que tiene el error».
Los casos de la cátedra fijan dos:

- `semIIError02` y `semIIError03`: en una asignación incompatible o con un destino no asignable, el
  `=`;
- `semIIError04`: con un argumento que no conforma con su parámetro, el nombre del método.

Para el resto, el criterio es el de la Etapa 3
([`../../CONVENCIONES-CATEDRA.md`](../../CONVENCIONES-CATEDRA.md) §5): **el token que marca
`javac`**, verificado caso por caso con `javac` 25. `javac` marca un token representativo de cada
expresión: el operador de una binaria o unaria, el `(` de los argumentos de una llamada, el `.` de un
acceso a atributo, el `[` de una indexación, el `new` de una creación, el nombre de una variable. Los
paréntesis de una expresión parentizada no cuentan: el error va adentro. Ese token es el que
devuelve `NodoExpresion.token()`.

Cada fila tiene al menos un caso en `compilador/resources/conErrores/`.

| Error | Ejemplo | Token | Fuente |
|---|---|---|---|
| Nombre no declarado | `zz = 1;` | `zz` | `javac`; Pautas slide 31 |
| Atributo sin receptor en un estático | `static void m() { x = 1; }` | `x` | `javac` |
| `this` en un estático | `var a = this;` | `this` | `javac` |
| Asignación incompatible | `b = a;` | `=` | cátedra (`semIIError02`) |
| Destino no asignable | `new Aa() = 5;` | `=` | cátedra (`semIIError03`) |
| Operador con tipos que no admite | `i + true` | `+` | Pautas slide 30 |
| Método sin receptor no declarado, o sin esa aridad | `f(1, 2);` | `f` | `javac` |
| Método de instancia sin receptor en un estático | `n();` | `n` | `javac` |
| Argumento que no conforma con su parámetro, en un método | `f(true)`, `a.f(true)`, `Aa.f(true)` | `f` | cátedra (`semIIError04`) — consultado (duda 6) |
| Argumento que no conforma con su parámetro, en un constructor | `new Aa(true)` | `true` | `javac` — consultado (duda 6) |
| Método o atributo no declarado en el receptor | `a.f();`, `a.x` | `.` | `javac` |
| Miembro de un tipo que no tiene miembros: primitivo, `void`, `T` | `x.f()`, `v().x` | `.` | `javac` |
| Clase no declarada en `Clase.m()` | `Nada.f();` | `Nada` | `javac` |
| Interfaz en `Clase.m()` | `Ix.f();` | `Ix` | el nombre del tipo, como la clase no declarada |
| Método no estático o inexistente en `Clase.m()` | `Aa.n();` | `.` | `javac` |
| `new` de una interfaz | `new Figura()` | `new` | `javac` |
| `new` de un tipo inválido: no declarado, raw, con argumento sobre una no genérica | `new Nada()`, `new Caja()` | el nombre del tipo | `javac`; como en la Etapa 3 |
| Constructor sin esa aridad | `new Aa(1)` | `new` | `javac` |
| Tamaño de arreglo que no es `int` | `new int[true]` | el tamaño | `javac` |
| Índice que no es `int` | `v[true]` | el índice | `javac` |
| Indexar algo que no es un arreglo | `n[0]` | `[` | `javac` |
| Condición que no es `boolean` | `if (x)`, `while (n())` | la condición: `x`, `(` | `javac` |
| `var` con `null` o `void` | `var x = null;` | `x` | `javac` |
| Local que repite un parámetro o una local visible | `var x = 2;` | `x` | `javac`; `semIIError05` |
| Declaración como cuerpo directo de `if`, `else` o `while` | `if (c) var x = 1;` | `x` | `javac` |
| `return;` en un método con retorno | `return;` | `return` | `javac` |
| `return e;` en un método `void` o un constructor, o con un valor que no conforma | `return 1;` | la expresión: `1` | `javac` |
| Sentencia que no es asignación ni llamada | `x + 1;`, `v[0];`, `(x = 3);` | la expresión: `+`, `[`, `(` | `javac` |
| Construcción de un logro que no se controla | `for`, `?`, `++`, `int`, `new` | su primer token | sin consulta: no aparece en los programas de prueba |

Una expresión parentizada como sentencia (`(x = 3);`) es el único lugar donde el `(` del paréntesis
es el token, como en `javac`. En `(o).m();`, que `javac` acepta y el documento rechaza (§11.4), va el
token de la llamada, el `(` de sus argumentos.

## La suite de Santiago Salamanca

De sus 137 casos, coincidimos en el token con 115. De los otros 22:
- 18 difieren en el token donde la cátedra no lo fija, y seguimos a `javac`;
- `semIIError20` (`this = x;` con `x` sin declarar) espera el `=`; seguimos el orden de la Clase 11,
  que chequea los dos lados antes que la asignabilidad, y el error es la `x`;
- `semIIError61`, una herencia circular, que reportamos en la primera clase del ciclo, como en la
  Etapa 3;
- `semIICorrecto03` es el caso de la cátedra con `a1++` (§1 de
  [`decisiones-de-logica.md`](decisiones-de-logica.md));
- `semIICorrecto56` no es correcto: llama a `crear()` desde una clase que no lo declara.

Sus casos están importados en el corpus con los tokens de `javac` (`compilador/resources/CREDITOS.md`).
