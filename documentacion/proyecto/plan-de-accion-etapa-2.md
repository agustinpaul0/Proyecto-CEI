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
| **S4** | Robustez, testing y entrega | Pendiente |
| **S5** | Reserva | Pendiente |

Días de 8-10 horas, asistidos. La S5 es colchón: para desvíos, para la corrección del léxico, o
para pulido.

### Lo que dejó la S1

Todo verificado; los comandos están en [Verificación](#verificación-permanente).
**Hoy no hay ni una línea de analizador sintáctico en Java**: la S1 fue enteramente gramática,
herramientas y documentación.

- **Gramática LL(1) final y completa**, con los 9 logros de Etapa 2 y el *carry-over* de floats.
  104 no terminales, 214 producciones. **No queda gramática por diseñar en las sesiones
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

El analizador sintáctico **completo**. `mvn verify` verde, 1117 tests.

- **Los 104 no terminales implementados**, divididos en una clase por sección de la gramática
  (`sintactico/partes/`, de 99 a 172 líneas cada una) más `EntradaDeTokens`, `Sintaxis` y
  `RestriccionesDeLambda`. Una sola clase habría llegado a 1176 líneas y 89 métodos.
- **Los 8 casos de la cátedra pasan con el compilador real**, cada error en el token exacto.
- **Los 10 logros funcionan como interruptores** de punta a punta, verificado en 16 configuraciones.
- **Las tres restricciones de MiniLambdas** que la gramática no puede expresar: `this` explícito,
  `this` implícito (toda llamada sin punto dentro de una lambda) y la captura de variables, más que
  el parámetro sea un identificador pelado y no se repita.
- **Los Primeros se generan** desde la gramática con `herramientas/primeros_a_java.py`: 220
  constantes, una por producción.
- **Verificación diferencial** con `herramientas/comparar_con_gramatica.py`: el analizador en Java y
  el simulador de la gramática dan el mismo veredicto y el mismo token de rechazo sobre 2250
  fuentes, con las divergencias deliberadas declaradas.

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
  resolución. Son siete y todas están declaradas.
- **Se extendió el inicializador de arreglos a multidimensional**, porque el logro dice «como en
  Java» y sólo restringe dónde, no cuántas dimensiones.
- El orden de los modificadores se mantiene fiel a la gramática original: `static public` se
  rechaza.

---

## Estado en una línea, para arrancar rápido

El compilador **está completo salvo el Modo Pánico**. Lo que existe y anda hoy:

```bash
cd compilador
mvn verify                    # 1117 tests, cobertura sobre el umbral
java -jar target/Compilador.jar programa1.java             # analiza sintácticamente
java -jar target/Compilador.jar --etapa=1 programa1.java   # vuelca tokens (Etapa 1)
java -jar target/Compilador.jar --logros=ninguno prog.java # sin ningún logro
```

Lo que **falta** para cerrar la etapa: el Modo Pánico, el corpus de `resources/sintactico/` —hoy
vacío—, y las cuatro secciones del informe que pide Pautas slide 33. Todo eso es la S4.

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

### S4 — Robustez, testing y entrega

**Objetivo.** Modo pánico, el corpus de tests, la documentación y el informe final. **Es lo único
que queda de la etapa**: el analizador está completo y verificado.

**Leer antes.** `Pautas Etapa 2.pdf` slide 40 (Modo Pánico) y la sección 5 de
[`../sintactico/decisiones-de-deteccion-de-errores.md`](../sintactico/decisiones-de-deteccion-de-errores.md).

**Trabajo.**
1. **Recuperación en Modo Pánico** (logro): al detectar un error, descartar tokens hasta un token
   de sincronización —`;`, `}` o `{` según el contexto, Pautas slide 40— y reanudar. Detrás de su
   flag `MODO_PANICO`, que ya existe en el enum: apagado, el compilador corta en el primer error
   como pide la convención base. **Es el único logro de la etapa sin implementar.**
2. **Corpus** en `compilador/resources/sintactico/{sinErrores,conErrores}/`, que **hoy están
   vacíos**, con el criterio de robustez de la regla 2 de este documento. El profesor autorizó
   **asumir programas léxicamente válidos** para los tests del sintáctico, así que el corpus no
   repite casos léxicos. Se reusa la mecánica que ya existe: el formato de expectativas
   (`//#lexema,linea` y `// [Error:lexema|linea]`), `casos/VolcadoDeCasos`,
   `RequisitosDeLogrosDelCaso` con su manifiesto, y los tres testers. **Las expectativas se predicen
   a mano antes de correr.**
3. **Integrar la suite de tests de un compañero** para ampliar el corpus. El usuario la trae al
   arrancar la sesión. Cuidado: los casos ajenos pueden asumir logros distintos de los nuestros, así
   que cada uno entra con sus requisitos declarados en el manifiesto, y **cualquier divergencia de
   veredicto se investiga antes de aceptarla como propia** —puede ser un defecto nuestro o una
   lectura distinta de la especificación, y las dos cosas hay que resolverlas, no taparlas—.
4. **Llevar `comparar_con_gramatica.py` al corpus fijo**, para que la equivalencia entre el
   analizador y la gramática quede en la verificación permanente y no dependa de fuentes generados
   al momento.
5. Actualizar `documentacion/proyecto/*.md` y `documentacion/*.md`.
6. **Cerrar el informe.** Hoy cubre entera la gramática —alcance, notación, la original comentada,
   diagnóstico, transformaciones, extensiones por logro, gramática final y verificación— pero
   **nada de lo que produjeron la S2 y la S3**. Pautas slide 33 pide explícitamente cuatro cosas que
   todavía no están:
   - **cómo compilar el código entregado y cómo correrlo** (`mvn package`, `java -jar`, `--etapa`,
     `--logros`);
   - los **tipos de errores sintácticos** que el analizador puede detectar —los dos puntos del
     Esquema Simple más los tres chequeos de lambda—;
   - la **documentación usual de un proyecto de software**: decisiones de diseño y clases
     utilizadas, o sea la división en `sintactico/partes/`, `EntradaDeTokens`, la fachada `Sintaxis`
     y por qué;
   - **qué logros se intentan alcanzar** en la etapa.

   Y conviene sumar la verificación diferencial contra la gramática, que es el argumento más fuerte
   de corrección que tiene el trabajo.

### S5 — Reserva

Colchón. Si no se usa, se dedica a subir cobertura y a buscar más casos raros.

---

## Backlog

Abierto. Lo que dice «consultar» requiere hablarlo con el usuario antes de resolverlo.

- [ ] **Traer al arrancar la S4.** La suite de tests de un compañero, para ampliar el corpus. Ver
      el punto 3 de la S4.
- [ ] **Hacer en la S4.** Llevar la comparación diferencial al corpus fijo: hoy
      `herramientas/comparar_con_gramatica.py` corre sobre fuentes generados al momento, y debería
      correr sobre `resources/sintactico/` como parte de la verificación permanente.
- [ ] **Considerar para la S4.** La otra prueba diferencial, la de equivalencia de LENGUAJES entre
      `minijava-base.g` y `minijava-ll1-logros.g` con cero logros, con un reconocedor de Earley.
      Es distinta de la que ya existe —ésa compara el analizador contra la gramática final, no las
      dos gramáticas entre sí— y responde de frente a Clase 3 slide 42.
- [ ] Los logros de la Etapa 1 que sigan siendo alcanzables se mantienen; los de Etapa 2 no
      implementables por código son *Entrega Anticipada*, *Imbatibilidad* y *Well Done!*.

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
- Las siete ambigüedades de la gramática se resuelven **voraz**, por regla de implementación, y
  están declaradas en el verificador. Ninguna se resuelve reescribiendo la gramática.
- El inicializador de arreglos es anidable; el orden `static public` se rechaza.

---

## Verificación permanente

Todo esto tiene que pasar al cerrar cualquier sesión:

```bash
cd compilador
python3 herramientas/primeros.py gramatica/minijava-ll1-logros.g                # ES LL(1)
python3 herramientas/primeros.py gramatica/minijava-ll1-logros.g --combinatoria # 1024/1024
bash herramientas/casos-gramatica.sh                                            # 69/69
python3 herramientas/aceptar_fuente.py ../vendor/testersSintactico/resources/*/*.java  # 8/8
mvn verify                                                                      # suite + cobertura
mvn test -Dlogros=ninguno                                                       # cero logros
mvn test -Dlogros=todos                                                         # todos
mvn test -Pfase-lexico                                                          # sólo el léxico
mvn test -Pfase-sintactico                                                      # sólo el sintáctico
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
