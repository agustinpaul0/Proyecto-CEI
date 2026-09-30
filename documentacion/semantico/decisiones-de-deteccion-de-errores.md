# Decisiones de detección de errores

## El reporte

Es el de las Pautas Etapa 3, slides 28-29, con la misma forma que el sintáctico: el mensaje, un
renglón en blanco y el código.

```
Error Semantico en linea 7: La clase A1 ya esta declarada

[Error:A1|7]
```

**El primer error termina el análisis.** Seguir después de un error semántico es el logro
Multi-Detección, que no se persigue. Si el programa tiene errores léxicos o sintácticos, se
reportan como en la Etapa 2 y el semántico no corre (slides 20 y 25).

## Qué token se reporta

Lo fija la Pautas slide 30:

1. en un nombre repetido, la declaración que aparece **después**;
2. en un método mal redefinido, su nombre en la clase **hija**;
3. en un tipo no declarado, el token de ese tipo;
4. si no hay un token claramente asociado, el nombre de la entidad con el problema.

| # | Error | Ejemplo | Código |
|---|---|---|---|
| 1 | Clase o interfaz repetida, incluidas `Object`, `String` y `System` | `class Aa { }` `class Aa { }` en las líneas 1 y 2 | `[Error:Aa\|2]` (regla 1) |
| 2 | Atributo repetido | `int x;` `int x;` | el segundo `x` (regla 1) |
| 3 | Método repetido `(nombre, aridad)` | `void m(int a)` y `void m(char b)` | el segundo `m` (regla 1) |
| 4 | Constructor repetido (aridad) | `Aa(int x)` y `Aa(char c)` | el segundo `Aa` (regla 1) |
| 5 | Parámetro repetido | `void m(int a, char a)` | el segundo `a` (regla 1) |
| 6 | Constructor con otro nombre | `class Aa { Bb() { } }` | `Bb` (regla 4) |
| 7 | Herencia de un tipo no declarado | `class B2 extends C3` | `C3` (regla 3) |
| 8 | Herencia del tipo equivocado | `class Aa extends Ix`, con `Ix` interfaz | `Ix` (regla 3) |
| 9 | Herencia de una clase `final` | `class Aa extends String` | `String` (regla 3) |
| 10 | Argumento sobre un tipo no genérico | `Caja<String> x;`, con `Caja` no genérica | `Caja` (regla 3) |
| 11 | Argumento no declarado | `Caja<Nada> x;` | `Nada` (regla 3) |
| 12 | `T` fuera de la clase que la declara, o en un método estático | `class Aa { T x; }` | `T` (regla 3) |
| 13 | Tipo no declarado en un atributo, retorno o parámetro | `Nada[] x;` | `Nada` (regla 3) |
| 14 | Herencia circular | `class Aa extends Bb` `class Bb extends Aa` | `Aa`, la primera declarada del ciclo (regla 4) |
| 15 | Atributo con el nombre de uno heredado | `int x;` en `Aa` y en `Bb extends Aa` | el `x` de `Bb` (regla 1) |
| 16 | Redefinición con otra signatura | `void m1()` en `A1` e `int m1()` en `B2` | el `m1` de `B2` (regla 2) |
| 17 | Redeclaración de un estático heredado | `void debugPrint(int i)` en cualquier clase | ese `debugPrint` (regla 2) |
| 18 | Estático con la clave de uno de instancia heredado | `static String toString()` | ese `toString` (regla 2) |
| 19 | Interfaz que contradice a `Object` | `interface Ix { int toString(); }` | `toString` (regla 2) |
| 20 | Clase que no implementa un método de su interfaz | `class Aa implements Ix { }` | `Aa` (regla 4) |
| 21 | Implementación con otra signatura, o estática | `static void m()` para el `void m()` de `Ix` | `m`, si es propio; `Aa`, si es heredado |

Los tokens de los errores 14, 20 y 21 coinciden con los que marca `javac`. Los de los errores 10, 14
y 21 tienen una alternativa razonable, y están consultados en [`../../dudas.md`](../../dudas.md).

## En qué orden se detectan

El error reportado es el primero que se encuentra en este orden:

1. **el primer nombre repetido del fuente**, porque se detecta al parsear (documento §8);
2. **la segunda pasada**, tipo por tipo en orden de declaración. Dentro de cada tipo va primero la
   herencia, después el ciclo, después los atributos, los constructores y los métodos, y cada lista
   en el orden del fuente;
3. **la consolidación**, también en orden de declaración, pero cada tipo consolida primero a su
   ancestro. Dentro de una clase van primero los atributos heredados, después los métodos y por
   último la interfaz.

Por esto, el error 14 se reporta en la primera clase declarada **que está en el ciclo**. Si una
clase anterior extiende a una del ciclo sin ser parte de él, su recorrido termina sin error, y el
ciclo lo reporta su primer integrante cuando le toca.

## Por qué el compilador no se cae

Las acciones de la EDT corren también mientras el Modo Pánico recupera un error sintáctico, y
pueden dejar declaraciones a medio armar. Esas declaraciones no se chequean nunca, porque el
semántico no corre si hubo errores. `RobustezDelSemanticoTest` lo verifica: corre el compilador
completo sobre los ~39.600 fuentes del corpus léxico, del sintáctico y del externo, y exige que
ninguno lo haga caer y que todos terminen en un código de la cátedra.
