# Plan de acción — Etapa 2: Analizador Sintáctico

Índice: [`../AGENTS.md`](../../AGENTS.md).

**Este archivo es el estado vivo de la etapa.** Una sesión que arranca en cero lee esto y sabe
qué se hizo, qué falta y qué tiene que leer antes de tocar nada. **Se actualiza al cerrar cada
sesión de trabajo** — la sección [Estado](#estado) y el [Backlog](#backlog) no pueden quedar
describiendo algo que ya cambió.

- **Entrega**: 23/09/2026 a las 16:00, zip por Moodle.
- **Método fijado**: Analizador Descendente Recursivo Predictivo LL(1), **Esquema Simple** de
  Clase 5. El profesor dijo que siguiendo esa receta la solución es correcta, así que **no nos
  despegamos de ella**; cualquier desvío se documenta con la alternativa evaluada y su razón, y
  se consulta con el usuario antes de aplicarlo.

---

## Reglas que gobiernan toda la etapa

Estas cuatro no se negocian y aplican a cada decisión, no sólo al arranque.

### 1. Todo lo que no esté especificado se comporta como en Java

Regla explícita de la cátedra, repetida en `etapas/etapa-1/README.md` y en
`etapas/etapa-2/README.md`: *«todo lo que NO esté especificado para MiniJava tiene comportamiento
por defecto igual al de Java; es decir, todo lo que no esté debidamente especificado, es igual a
Java»*.

Vale para la sintaxis igual que valía para el léxico: si una construcción no está resuelta por las
*Reglas de Sintaxis*, por las *Pautas* ni por un logro, **el desempate es replicar lo que hace
Java** — nunca inventar una regla propia, nunca elegir «lo que parezca razonable». El procedimiento
completo y obligatorio está en [`investigacion.md`](protocolo-de-investigacion.md): chequear primero si MiniJava
ya lo resuelve, si no verificar la especificación real de Java (no de memoria), **informar al
usuario antes de implementar**. Si la cátedra publica después una regla explícita que diga otra
cosa, esa regla gana.

Casos de esta etapa donde ya se aplicó: la asociatividad a derecha del ternario, la precedencia
del ternario respecto de la asignación, el ligado del `else` al `if` más cercano, y que
`List<List<T>>` cierre con dos `>` separados.

### 2. La robustez del testing es lo primero

**Los tests importan más que el código.** No alcanza con que el compilador ande en los casos
felices: la implementación tiene que cumplir el requisito **en los casos más extremos**, y la
búsqueda activa de casos raros y de borde es parte del trabajo, no un extra.

En concreto, para cada construcción hay que buscar y cubrir:

- el caso vacío y el caso de un solo elemento (lista de cero argumentos, clase sin miembros,
  bloque vacío, `for(;;)`, `new int[]{}`);
- el anidamiento profundo (bloques dentro de bloques, genéricos dentro de genéricos, cadenas de
  encadenamiento largas, ternarios anidados, `if/else` colgantes);
- los límites del contrato de salida (el token `eof` con lexema `$`, la línea y el lexema exactos
  del error, el formato `[Error:lexema|línea]`);
- lo que la construcción **debe rechazar**, no sólo lo que debe aceptar: por cada logro, un caso
  que pase con el logro prendido y falle con el logro apagado;
- las combinaciones entre logros, no sólo cada logro aislado;
- entradas degeneradas: archivo vacío, archivo con sólo comentarios, un único token, secuencias
  larguísimas, el error en el primer token y el error en el último.

La estrategia completa y la taxonomía de tipos de test están en [`testing.md`](../compartido/estrategia-de-testing.md). La
Etapa 1 dejó 169 casos de corpus y fuzzing con jqwik; **la Etapa 2 tiene que llegar a un nivel de
cobertura equivalente o mejor**.

### 3. Cada logro es un interruptor independiente, de punta a punta

El compilador tiene que poder ejecutarse y testearse con **0, 1 o N logros en cualquier
combinación**, incluida la de cero logros, que debe reproducir exactamente la especificación base
de la cátedra. Eso vale para la gramática (ya verificado en las 1024 combinaciones), para el
léxico (los 4 tokens nuevos), para el módulo principal y para los tests, que se saltean solos
cuando su logro está apagado.

### 4. En los entregables no se deja constancia de los errores del desarrollo

El informe y la documentación describen **el estado final y por qué es así**. No se narran los
errores cometidos durante el desarrollo, ni las versiones intermedias que se descartaron por
equivocadas, ni las correcciones que hubo que hacer sobre el propio trabajo. Sí se documenta,
porque es contenido técnico legítimo:

- los defectos **de la gramática de la cátedra** y cómo se resolvieron;
- las **alternativas de diseño evaluadas y descartadas**, con su razón;
- las **decisiones** tomadas donde la especificación admitía más de una lectura.

La distinción es entre «acá había un problema en el material y así lo resolví» —que va— y «yo me
equivoqué y después lo arreglé» —que no va—.

---

## Estado

| Sesión | Foco | Estado |
|---|---|---|
| **S1** | Conceptos + gramática | **Terminada** |
| **S2** | Cimientos + arranque del parser | **Terminada** |
| **S3** | Parser completo | **Terminada** |
| **S4** | Robustez, testing y entrega | **Terminada** |
| **S5** | Precedencia de lambdas + devolución del profesor | **Terminada** |
| **S6** | Contraste contra un corpus externo | **Terminada** |

Días de 8-10 horas, asistidos. La S5 es colchón: para desvíos, para la corrección del léxico, o
para pulido.

### Lo que dejó la S1

Todo verificado; los comandos están en [Verificación](#verificación-permanente).
**Hoy no hay ni una línea de analizador sintáctico en Java**: la S1 fue enteramente gramática,
herramientas y documentación.

- **Gramática LL(1) final y completa**, con los 9 logros de Etapa 2 y el *carry-over* de floats.
  111 no terminales, 228 producciones. **No queda gramática por diseñar en las sesiones
  siguientes.**
- `compilador/gramatica/` con tres versiones y su
  [`README.md`](../../compilador/gramatica/README.md): `minijava-base.g` (transcripción del PDF,
  16 conflictos), `minijava-ll1.g` (núcleo corregido) y `minijava-ll1-logros.g` (la vigente).
- `compilador/herramientas/`: `primeros.py` (vaciables, Primeros, LL(1), combinatoria de logros,
  registro de ambigüedades, rangos de longitud derivable, simulador del Esquema Simple),
  `aceptar_fuente.py` (tokeniza un fuente real con el léxico de la Etapa 1 y lo corre contra la
  gramática), `casos-gramatica.sh` (84 casos), `gramatica_a_markdown.py` y `gramatica_a_latex.py`.
- **Documentación reorganizada por fase**: `documentacion/{lexico,sintactico,compartido,proyecto,minijava}/`,
  cada carpeta con su README. Los cuatro documentos de gramática y los cinco del sintáctico están
  en [`../sintactico/`](../sintactico/README.md).
- **Informe** en `etapas/etapa-2/entregables/informe/`, compila sin desbordes ni avisos de fuente.
  Tipografía y color de la cátedra: Computer Modern a 12pt, y el azul `#4C4CB2` sólo en los
  terminales.
- **84 casos** de aceptación y rechazo propios, más los **8 de la cátedra**, que además fallan en
  el token exacto que declara la expectativa de cada uno.

### Lo que dejó la S2

Primer código de la Etapa 2. `mvn verify` verde, 889 tests.

- **Código y corpus reorganizados por fase**: `minijava/{comun,lexico,sintactico,aplicacion}/` y
  `resources/{lexico,sintactico}/`. El árbol de tests espeja el de producción.
- **Los logros son un `EnumSet<Logro>`** con los 14 conmutables de las dos etapas, configurable por
  `--logros=` y por `-Dlogros=`. El nombre de cada logro es el flag con el que la gramática marca
  sus producciones, y hay un test que lo verifica.
- **Un solo `Main` con selector de etapa**: sin opciones corre el sintáctico; `--etapa=1` vuelve al
  volcado de tokens con los logros de la Etapa 1.
- **Los cuatro tokens de la Etapa 2** (`for`, `private`, `?`, `->`), cada uno detrás de su logro.
  Apagado el logro, el léxico se comporta exactamente como la Etapa 1.
- **La suite corre por fase y por configuración de logros**: `@RequiereLogro` /
  `@RequiereLogroApagado` saltean el test que no aplica, y el corpus léxico declara sus requisitos
  en `resources/lexico/requisitos-de-logros.txt` en vez de asumirlos. Verificado en 11
  configuraciones, `-Pfase-lexico` (643 tests) y `-Pfase-sintactico` (247).
- **El núcleo del analizador sintáctico**: `Inicial`, `ListaClases`, `Clase`, `Interfaz`,
  `GenericidadOpcional`, `HerenciaOpcional`, `ExtensionOpcional`, `TipoReferencia`,
  `TipoGenericoOpcional` y `ArgumentoDeTipo`, con los Primeros como constantes con nombre en
  `sintactico/primeros/`. El logro de genericidad ya funciona de punta a punta.
- **El contrato de salida de la Etapa 2** completo: `Compilacion Exitosa` + `[SinErrores]`, y
  `Error Sintactico en linea N: ...` + `[Error:Y|N]`, con `$` de lexema para el `eof`.

### Lo que dejó la S3

El analizador sintáctico **completo**. `mvn verify` verde, 1117 tests en ese momento.

- **Los no terminales implementados**, divididos en una clase por sección de la gramática
  (`sintactico/partes/`, de 99 a 172 líneas cada una) más `EntradaDeTokens`, `Sintaxis` y
  `RestriccionesDeLambda`. Una sola clase habría llegado a 1176 líneas y 89 métodos.
- **Los 8 casos de la cátedra pasan con el compilador real**, cada error en el token exacto.
- **Los 10 logros funcionan como interruptores** de punta a punta, verificado en 16 configuraciones.
- **Las restricciones de MiniLambdas** que la gramática no puede expresar: `this` explícito,
  `this` implícito (toda llamada sin punto dentro de una lambda) y la captura de variables, más que
  el parámetro sea un identificador pelado y no se repita.
- **Los Primeros se generan** desde la gramática con `herramientas/primeros_a_java.py`: 220
  constantes, una por producción.
- **Verificación diferencial** con `herramientas/comparar_con_gramatica.py`: el analizador en Java y
  el simulador de la gramática dan el mismo veredicto y el mismo token de rechazo sobre 2250
  fuentes, con las divergencias deliberadas declaradas.

### Lo que dejó la S4

El compilador **completo**, con los diez logros implementados. `mvn verify` verde.

- **Modo Pánico**, el único logro que faltaba. Tres puntos de recuperación —las tres listas de la
  gramática que repiten un elemento—, sincronización con los tokens de Pautas slide 40 más los
  Primeros del elemento, llaves contadas para saltear el cuerpo de un método con la firma rota, y
  la invariante de que cada vuelta consume al menos un token.
- **`match(...)` sin prefijo**: `ParteDelAnalizador` delega los cinco métodos de entrada, así cada
  método de no terminal se lee como el ejemplo de Clase 5 slide 57. `EntradaDeTokens` sigue siendo
  el único punto que consume entrada.
- **El corpus del sintáctico**: 1879 casos. ~120 escritos a mano con la expectativa predicha, y el
  resto generado con Grammarinator desde la propia gramática y etiquetado por un oráculo JavaCC.
- **Tres reconocedores independientes coinciden** sobre el corpus entero: el analizador en Java, el
  simulador en Python y el parser generado por JavaCC.
- **Equivalencia de lenguajes verificada** con un reconocedor de Earley: la gramática final con
  cero logros y la de la cátedra generan el mismo lenguaje, con cobertura por producción y dos
  controles negativos que la validan.
- **El analizador cita exactamente la gramática**: 234 de 234 producciones, verificado por
  `auditar_analizador.py`.
- **Modularización**: `Primeros.java` (828 líneas) pasó a diez clases por sección,
  `AnalizadorLexicoTest.java` (1026 líneas) a ocho suites por tema, y `ArgsActuales` salió de
  `AnalizadorDeCreacion` a su propia parte.
- **Cobertura** 98,9 % de líneas y 94,5 % de ramas, con umbrales en `mvn verify`. **Mutación** al
  90 %, con umbral, sobre todo el compilador.

### Lo que dejó la S5

Corrección de la **precedencia de las lambdas** y los cuatro puntos que trajo el profesor.

- **La lambda salió de `<Primario>` y pasa a producirse en el nivel de `<Expresion>`**, como el
  JLS. Antes era un operando válido y el lenguaje reconocido era estrictamente más grande que el de
  Java: `1 + x -> x`, `a == x -> x`, `- x -> x`, `(x -> x) + 1` y `(x -> x).m()` pasaban el
  análisis y `javac` los rechaza. Cada caso se contrastó con `javac` antes y después.
- **Cuatro de las siete ambigüedades declaradas desaparecieron de raíz**: eran un síntoma de la
  ubicación, no un rasgo de las lambdas. Queda la del `=` (`x -> a = b` es `x -> (a = b)`).
- **Un cuarto chequeo extra-gramatical**: una lambda parentizada no lleva cola. `f((x -> x))` es
  válido y `(x -> x).m()` no; distinguirlos en la gramática exigiría clonar la cadena de colas.
- **`()[]` verificado**: `f()[0]`, `f()[0][1]`, `f().g()[0]`, `Clase.met()[0]`, `new Cosa()[0]`,
  `(a)[0]`, `this.m()[0]` — viene de `Referencia -> Referencia AccesoArreglo` de la cátedra.
- **Informe**: los errores informan siempre las dos mitades —qué se encontró y qué se esperaba—,
  con la explicación de por qué ése es el par de datos con el que decide el Esquema Simple; el
  `this` en lambda queda anotado como pendiente de la Etapa 3.
- **El `else` explícito en las producciones ε**, en los 18 métodos que lo dejaban implícito.
- **`Logro.FLOAT`, un solo nombre**: se eliminó el alias `floats`/`float` de `--logros`.
- **La salida real del compilador al correr los tests unitarios**: `BaseDeAnalisis` corre el módulo
  principal sobre el mismo fuente y vuelca lo que imprimiría `java -jar Compilador.jar`, bajo el
  mismo `-Dvolcado` que el corpus. Sirve para mirar un caso desde IntelliJ sin romper un test.
- 111 no terminales, 228 producciones. `mvn verify` verde, 5785 tests, cobertura sobre el umbral.

### Lo que dejó la S6

Contraste contra **un corpus externo** de 30.078 fuentes —78 escritas a mano y 30.000 de fuzzing
sobre otra gramática de MiniJava, en tres variantes: base, variables locales clásicas y lambdas—.
El material se usó una vez, como oráculo independiente, y no quedó en el repositorio.

- **Cero falsos aceptados en los 30.078**: no hay un solo fuente que su corpus declare inválido y
  este compilador acepte. Los 78 casos escritos a mano pasan todos.
- Los ~3.300 rechazos nuestros se explican enteros, sin residuo: la divergencia documentada del
  string terminado en `\` (§7 del léxico), las restricciones del logro MiniLambdas que esa gramática
  no tiene (`this`, captura, parámetro que no es identificador) y la ubicación de la lambda fuera de
  `<Primario>`. Los 94 que no entraban en ninguna categoría se adjudicaron con `javac`: 74 los
  rechaza por sintaxis también, y los 20 restantes se redujeron a pares mínimos donde coincidimos.
- **Un falso positivo propio, encontrado y corregido**: `x -> (y) -> y` se rechazaba por captura de
  `y`. El contenido de un paréntesis se lee como expresión —no hay forma de saber que es un
  parámetro hasta ver el `->` que sigue al `)`—, y ese parseo pasaba por el chequeo de captura con
  el alcance de la lambda de afuera. Ahora los nombres quedan **en duda** hasta que se cierra el
  paréntesis. Cambió el error reportado en 838 de las fuentes de fuzzing.
- **18 casos nuevos en el corpus** —1879 en total—, los que no eran redundantes: dos argumentos de tipo en el
  `extends`, tipo primitivo como argumento de tipo, coma en el `extends` de una interfaz, `idClase`
  suelto como sentencia, los cuatro errores de la lista de declaradores, `var` sin nombre,
  parámetros de lambda sin coma, lambda de varios parámetros sin cuerpo, los cuatro de la
  precedencia de la lambda, y del lado válido la declaración local como cuerpo de un `if` sin
  llaves y los declaradores genéricos con inicializador.
- **El corpus de fuzzing NO se importó.** Son 30.000 fuentes etiquetadas contra otra gramática y sin
  los logros de esta etapa; sumarlas duplicaría una fuente de verdad que este proyecto ya cubre con
  su propio corpus generado desde `minijava-ll1-logros.g` y contrastado con tres reconocedores, y
  multiplicaría por dieciséis lo que tarda la corrida de los testers de la cátedra sobre la entrega.
- **`matchDelActual()` se eliminó.** Consumía el token actual sin nombrar qué terminal esperaba, y
  Clase 5 slide 60 hace lo contrario: para un no terminal cuyas producciones son un solo terminal
  —`OperadorBinario`, `OperadorUnario`, `Primitivo`— escribe una rama por producción, cada una con
  su `match` nombrando el terminal. Ahora se lee así, con las constantes por alternativa que el
  generador ya emitía y nadie usaba. Agrega 42 ramas y sólo 3 sin cubrir —el `else` de error de
  `OperadorBinario` y el de `OperadorUnario` son inalcanzables porque el llamador ya discriminó con
  el mismo conjunto de Primeros—, así que la cobertura de ramas pasa de 94,47 % a 94,39 %, con el
  umbral en 93 %. Se dejan igual, porque son las que pide el esquema y las que detectarían una
  divergencia entre el conjunto unión y los conjuntos por alternativa.
- `mvn verify` verde, 5857 tests, cobertura sobre el umbral.

### Estado de la Etapa 1

**Corregida: todos los logros alcanzados, sin errores ni observaciones.** Tiene que seguir siendo
ejecutable y testeable de forma aislada mientras la Etapa 2 avanza: `--etapa=1` en el `Main` y
`mvn test -Pfase-lexico`.

### Resultado de la revisión externa de la gramática

Se hizo una auditoría independiente del trabajo de la S1 y sus hallazgos se reprodujeron con el
verificador antes de actuar. Resultado:

- La transcripción de la gramática base es fiel al PDF, las transformaciones canónicas están bien
  aplicadas y en el orden correcto, y con los logros apagados el lenguaje coincide con el de la
  cátedra en cada punto verificado.
- **Se encontraron tres ambigüedades**, de las cuales dos no estaban documentadas: `new int[3][4]`
  —heredada de la gramática de la cátedra— y la del cuerpo de la lambda, que sí introduce el
  proyecto y contagia a cinco no terminales. Las tres quedaron documentadas y resueltas voraz.
- **Se agregó al verificador un registro de ambigüedades declaradas** (`--ambiguedades`): recorre
  las 1024 configuraciones y falla si aparece una que no esté en la lista con su regla de
  resolución. Son tres y todas están declaradas.
- **Se extendió el inicializador de arreglos a multidimensional**, porque el logro dice «como en
  Java» y sólo restringe dónde, no cuántas dimensiones.
- El orden de los modificadores se mantiene fiel a la gramática original: `static public` se
  rechaza.

---

## Estado en una línea, para arrancar rápido

El compilador **está completo**. Lo que existe y anda hoy:

```bash
cd compilador
mvn verify                    # 5856 tests, cobertura sobre el umbral
java -jar target/Compilador.jar programa1.java             # analiza sintácticamente
java -jar target/Compilador.jar --etapa=1 programa1.java   # vuelca tokens (Etapa 1)
java -jar target/Compilador.jar --logros=ninguno prog.java # sin ningún logro
```

El compilador **está completo**: los diez logros implementados, el corpus poblado y el informe
cerrado. Lo que queda es pulido y la preparación del zip.

## Protocolo de arranque de sesión

Antes de escribir una línea de código, en este orden:

1. **Leer este archivo entero**, y en particular [Estado](#estado) y [Backlog](#backlog).
2. **Leer [`workflow.md`](flujo-de-trabajo.md)** — aplica sin excepción a cualquier tarea.
3. **Leer la sección de la sesión que corresponde**, más abajo, y el material que ahí se indica.
4. **Releer los PDFs de la etapa como imágenes** si la sesión toca un tema que no se leyó todavía.
   Regla de `AGENTS.md` §0: **como imágenes, página por página, nunca con extracción de texto**.
   Los PDFs de esta etapa tienen el código de ejemplo de la implementación en capturas con globos
   de explicación; `pdftotext` los pierde. Está permitido usar `pdftotext` **sólo** para ubicar en
   qué página está un tema, nunca para leerlo.
5. **Explicarle al usuario, antes de ejecutar**, lo que se lista en la sesión bajo *«Explicar
   antes de ejecutar»*. No es un trámite: el objetivo declarado del usuario es entender el
   problema, no sólo tener el código.
6. **Consultar las decisiones abiertas** que la sesión marque como tales.

### Qué se le explica al usuario antes de ejecutar, siempre

Independientemente de la sesión:

- **Cualquier desvío del Esquema Simple de Clase 5**, con la alternativa evaluada y la razón.
- **Cualquier caso que caiga bajo la regla «como en Java»**: qué dice Java, dónde se verificó, y
  qué se va a implementar.
- **Cualquier cambio que toque el analizador léxico de la Etapa 1**, porque esa etapa se corrige
  aparte y tiene que seguir funcionando y siendo testeable sola.
- **Cualquier decisión que agrande o achique el lenguaje reconocido** respecto de la gramática de
  la cátedra.

---

## Material bibliográfico: qué consultar y para qué

| Si la tarea es… | Leer |
|---|---|
| Entender Primeros, LL(1), la estrategia descendente | `etapas/etapa-2/Clase 3 - Sint1 - Intro Sintactico y Primeros.pdf` |
| Factorizar, eliminar recursión, tratar ambigüedad | `etapas/etapa-2/Clase 4 - Sint2 - Ambiguedad, Factorizacion y Recursion a Izquierda.pdf` |
| **Implementar el parser** (el esquema, `match`, los métodos por NT) | `etapas/etapa-2/Clase 5 - Sint3 - Parser Implementacion Recursiva.pdf`, slides 33-62 |
| Contrato de salida, logros, formato de error, invocación | `etapas/etapa-2/Pautas Etapa 2.pdf` (errores en 25-26, invocación 28, logros 34-44) |
| Consultar una regla de la gramática original | `documentacion/minijava/Reglas de Sintaxis MiniJava 2026.pdf` |
| Consultar un token o una regla léxica | `documentacion/minijava/Aspectos Lexicos MiniJava 2026.pdf` |
| Entender qué hace cada regla y qué estaba roto | [`../sintactico/defectos-de-la-gramatica-original.md`](../sintactico/defectos-de-la-gramatica-original.md) |
| Ver cómo se corrigió una regla puntual | [`../sintactico/transformacion-a-ll1-paso-a-paso.md`](../sintactico/transformacion-a-ll1-paso-a-paso.md) |
| Consultar la gramática vigente | [`../sintactico/gramatica-ll1-final.md`](../sintactico/gramatica-ll1-final.md) o el `.g` |
| Escribir o revisar tests | [`testing.md`](../compartido/estrategia-de-testing.md) |
| Una duda de comportamiento no resuelta en ningún lado | [`investigacion.md`](protocolo-de-investigacion.md), **antes** de escribir código |
| Compilar, correr, empaquetar, entregar | [`comandos-y-entrega.md`](comandos-compilacion-y-entrega.md) |

---

## Las sesiones

### S2 — Cimientos y arranque del parser

**Objetivo.** Que el repositorio soporte varias etapas conviviendo, que los logros se puedan
prender y apagar de a uno de punta a punta, y que exista el esqueleto del parser.

**Leer antes.** Clase 5 completa como imágenes. `arquitectura.md`,
`convenciones-catedra.md`. Los cuatro archivos de `compilador/src/main/java/minijava/config/`,
`tokens/` y `aplicacion/`.

**Explicar antes de ejecutar.**
- El flujo completo: `Main` → módulo principal → `AnalizadorSintactico` → `match` ↔ métodos por
  no terminal, **con un fuente MiniJava chico corrido paso a paso**. Es un pedido explícito del
  usuario y quedó pendiente de la S1.
- Cómo va a quedar el árbol de paquetes y por qué.

**Trabajo.**
1. Reorganizar `compilador/src/main/java/minijava/` por fase: `comun/` (io, salida, config,
   errores), `lexico/`, `sintactico/`, `aplicacion/`. El árbol de tests espeja eso.
2. `Main` con selector de etapa: corre la Etapa 2 por defecto y acepta `--etapa=1` para volver al
   volcado de tokens. Un solo jar; tiene que seguir cumpliendo
   `java -jar Compilador.jar programa1.java`.
3. Generalizar `ConfiguracionDelCompilador` de cuatro booleanos a un `EnumSet<Logro>` que cubra
   los logros de Etapa 1 y de Etapa 2, con `ninguno()`, `todos()`, `soloEtapa(n)`, `con(...)`,
   `sin(...)`; configurable por CLI y por propiedad de sistema.
4. Anotaciones `@RequiereLogro` / `@RequiereLogroApagado` como `ExecutionCondition` de JUnit 5:
   **saltean** el test si el logro no corresponde, no lo fallan. Replicarlo hacia atrás sobre los
   logros del léxico.
5. Perfiles Maven `fase-lexico` y `fase-sintactico` más `@Tag`, para correr una fase sola.
6. Los cuatro tokens léxicos nuevos, **cada uno detrás del flag de su logro**: `for`, `private`,
   `?`, `->`. Con el logro apagado, `?` vuelve a ser error léxico y `for`/`private` vuelven a ser
   `idMetVar`. Impacto medido: `tokens/TipoDeToken.java` (+4), `tokens/TablaDeLexemas.java` (tabla
   condicionada por configuración), `lexico/estados/EstadoInicial.java` (el `?`) y
   `lexico/estados/operadores/` (el `-` tiene que mirar si sigue un `>`). **No toca** el patrón
   State, ni el driver, ni el manejo de errores, ni el `SourceManager`.
7. `AnalizadorSintactico` con `match`, `ExcepcionSintactica` y los no terminales del núcleo.

**Terminada cuando.** `mvn verify` verde; `mvn test -Dlogros=ninguno` y `-Dlogros=todos` pasan;
`mvn test -Pfase-lexico` corre sólo el léxico; el corpus de Etapa 1 pasa **sin modificar un solo
caso**; `--etapa=1` sigue volcando tokens.

### S3 — El parser completo — **terminada**

**Objetivo.** Todos los no terminales de la gramática final implementados.

**Leer antes.** Clase 5 slides 33-62 otra vez, `documentacion/gramatica-minijava.md`, y
`compilador/src/main/java/minijava/sintactico/`, que ya tiene el núcleo escrito.

**Trabajo.** Continuar desde `ListaMiembros` y `ListaMetodosInterfaz`, un método privado por no
terminal, en el orden de la gramática. Los conjuntos de
Primeros van como constantes con nombre en un paquete `primeros/`, no inline, cada una con un
comentario que cite la producción, para que el código se lea contra el informe. Mensaje de error
según Pautas slide 25: `Error Sintactico en linea N: se esperaba X se encontro "Y"` más
`[Error:Y|N]`; el lexema de EOF es `$`.

**Terminada cuando.** Los 8 casos de la cátedra pasan con el compilador real, no sólo con el
simulador de la gramática. ✅

### S4 — Robustez, testing y entrega — **terminada**

**Objetivo.** Modo pánico, el corpus de tests, la documentación y el informe final.

**Hecho.** Todo lo listado arriba en [Lo que dejó la S4](#lo-que-dejó-la-s4). Los puntos que
cambiaron respecto de lo planeado, y por qué:

- **La suite del compañero no se portó, se adaptó su andamiaje.** Lo que trajo era un pipeline de
  fuzzing armado sobre *otra* gramática de MiniJava, escrita a mano en Python y sin logros. Portar
  esa gramática habría creado una segunda fuente de verdad; en su lugar, las dos gramáticas que las
  herramientas externas entienden —ANTLR para generar, JavaCC para el oráculo— se **generan** desde
  `minijava-ll1-logros.g`, igual que los documentos y los Primeros.
- **Los casos que dependen de un logro APAGADO no van en el corpus.** La cátedra corre sus testers
  sobre la carpeta de entrega con el compilador tal cual, o sea con todos los logros; un caso que
  necesite uno apagado haría fallar esa corrida. Esos pares acepta-con / rechaza-sin los cubren
  `RamasSinLogroTest`, los tests con fuente embebido y `casos-gramatica.sh`.

### S5 — Reserva

Colchón. Si no se usa, se dedica a subir cobertura y a buscar más casos raros.

---

## Backlog

Abierto. Lo que dice «consultar» requiere hablarlo con el usuario antes de resolverlo.

- [ ] Los logros de la Etapa 1 que sigan siendo alcanzables se mantienen; los de Etapa 2 no
      implementables por código son *Entrega Anticipada*, *Imbatibilidad* y *Well Done!*.
- [ ] **Antes de entregar.** Armar el zip y medir cuánto tarda la corrida de los testers de la
      cátedra sobre las 1879 fuentes del corpus; si se va de escala, decidir con el usuario qué
      recortar.

### Cerrado en la S4

- [x] La suite de tests del compañero: su andamiaje quedó adaptado a esta gramática y
      `vendor/tests-giuli/` se borró.
- [x] La comparación diferencial corre sobre el corpus fijo y en paralelo.
- [x] La equivalencia de LENGUAJES entre `minijava-base.g` y `minijava-ll1-logros.g` con cero
      logros, con un reconocedor de Earley y cobertura por producción. Responde de frente a la
      exigencia de Clase 4 de no cambiar el lenguaje al transformar la gramática.
- [x] El Modo Pánico.
- [x] `entrada().match(...)` pasa a `match(...)`.
- [x] Los archivos densos: `Primeros.java` y `AnalizadorLexicoTest.java`.

### Cerrado en la S3

- [x] Los chequeos del analizador que la gramática no puede expresar: el indicador de lambda, el
      conjunto de parámetros, y que el contenido entre paréntesis de una lambda de un parámetro sea
      un `idMetVar` pelado.
- [x] `ListaMiembros` y `ListaMetodosInterfaz`, que en la S2 cubrían sólo su producción vacía.

### Cerrado en la S2

- [x] La explicación del flujo `Main` → módulo → `AnalizadorSintactico` → `match` ↔ métodos por no
      terminal, corrida paso a paso sobre un fuente MiniJava.
- [x] La corrección del léxico de la Etapa 1: llegó, y salió perfecta.
- [x] Que el corpus léxico corra en función de los logros prendidos en vez de asumir una
      configuración fija.

### Cerrado en la S1

- [x] `this` dentro de una lambda → **se delega al analizador semántico**, junto con la
      prohibición de capturar variables. Las dos son restricciones sobre qué nombres pueden usarse
      en un alcance; detectarlas exigiría que el parser llevara un indicador de contexto, que es un
      chequeo de contexto y no de forma.
- [x] Visibilidad en métodos de interfaz → **`public` sí, `private` no**, aplicado y verificado.
      Resuelto por la regla «como en Java», contrastada con `javac`: un método de interfaz
      `private` exige cuerpo, y en MiniJava los métodos de interfaz son siempre abstractos, así
      que un `private` ahí sería siempre la forma que Java rechaza.
- [x] La referencia obsoleta de `AGENTS.md` a «Aspectos Lexicos MiniJava 2026.pdf» bajo
      `etapas/etapa-1/`.
- [x] `documentacion/README.md` describía el proyecto como si fuera sólo la Etapa 1 y no indexaba
      los documentos de gramática.
- [x] La revisión de la gramática por parte del usuario, con auditoría externa incluida. Resultado
      arriba.
- [x] `this` y captura de variables en lambdas: **los rechaza el analizador**, con un indicador de
      contexto y un conjunto con los parámetros. No es expresable en la gramática, pero sí
      verificable con información que el analizador acaba de leer.
- [x] Inicializadores de arreglo multidimensionales, como en Java.
- [x] Orden de `static` y la visibilidad: fiel a la gramática original.

## Decisiones ya tomadas que no hay que volver a discutir

- Gramática LL(1) cerrada, con nombres de no terminal semánticos en español y notación BNF del PDF
  de la cátedra. **No se rediseña**; si aparece un defecto se corrige puntualmente y se regeneran
  los documentos.
- Ante factorizar fusionando o insertando un no terminal, **se inserta**: el NT insertado es el
  punto de decisión que el semántico de la Etapa 3 necesita. Un NT de decisión por contexto,
  aunque dos contextos tengan la misma forma.
- Un solo `Main` con selector de etapa, no dos jars.
- Los cuatro tokens léxicos nuevos van condicionados al flag de su logro.
- La ambigüedad `<Asignacion>`/`<Llamada>` se colapsa en una sentencia-expresión; distinguirlas es
  trabajo del semántico. El *dangling else* se resuelve por regla de implementación: la rama
  `else` va primero, y liga al `if` más cercano.
- Los métodos de interfaz admiten `public` y no `private`, y **son siempre abstractos**: ningún
  logro de esta etapa les agrega cuerpo.
- El `this` dentro de una lambda —explícito e implícito— y la captura de variables **los rechaza el
  analizador sintáctico**, con un indicador de contexto y un conjunto con los parámetros de la
  lambda. No son expresables en la gramática, pero sí verificables con información local.
  **Se evaluó diferirlos al semántico** —la cátedra lo admite— y se decidió **dejarlos como están**.
  El motivo técnico, además de que ya estaban hechos: el control del `this` implícito **no es
  separable** del de la captura, porque `x -> campo` y `x -> n` son la misma forma sintáctica;
  quitar uno quita el otro, y quitar sólo el `this` explícito no simplifica nada —todo el estado de
  `RestriccionesDeLambda` lo sostiene la cláusula de la captura—. No se vuelve a discutir.
- Las tres ambigüedades de la gramática se resuelven **voraz**, por regla de implementación, y
  están declaradas en el verificador. Ninguna se resuelve reescribiendo la gramática.
- **La lambda se produce en el nivel de `<Expresion>`, nunca en `<Primario>`**, como el JLS. No es
  un operando: `1 + x -> x`, `- x -> x`, `(x -> x) + 1` y `(x -> x).m()` se rechazan, igual que en
  Java. No se vuelve a discutir.
- **Ningún método de no terminal omite el `else` final de su cadena de Primeros.** Si el no terminal
  no es anulable, ese `else` es el error (Clase 5, slide 39); si alguna parte derecha deriva a ε, es
  un `else` vacío con la producción citada al lado —el «no hago nada :)» de la slide 52—, que es
  donde la Etapa 3 va a colgar la traducción de la producción vacía. Pedido explícito del profesor.
- **Cada logro tiene UN solo nombre, sin alias**: el mismo con el que la gramática marca sus
  producciones. El de floats es `float`, no `floats`.
- El inicializador de arreglos es anidable; el orden `static public` se rechaza.

---

## Verificación permanente

Todo esto tiene que pasar al cerrar cualquier sesión:

```bash
cd compilador
python3 herramientas/primeros.py gramatica/minijava-ll1-logros.g                # ES LL(1)
python3 herramientas/primeros.py gramatica/minijava-ll1-logros.g --combinatoria # 1024/1024
python3 herramientas/primeros.py gramatica/minijava-ll1-logros.g --ambiguedades # 3/3 declaradas
python3 herramientas/auditar_analizador.py                                      # 234/234 citadas
python3 herramientas/equivalencia_de_lenguajes.py                               # mismo lenguaje
bash herramientas/casos-gramatica.sh                                            # 87/87
python3 herramientas/aceptar_fuente.py ../vendor/testersSintactico/resources/*/*.java  # 8/8
python3 herramientas/comparar_con_gramatica.py                                  # 1879 fuentes
mvn verify                                                                      # suite + cobertura
mvn -Pmutacion verify                                                           # mutación 90 %
mvn test -Dlogros=ninguno                                                       # cero logros
mvn test -Dlogros=todos                                                         # todos
mvn test -Pfase-lexico                                                          # sólo el léxico
mvn test -Pfase-sintactico                                                      # sólo el sintáctico
```

El corpus generado se regenera desde su semilla versionada, y hace falta el andamiaje del fuzzer:

```bash
make venv && make fuzz          # Grammarinator + los jars de ANTLR y JavaCC
python3 herramientas/lote_sintactico.py   # los casos escritos a mano
```

Y, cuando el informe cambie:

```bash
cd etapas/etapa-2/entregables/informe
latexmk -pdf informe.tex && cp informe.pdf ../Informe-Etapa2-Sintactico.pdf
```

Los documentos `documentacion/gramatica-minijava.md` y los `.tex` de
`entregables/informe/generado/` **se generan** desde el `.g`; si se toca la gramática hay que
regenerarlos, nunca editarlos a mano:

```bash
cd compilador
python3 herramientas/gramatica_a_markdown.py gramatica/minijava-ll1-logros.g --sin-cabecera
python3 herramientas/gramatica_a_latex.py gramatica/minijava-ll1-logros.g producciones
python3 herramientas/gramatica_a_latex.py gramatica/minijava-ll1-logros.g primeros
```
