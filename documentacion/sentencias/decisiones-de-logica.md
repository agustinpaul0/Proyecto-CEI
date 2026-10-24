# Decisiones de lógica

Qué controla la etapa. La fuente es *MiniJava-Semantica-Completa-2026* («el documento»), en
[`../minijava/`](../minijava/README.md). Lo que el documento no dice se resuelve como en Java,
verificado con `javac` 25 caso por caso. Lo que contestó el profesor está en
[`../../CONVENCIONES-CATEDRA.md`](../../CONVENCIONES-CATEDRA.md).

## 1. Los logros son independientes entre etapas

Es el criterio que el profesor confirmó en la Etapa 3
([`../../CONVENCIONES-CATEDRA.md`](../../CONVENCIONES-CATEDRA.md) §8): los programas con que se
prueba una etapa no traen construcciones de logros que no se persiguen en ella. En la Etapa 4 no se
persigue ningún logro de código, así que el chequeo es el del MiniJava base del documento.

El parser sigue aceptando lo que agregan los logros de la Etapa 2, porque esa etapa se corrige
aparte. En la Etapa 4 esas construcciones son como si el sintáctico las rechazara: el AST las
representa con un nodo propio que, si llega a chequearse, da un error semántico. No se acepta nada
sin controlarlo.

| Construcción (logro de la Etapa 2) | Nodo | Logro de la Etapa 4 que la controlaría |
|---|---|---|
| `for (...) ...` | `SentenciaNoControlada` | For E4 |
| `int x = 1;`, `Aa x;` | `SentenciaNoControlada` | Variables Locales Clásicas Controladas |
| `c ? a : b` | `ExpresionNoControlada` | Ternario E4 |
| `x++`, `x--` | `ExpresionNoControlada` | Postfijos – Chequeado |
| `new int[]{1, 2}` | `ExpresionNoControlada` | — |
| `new Caja<>()` | `ExpresionNoControlada` | Genericidad Avanzada |
| `int x = 5;` como atributo | la expresión se reconoce y se descarta | Atributos Inicializados – Chequeado |

`float` es un tipo primitivo más, como en la Etapa 3: un literal `1.5` tiene tipo `float`, que
conforma sólo consigo mismo y no participa de ningún operador. Controlar sus usos es el logro Floats
y Coerción.

El caso de la cátedra `semIICorrecto03` traía `a1++ + 10`. El profesor confirmó que es un error del
caso: va `a1+4+ + 10`.

## 2. Qué se controla

Cada control con su sección del documento:

| Construcción | Control |
|---|---|
| Nombre sin receptor (§10.1) | local visible, después parámetro, después atributo de la clase con los heredados; si no hay ninguno, error. En un método estático, un atributo sin receptor es un error. |
| `this` (§11.3, §10.4) | vale en constructores y métodos de instancia; su tipo es la clase con su parámetro: `Caja<T>` dentro de `Caja<T>`. |
| Literales (§11.1) | `int`, `char`, `boolean`, `String` y el tipo especial de `null`. |
| Operadores (§11.2) | aritméticos y relacionales sobre `int`; `&&`, `||` y `!` sobre `boolean`; `==` y `!=` entre tipos que conforman en alguna dirección. Sin promoción ni concatenación. |
| Paréntesis (§11.4) | conservan el tipo y nada más: no son asignables, ni asignación, ni llamada, tampoco como base de un encadenado. |
| Acceso a atributo (§10.1, §11.5) | se busca en el tipo estático del receptor, visto con su sustitución genérica. Primitivos, `void`, `null` y `T` no tienen miembros; las interfaces no tienen atributos; un arreglo tiene `length`, de tipo `int` y no asignable. |
| Llamada (§10.2, §11.6) | se elige el método por `(nombre, aridad)`: sin receptor en la clase actual, con receptor en su tipo estático, con `Clase.m` en una clase que tiene que existir y ser clase. Después, cada argumento conforma con su parámetro visto desde el receptor. Sin receptor, en un estático, sólo se elige un estático; con `Clase.m`, el método tiene que ser estático; con un receptor, puede ser cualquiera. |
| Creación (§10.3, §11.7) | el tipo es válido (existe, con argumento si es genérico y sin él si no), es una clase y tiene un constructor de esa aridad, sin buscar en ancestros. El resultado es el tipo completo. |
| Arreglos (§11.8) | el tipo base es válido, cada tamaño es `int`, el resultado tiene una dimensión por tamaño. Se indexa sólo un arreglo, con un índice `int`, y el resultado tiene una dimensión menos. |
| Asignación (§11.9) | el lado izquierdo es asignable —una variable, un atributo o un componente, por la forma de su último acceso— y el tipo de la derecha conforma con el de la izquierda. Su valor es el del destino. |
| Sentencia-expresión (§12.2) | es una asignación o una llamada (a método o a constructor), no parentizada. |
| `var` (§12.3) | el inicializador se chequea antes de declarar la variable; su tipo no puede ser el de `null` ni `void`; el nombre no repite un parámetro ni una local visible. |
| `if` y `while` (§12.4) | la condición es `boolean`; el cuerpo directo no puede ser una declaración. Se chequean todas las ramas. |
| `return` (§12.5) | sin valor en métodos `void` y constructores; con un valor que conforma con el retorno en los demás. Un valor `void` es un error, incluso en un método `void`. |
| Bloques (§12.6) | las sentencias se chequean en orden, también después de un `return`, y las locales dejan de verse al cerrar el bloque. |

La conformidad es la del §9: está en
[`arquitectura-del-chequeo-de-sentencias.md`](arquitectura-del-chequeo-de-sentencias.md), «Los tipos».

## 3. Qué no se controla

Lo que el documento deja afuera explícitamente:
- que un método con retorno devuelva un valor en todos los caminos, y el código inalcanzable (§12.5);
- la existencia, unicidad o signatura del `main` (§8 y §14): es de la Etapa 5;
- los valores en ejecución: tamaños negativos, índices fuera de rango, referencias nulas (§11.8).

## 4. Cambios al chequeo de declaraciones

El documento completo reemplaza a la Parte I, que era la fuente de la Etapa 3. Tres cosas cambian:

- **Tipos raw** (§5.2). Fuera de `extends` e `implements`, una clase o interfaz genérica sin
  argumento es un error: también en atributos, parámetros, retornos y tipos base de arreglos, que se
  controlan en las declaraciones. La Parte I, §3, lo permitía; el profesor confirmó el documento
  completo, y que en la Etapa 3 también debía ser error.
- **El argumento de tipo** (§3, 4.º párrafo) es un parámetro válido o una clase o interfaz **no
  genérica**: `Caja<Caja>` es un error. Un argumento con su propio argumento (`Caja<Par<String>>`)
  es del logro Genericidad Avanzada; se controla entero, como cualquier otro tipo.
- **Un solo parámetro de tipo** (§5, aclarado por el profesor). Al heredar sin argumento, el tipo
  conserva el parámetro de su ancestro, así que no puede declarar otro: `class Der<T> extends Base`,
  con `Base<T>`, es un error en `Base`. Se hereda con argumento (`Base<T>`, `Base<String>`) o sin
  parámetro propio (`class Der extends Base`).

Los dos rigen también con `--etapa=3`, porque el chequeo de declaraciones es uno solo.

## 5. Dos parámetros de tipo con el mismo nombre son tipos distintos

Documento §5.1: «Para representar los tipos se debe conservar su declaración de origen, además del
nombre». La `T` que `class Derivada extends Base` conserva de `Base` no es la `T` de otra clase
genérica: dentro de `Par<T>`, `d.obtener()` con `d` de tipo `Derivada` no conforma con la `T` de
`Par`.

## 6. Donde la cátedra fija el orden, no se sigue a Java

El profesor confirmó los dos.

| Pregunta | La cátedra | Java |
|---|---|---|
| En una asignación, ¿qué se reporta primero: un destino no asignable o un error del lado derecho? | Clase 11, slides 26, 31 y 36: se chequean los dos lados, después la conformidad y al final la asignabilidad. `this = x;` con `x` sin declarar da `[Error:x\|N]`. | `javac` reporta primero `cannot assign to 'this'`. |
| ¿Se controla la forma de una sentencia-expresión antes que sus tipos? | No: «Cada expresión chequea sus subexpresiones antes de determinar su resultado» (documento §11, 1.er párrafo). `true + zz;` da `[Error:zz\|N]`. | `javac` reporta primero «not a statement», un error de su parser. |

## 7. Resueltas como en Java

Revisadas contra el documento completo, las Pautas y las Clases 09 a 11: ninguna de estas tiene una
regla de la cátedra que la contradiga.

| Pregunta | Java | Decisión |
|---|---|---|
| ¿Un receptor de tipo interfaz ofrece `toString()`? El §10.2 nombra sólo los métodos declarados y heredados de la interfaz. | Sí: una interfaz tiene los métodos públicos de instancia de `Object` (JLS §9.2). `debugPrint` es estático y no es miembro de la interfaz. | Igual que Java; lo confirmó el profesor. |
| ¿Se controla que el cuerpo de un `if` no sea una declaración antes que la declaración? | Sí: «variable declaration not allowed here» es un error del parser. | Igual que Java. |
