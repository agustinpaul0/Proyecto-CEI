# Decisiones de lógica

Qué controla la etapa. La fuente es el documento «MiniJava-Semantica-2026-Parte 1». Lo que el
documento no dice se resuelve como en Java, verificado con `javac` 25.

## 1. Los logros son independientes entre etapas

El profesor indicó que los logros son independientes entre etapas. Por eso el semántico se
implementa para el **MiniJava base** del documento. Lo que agregan los logros de la Etapa 2 se sigue
aceptando sintácticamente, pero no suma controles:

| Construcción (logro de la Etapa 2) | En la Etapa 3 |
|---|---|
| `private` y `public` (Visibilidad Mejorada) | no se registran; no afectan ningún control |
| `Caja<Par<String>>` (Genericidad Avanzada E2) | se controla entero, como cualquier otro tipo |
| `int x = 5;` (Atributos Inicializados) | el atributo se registra; la expresión no se controla |
| `float` (logro de la Etapa 1, confirmado por el profesor) | es un tipo primitivo más |

El profesor confirmó el criterio: si un logro no se continúa, los programas con que se prueba esta
etapa no traen esa construcción ([`CONVENCIONES-CATEDRA.md`](../../CONVENCIONES-CATEDRA.md) §8).

## 2. Los cuerpos no se chequean

Documento §1. Un cuerpo con nombres no declarados compila: `sintCorrecto04`, de la cátedra, tiene
`this.b().a = 5;` sin `b`. Tampoco se mira el inicializador de un atributo.

## 3. Tipos

- Un tipo referencia nombra una clase, una interfaz o una predefinida (documento §3).
- Con argumento, el tipo tiene que ser genérico. El argumento es una `T` válida o una clase o
  interfaz existente y no genérica (documento completo §3).
- Un tipo genérico sin argumento (`Caja`, la forma raw) vale sólo en `extends` e `implements`
  (documento completo §5.2). En un atributo, un parámetro, un retorno o el tipo base de un arreglo
  es un error.
- Una `T` vale sólo dentro de la clase o interfaz que la declara. Tampoco vale en un método
  estático, como en Java: «non-static type variable T cannot be referenced from a static context».
  El documento no distingue los métodos estáticos.
- Un arreglo es válido si su tipo base lo es.

## 4. Herencia

- Una clase extiende una clase y implementa una interfaz; una interfaz extiende una interfaz
  (documento §4). La gramática ya impide que una clase haga las dos cosas a la vez.
- Sin `extends`, una clase extiende `Object`, también cuando implementa una interfaz.
- `String` y `System` se pueden extender: `final` es parte del logro Sobrecarga! E3, que no se
  persigue (respuesta del profesor). Una hija de `System` hereda sus diez métodos estáticos.
- La herencia es acíclica, entre clases y entre interfaces. Una clase que extiende a otra de un
  ciclo sin ser parte de él no es la que se reporta ([`decisiones-de-deteccion-de-errores.md`](decisiones-de-deteccion-de-errores.md)).
- Una clase puede extender otra declarada más adelante: la tabla se completa antes de chequear.

## 5. Genericidad en la herencia

Documento §5:
- con argumento (`extends Caja<String>`), los tipos de lo heredado se instancian: `T` pasa a
  `String`, `T[]` pasa a `String[]` y `Caja<T>` pasa a `Caja<String>`;
- sin argumento (`extends Caja`), la `T` se conserva, y la hija hereda `T obtener()` aunque ella no
  declare `T`;
- la instanciación se encadena: `Otra<U> extends Caja<U>` hereda `U obtener()`, y
  `Final extends Otra<Object>` hereda `Object obtener()`.

Un tipo tiene a lo sumo un parámetro, contando el que conserva (indicación del profesor): si hereda
sin argumento de un tipo con parámetro, propio o conservado, no puede declarar el suyo.
`class Der<T> extends Caja` es un error; `class Der<T> extends Caja<T>` y
`class Der extends Caja` valen. Lo mismo con `implements` y con la extensión de interfaces.

Las redefiniciones se comparan contra lo heredado **ya instanciado**. Consecuencia de la forma
cruda: `class Aa implements Ix` con `interface Ix<T> { T m(); }` no se puede implementar, porque
`Aa` no puede declarar `T`.

## 6. Miembros

- **Atributos**: no hay dos con el mismo nombre en una clase, ni uno con el nombre de un heredado
  (documento §6.1). Un atributo y un método pueden llamarse igual.
- **Métodos**: la clave es `(nombre, aridad)`. Dos con la misma clave en una clase o interfaz son un
  repetido, aunque cambien los tipos o uno sea estático (documento §6.2).
- **Redefinición**: un método de instancia con la clave de uno heredado de instancia lo redefine, y
  la signatura tiene que ser exactamente la misma: los tipos de los parámetros, en orden, y el
  retorno. Los nombres de los parámetros no cuentan.
- **Estáticos**: no se redefinen. Ninguna descendiente declara la clave de un estático heredado, ni
  un estático con la clave de uno de instancia heredado. `Object.debugPrint(int)` es estático, así
  que ninguna clase puede declarar `debugPrint` con un parámetro.
- **Constructores**: se llaman como su clase, y dos con la misma aridad son un repetido (documento
  §6.3). Si no hay ninguno, se agrega el constructor por defecto.
- **Parámetros**: no se repiten en un mismo método o constructor, y cada uno tiene un tipo válido
  (documento §6.4).

## 7. Interfaces

- Una clase que implementa una interfaz tiene, propio o heredado, un método de instancia con la
  misma signatura para cada método de la interfaz consolidada. Un estático no implementa (documento
  §6.2).
- Una interfaz que extiende otra hereda sus métodos, y los redefine con la misma signatura.
- Una interfaz incluye los métodos públicos de instancia de `Object`, como en Java:
  `interface Ix { int toString(); }` es error, con el mensaje «toString() in Ix cannot override
  toString() in Object». `debugPrint` es estático, así que no cuenta:
  `interface Ix { void debugPrint(int i); }` es válida. Los métodos de `Object` no se suman a la
  tabla de la interfaz: una clase los tiene siempre por herencia.

## 8. Entidades predefinidas

Documento §7:
- `Object` declara `static void debugPrint(int i)` y `String toString()`;
- `String` hereda de `Object` y no tiene métodos propios;
- `System` hereda de `Object` y declara diez métodos estáticos.

Las tres están en la tabla antes de parsear (Clase 8, slide 17), tienen su constructor por defecto y
no se pueden redeclarar, ni como clase ni como interfaz.

## 9. `main` no se exige

El documento no lo pide, Java compila sin él, y el profesor confirmó que no se exige.
