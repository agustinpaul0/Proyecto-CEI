# Etapa 5 — Plan de desarrollo

Documento de trabajo: fija qué se va a construir y en qué orden. No va en el zip de la entrega
(`.gitattributes`). Cuando algo de acá quede implementado, su descripción pasa a
`documentacion/generacion/` y se borra de este plan.

Fuentes, leídas como imágenes: *Pautas Etapa 5*, Clases 12 a 16, el *Apunte CeIVM 2026*
(`documentacion/minijava/`), la guía de los testers, el `.out` de ejemplo y el documento completo de
semántica (§1, §7, §8, §14). Las preguntas que dependen del profesor están en
[`consultas/dudas.tex`](consultas/dudas.tex).

## 1. Qué hay que entregar

| | |
|---|---|
| Fecha límite | 20/11/2026, 16:00 (Pautas, slide 28) |
| Entrega Anticipada | 48 h antes: **18/11/2026, 16:00** (slide 37). Es el objetivo. |
| Invocación | `java -jar Compilador.jar programa1.java salida.txt`, con cualquier extensión (slide 30) |
| Éxito | `Compilacion Exitosa`, línea vacía, `[SinErrores]`, y el archivo de salida con el código CeIASM (slides 24 y 29) |
| Errores | igual que en las etapas anteriores: el primero corta, `[Error:lexema\|linea]` (slide 26). No se escribe el archivo. |
| Ejecución | el `.out` tiene que correr en la CeIVM y hacer lo que dice el fuente (slide 25) |
| Informe | cómo compilar y correr, y los logros que se intentan (slide 34). Nada más. |
| Logros | sólo los que no piden código: **Entrega Anticipada Generación**, **Imbatibilidad Generación** y **Congrats!** |

El lenguaje es el MiniJava base del documento completo, el mismo que chequea la Etapa 4: clases,
interfaces, genéricos, herencia, métodos estáticos y de instancia, constructores, atributos,
locales `var`, `if`, `while`, `return`, bloques, asignaciones, llamadas, `new`, arreglos de
cualquier dimensión, `String`, `char`, `boolean`, `int`, `null` y los predefinidos `Object`,
`String` y `System`. Las construcciones de logros que el parser acepta (`for`, ternario, posfijos,
locales tipadas, `float`, atributos inicializados) siguen siendo un error semántico, como en la
Etapa 4: los logros son independientes entre etapas (`CONVENCIONES-CATEDRA.md` §8).

## 2. El diseño: el camino que recomienda la cátedra

**Pasada separada.** Primero el análisis semántico completo; recién si terminó sin errores, el
Módulo Principal le pide a la tabla `generarCodigo()` (Pautas, slide 22). Es la forma «más
recomendada» de la Clase 16, slide 4. Cada nodo del AST y cada entidad de la tabla gana un
`generar(...)` que emite sus instrucciones y delega en sus hijos en orden (slides 2 y 10-11).

**La función `Gen`** (Clase 16, slide 7) es un objeto `Generador` que acumula las líneas y numera
las etiquetas. No es estático: se crea por compilación y se pasa a cada `generar`, así la tabla de
símbolos sigue siendo el único estado estático (invariante de `convenciones-de-la-catedra.md`).
Al terminar, el Módulo Principal escribe el texto en el archivo de salida.

**Los offsets se calculan en el análisis semántico** (Clase 16, slides 5 y 36), y la resolución de
nombres deja anotada la entidad elegida en el nodo (slide 17):

| Qué | Dónde se calcula | Valor (Apunte §10, tabla de la p. 19) |
|---|---|---|
| Atributo | al consolidar la clase, después de su ancestro | 1, 2, … en el CIR: primero los heredados; el 0 es la VT |
| Método de instancia | al consolidar: la disposición de la VT | 0 es `toString`; siguen los del ancestro, después los nuevos |
| Método de interfaz | al consolidar la interfaz | `toString`, los de la interfaz extendida, los propios (Apunte §9.1) |
| Parámetro i (desde 1) de n | fórmula | estático `n+3-i`; de instancia y constructor `n+4-i` |
| `this` | fijo | 3 |
| Valor de retorno | fórmula | estático `n+3`; de instancia `n+4` |
| Local | en el chequeo, por el `Contexto` | 0, −1, −2, … según las locales vivas al declararla (Apunte p. 12) |
| `FMEM` de un `return` | en el chequeo | las locales vivas en ese punto (Apunte §8.1) |

Una redefinición reusa la entrada de la VT del método heredado; los estáticos no ocupan VT.

**Estructuras en ejecución**, todas del Apunte:

- CIR: `[VT, atributos…]`, alojado con `simple_malloc` (§11).
- VT en `.data`: `lblVT_<Clase>: DW <etiquetas>` (§16). Las interfaces no tienen VT.
- Arreglo: `[VT de Object, longitud, componentes…]`; la referencia apunta a la VT (§12).
- String: `[VT de String, longitud, caracteres…, 0]`, en el heap, creado por el código (§13).
- Etiquetas únicas con separadores que no existen en un identificador:
  `metodo@<Clase>@<nombre>@<aridad>`, `constructor@<Clase>@<aridad>`, `vt@<Clase>`,
  `<rutina>@runtime`, y `if@fin@<n>` y similares para el control de flujo.

**El código de arranque** (Apunte §15): `PUSH simple_heap_init`, `CALL`, `PUSH <main>`, `CALL`,
`HALT`. Después, las rutinas de soporte, las VT y el código de cada clase.

### Traducción de cada nodo

Con los esquemas del Apunte y de la Clase 16:

| Nodo | Código |
|---|---|
| Literal | `PUSH` del valor: `true` 1, `false` 0, `null` 0, un `char` su código. Un String crea el objeto en el heap. |
| Binario | izquierdo, derecho, la instrucción (`ADD`, `SUB`, `MUL`, `DIV`, `MOD`, `EQ`, `NE`, `LT`, …). `&&` y `\|\|` con cortocircuito: `DUP`, `BF`/`BT` al final, `POP`, derecho (documento §11.2). |
| Unario | `NEG`, `NOT`; el `+` no genera nada. |
| Variable | local o parámetro: `LOAD`/`STORE`; atributo: `LOAD 3` y `LOADREF`/`SWAP`+`STOREREF` (Clase 16, slide 29, la versión correcta). |
| Asignación | derecho, `DUP`, destino con `STORE`/`STOREREF`: deja el valor (Apunte p. 10). |
| Sentencia-expresión | la expresión y `POP` si deja un valor. |
| `var` | el inicializador queda en la celda de la local: no hace falta `RMEM` (Apunte p. 12). |
| Bloque | sus sentencias y `FMEM` de sus locales al cerrar. |
| `if`, `while` | `BF`/`JUMP` con etiquetas, como el Apunte §5. |
| `return` | `STORE` del valor en la celda de retorno, `FMEM` de las locales vivas, `STOREFP`, `RET`. |
| Llamada estática | `RMEM 1` si devuelve algo, argumentos, `PUSH etiqueta`, `CALL`. Con un receptor, el receptor se evalúa y se descarta (documento §10.2). |
| Llamada dinámica | receptor, `RMEM 1` y `SWAP` si devuelve algo, cada argumento con `SWAP`, `DUP`, `LOADREF 0`, `LOADREF offset`, `CALL` (Apunte p. 19). Sin receptor, el receptor es `this`. |
| `new C(...)` | `RMEM 1`, `PUSH tamaño`, malloc, `DUP`, `PUSH vt@C`, `STOREREF 0`, `DUP`, argumentos con `SWAP`, `PUSH constructor`, `CALL` (Apunte p. 21). |
| Unidad | `LOADFP`, `LOADSP`, `STOREFP`, cuerpo, `STOREFP`, `RET n` (`n+1` en las de instancia). El constructor copia `this` a su celda de resultado antes de volver. |
| Arreglos | creación, indexación y `length` por rutinas de soporte, que controlan referencia nula, índice fuera de rango y tamaño negativo (Apunte §12 y §17). |

### Las rutinas de soporte

Escritas a mano en CeIASM, una sola vez, y probadas aisladas:

- `simple_heap_init` y `simple_malloc`, las de `rutinasHeap.txt`. El malloc deja el bloque en cero,
  para que atributos y componentes arranquen en `0`/`false`/`null` como en Java (JLS §4.12.5): la
  CeIVM no garantiza memoria limpia si la pila pasó antes por esa zona.
- Crear un arreglo de una dimensión, y uno de varias con una rutina recursiva que recibe los tamaños.
  Todos los tamaños se controlan antes de alojar nada, como `javac` 25: `new int[0][-1]` falla.
- Calcular la dirección de una componente, controlando la referencia y el rango.
- Los errores de ejecución: imprimen un mensaje y terminan (el formato depende de la duda 2).
- Los predefinidos, como el Apunte §14: `debugPrint` (`IPRINT` y `PRNLN`, como dice el documento
  §7, aunque el `.out` de ejemplo no salta de línea), `toString` (devuelve `""`), los `print*` de
  `System` y `read` (`READ` sin restar 48: el documento §7 dice «el próximo byte», y la CeIVM
  devuelve 53 para un `5`; el `.out` de ejemplo resta 48).

### El `main`

El documento (§1, §8 y §14) deja para esta etapa el control del punto de entrada, y el Apunte §15
dice que todo programa declara un `static void main()`. Se busca entre los métodos que declara cada
clase, no los heredados, un estático, `void` y sin parámetros. Qué error dar si no hay ninguno o si
hay más de uno es la duda 1; hasta que conteste, se implementa la propuesta de la duda.

## 3. El módulo principal

- `--etapa=5` pasa a ser la etapa por defecto, con `ModuloPrincipalGeneracion` en el mapa de `Main`.
  Los testers de las etapas anteriores ya pasan su `--etapa`, así que no cambian.
- `OpcionesDeInvocacion` acepta un segundo argumento, el archivo de salida. En la Etapa 5 es
  obligatorio: sin él, el compilador imprime el uso y no compila.
- Si hay un error, no se crea el archivo.

## 4. Los tests: lo más robusta posible

El código se mantiene simple; la exigencia está en los tests. El oráculo nunca es nuestro propio
compilador: la salida esperada sale de Java o de la cátedra.

1. **El tester de la cátedra**, `TesterDeCasosSinErrores`, en `src/test/java/catedra/`, con sus 4
   casos en `resources/catedra/generacion/`. La CeIVM va en `compilador/`, donde la busca el tester.
   Los `.out` que genera se ignoran en git.
2. **El corpus a mano**, en `resources/sinErrores/`, con el formato de la cátedra (`///salida&…&exitosamente`):
   uno o más casos por fila de la tabla de traducción y por borde. Entre otros: despacho dinámico en
   tres niveles, redefinición, `toString` redefinido, llamadas por interfaz y por interfaz extendida,
   genéricos, estáticos llamados de las tres formas, recursión, constructores con y sin argumentos,
   atributos heredados, el valor por defecto de atributos y componentes, cortocircuito con efectos,
   locales en bloques hermanos y anidados, `return` desde bloques anidados y desde un `while`,
   arreglos de 1 a 3 dimensiones, de referencias y de largo 0, Strings con escapes, `System` entero,
   y `main` en una clase que no es la primera.
   **Cada caso se corre también con `javac` y `java` 25**, con un adaptador para `debugPrint` y
   `System`, y su salida tiene que coincidir con la esperada: la primera línea no la escribe nuestro
   compilador.
3. **Los errores de ejecución**, en `resources/errores-de-ejecucion/`: índice negativo y fuera de
   rango, tamaño negativo en cualquier dimensión, referencia nula al acceder a un atributo, llamar a
   un método, indexar o imprimir un String, y división por cero. La guía de los testers pide
   probarlos a mano; se automatizan con un tester propio que controla el mensaje.
4. **El contraste diferencial con Java.** Un generador determinista, con semillas fijas, arma
   programas MiniJava válidos y con salida: jerarquías con redefiniciones, interfaces, llamadas
   dinámicas y estáticas, arreglos, recursión, `while` e `if` anidados, expresiones con todos los
   operadores. Cada uno se compila con el nuestro y se corre en la CeIVM, y se compila con `javac` y
   se corre con `java`: las salidas tienen que ser iguales. Unos 3.000 programas, empaquetados en un
   `.tar.gz` como el corpus generado de la Etapa 4. La CeIVM se corre en proceso, con `CeIVMAPI`, para
   que la batería entre en el tiempo de `mvn test`.
5. **Propiedades** (jqwik, como en la Etapa 4): expresiones enteras y booleanas al azar evaluadas por
   la CeIVM y por Java; offsets y disposición de la VT contra invariantes (un heredado conserva su
   offset en todos los descendientes; una redefinición no agrega entradas).
6. **Unitarios** de cada nodo y de cada rutina de soporte, con el CeIASM esperado o con su efecto en
   la VM.
7. **Robustez**: los ~40.000 fuentes de las etapas anteriores que pasan el chequeo de sentencias y
   tienen `main` se compilan sin excepciones y el `.out` ensambla en la CeIVM.
8. **Regresión**: las suites de las Etapas 1 a 4 siguen verdes. El corpus de la Etapa 4 se muda a
   `resources/sentencias/` y la raíz pasa a ser de la Etapa 5 (`comandos-compilacion-y-entrega.md`).
9. **Cobertura y mutación**: el umbral de JaCoCo actual, y PIT sobre el paquete de generación con por
   lo menos el 90 % de la Etapa 2.

## 5. Orden de trabajo

Toda la etapa se hace el 11/11, en siete fases, con un commit por fase y los tests primero en cada
una. Queda margen hasta la Entrega Anticipada del 18/11 para aplicar las respuestas del profesor.

| Fase | Qué | Termina cuando |
|---|---|---|
| 1 | Mudar el corpus de la Etapa 4, la CeIVM en `compilador/`, el ejecutor en proceso, el tester de la cátedra y el perfil `fase-generacion` | las Etapas 1 a 4 siguen verdes |
| 2 | Opciones y `Main`, `Generador`, arranque, rutinas del heap, predefinidos, `main`, unidades estáticas | pasa `gen-01` |
| 3 | Offsets y VT al consolidar, atributos, locales, llamadas estáticas y dinámicas, `new`, `this` | pasan `gen-02` a `gen-04` |
| 4 | Sentencias, operadores con cortocircuito, escapes, Strings, interfaces y genéricos | el corpus a mano de esas construcciones da lo mismo en la CeIVM y en Java |
| 5 | Arreglos y errores de ejecución | los errores de ejecución dan su mensaje |
| 6 | Adaptador a Java, generador diferencial, propiedades, robustez, mutación | `mvn verify` verde con todas las baterías |
| 7 | `documentacion/generacion/`, documentos de estado, informe mínimo, `/cerrar` | deuda cero y zip medido |

## 6. Riesgos

- **Que el profesor conteste después de implementar.** Cada duda tiene una propuesta concreta que se implementa
  mientras tanto, con su test, para que cambiar de criterio sea tocar un caso.
- **El tiempo de la batería.** Un proceso de la CeIVM por caso tarda ~0,2 s; por eso los casos
  propios usan la API en proceso y sólo el tester de la cátedra lanza procesos.
- **El adaptador a Java.** `debugPrint` se llama sin receptor desde cualquier clase, porque es un
  estático heredado de `Object`. El adaptador agrega a cada clase sin padre explícito un ancestro
  común que lo declara, y una clase `System` propia.
