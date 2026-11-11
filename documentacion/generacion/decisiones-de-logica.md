# Decisiones de lógica

Qué traduce la etapa y qué valor tiene cada cosa en ejecución. Las fuentes son el Apunte CeIVM y el
documento completo de semántica; lo que no dicen se resuelve como en Java, verificado con `javac`
25. Lo que contestó el profesor está en
[`../../CONVENCIONES-CATEDRA.md`](../../CONVENCIONES-CATEDRA.md).

## 1. El lenguaje que se traduce

El MiniJava base que chequea la Etapa 4. Los logros son independientes entre etapas
([`../../CONVENCIONES-CATEDRA.md`](../../CONVENCIONES-CATEDRA.md) §8), así que las construcciones de
logros que el parser acepta (`for`, el ternario, los posfijos, las locales tipadas, los atributos
inicializados) siguen siendo un error semántico y nunca llegan a generarse.

`float` es la excepción: la Etapa 4 lo acepta como un tipo sin operaciones. Un literal `float` se
traduce como sus bits, sólo para que el compilador no se caiga; operar con floats es el logro Floats
y Coerción E5, que no se persigue.

## 2. Las consultas pendientes, con su propuesta

Las cinco preguntas de [`../../etapas/etapa-5/consultas/dudas.pdf`](../../etapas/etapa-5/consultas/dudas.pdf).
Cada propuesta está en un solo lugar del código y tiene sus casos, así que cambiarla es tocar eso.

| Consulta | Propuesta implementada | Java (verificado con `javac` 25) |
|---|---|---|
| 1. El error si falta `main` o hay más de uno | [`decisiones-de-deteccion-de-errores.md`](decisiones-de-deteccion-de-errores.md) §1 | compila sin `main`; falla al lanzar |
| 2. Cómo termina un error de ejecución propio | un mensaje y `HALT` (Apunte §17) | una excepción |
| 3. El valor de los escapes | los de Java; uno que Java no tiene vale el carácter mismo | `'\q'` no compila |
| 4. El orden de evaluación de una asignación | primero el lado derecho (Clase 16, slides 18-29; Apunte §7) | primero el lado izquierdo |
| 5. Si dos literales String iguales son el mismo objeto | no: cada evaluación crea un String (Apunte §13) | sí |

Los escapes de Java son `\b \t \n \f \r \s \" \' \\`, los octales de hasta tres dígitos (`\0` a
`\377`) y el Unicode `\uXXXX`, con la `u` repetible (JLS §3.3 y §3.10.7). Valen igual en un `char`
y en un String.

## 3. Lo que fija el material

| Pregunta | Respuesta | Fuente |
|---|---|---|
| ¿Qué imprime `debugPrint`? | el entero y un salto de línea | documento §7 |
| ¿Qué devuelve `System.read()`? | el código del próximo byte de la entrada, tal cual lo da `READ` | documento §7 |
| ¿Qué devuelve `toString()` de `Object`? | el String vacío; también para un String y un arreglo, que no lo redefinen | Apunte §12-§14 |
| ¿`==` entre Strings o arreglos? | compara referencias | documento §11.2 |
| ¿`&&` y `\|\|`? | con cortocircuito | documento §11.2 |
| ¿Qué valor tiene un atributo o una componente sin asignar? | 0, `false` o `null` | JLS §4.12.5 |
| ¿Qué devuelve un método con retorno que termina sin `return`? | 0, `false` o `null` | el documento §12.5 no lo establece; se elige un valor definido ([`decisiones-de-diseno.md`](decisiones-de-diseno.md) §6) |
| ¿Se llama al constructor del padre? | no | documento §6.3 y §11.7 |
| ¿Se evalúa el receptor de una llamada a un estático? | sí, y se descarta | documento §10.2 |
| ¿Cuándo se controla un tamaño negativo en `new T[a][b]`? | después de evaluar todos los tamaños y antes de alojar nada: `new int[0][-1]` falla | `javac` 25 |

## 4. Los defectos del material

El `.out` de ejemplo que acompaña a la CeIVM y a los testers (`[gen-01.java].out`) es «una guía»
(guía de los testers, slide 16), y en dos puntos contradice al documento. Rige el documento:

- su `read` le resta 48 al valor leído; el documento dice «el próximo byte», y la CeIVM devuelve 53
  para un `5`;
- su `debugPrint` no salta de línea; el documento dice que «imprime un entero y finaliza la línea».

El tester de la cátedra busca fragmentos, así que acepta las dos formas. Los dos puntos están
consultados al profesor.
