# Plan de acción — Etapa 2: Analizador Sintáctico

Índice: [`../AGENTS.md`](../AGENTS.md).

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
completo y obligatorio está en [`investigacion.md`](investigacion.md): chequear primero si MiniJava
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

La estrategia completa y la taxonomía de tipos de test están en [`testing.md`](testing.md). La
Etapa 1 dejó 159 casos de corpus y fuzzing con jqwik; **la Etapa 2 tiene que llegar a un nivel de
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
| **S2** | Cimientos + arranque del parser | Pendiente |
| **S3** | Parser completo | Pendiente |
| **S4** | Robustez, testing y entrega | Pendiente |
| **S5** | Reserva | Pendiente |

Días de 8-10 horas, asistidos. La S5 es colchón: para desvíos, para la corrección del léxico, o
para pulido.

### Lo que dejó la S1

Todo verificado; los comandos están en [Verificación](#verificación-permanente).

- **Gramática LL(1) final y completa**, con los 9 logros de Etapa 2 y el *carry-over* de floats.
  98 no terminales, 204 producciones. **No queda gramática por diseñar en las sesiones siguientes.**
- `compilador/gramatica/` con tres versiones: `minijava-base.g` (transcripción del PDF, 16
  conflictos), `minijava-ll1.g` (núcleo corregido) y `minijava-ll1-logros.g` (la final).
- `compilador/herramientas/`: `primeros.py` (vaciables, Primeros, LL(1), combinatoria de logros,
  rangos de longitud derivable, simulador del Esquema Simple), `aceptar_fuente.py` (tokeniza un
  fuente real con el léxico de la Etapa 1 y lo corre contra la gramática),
  `casos-gramatica.sh` (59 casos), `gramatica_a_markdown.py` y `gramatica_a_latex.py`.
- **Documentación**: `documentacion/gramatica-base-minijava.md`, `guia-de-la-gramatica.md`
  (la gramática original explicada regla por regla), `transformacion-gramatica.md` (el paso a
  paso atómico) y `gramatica-minijava.md` (la final, **generada** desde el `.g`).
- **Informe** en `etapas/etapa-2/entregables/informe/`, 44 páginas, compila sin desbordes ni
  avisos de fuente. Tipografía y color de la cátedra: Computer Modern a 12pt, y el azul `#4C4CB2`
  usado **sólo** en los terminales.
- **59 casos** de aceptación y rechazo propios, más los **8 de la cátedra**, que además fallan en
  el token exacto que declara la expectativa de cada uno.

---

## Protocolo de arranque de sesión

Antes de escribir una línea de código, en este orden:

1. **Leer este archivo entero**, y en particular [Estado](#estado) y [Backlog](#backlog).
2. **Leer [`workflow.md`](workflow.md)** — aplica sin excepción a cualquier tarea.
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
| Consultar una regla de la gramática original | `documentacion/minijava-tecnico/Reglas de Sintaxis MiniJava 2026.pdf` |
| Consultar un token o una regla léxica | `documentacion/minijava-tecnico/Aspectos Lexicos MiniJava 2026.pdf` |
| Entender qué hace cada regla y qué estaba roto | `documentacion/guia-de-la-gramatica.md` |
| Ver cómo se corrigió una regla puntual | `documentacion/transformacion-gramatica.md` |
| Consultar la gramática vigente | `documentacion/gramatica-minijava.md` o el `.g` |
| Escribir o revisar tests | [`testing.md`](testing.md) |
| Una duda de comportamiento no resuelta en ningún lado | [`investigacion.md`](investigacion.md), **antes** de escribir código |
| Compilar, correr, empaquetar, entregar | [`comandos-y-entrega.md`](comandos-y-entrega.md) |

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

### S3 — El parser completo

**Objetivo.** Todos los no terminales de la gramática final implementados.

**Leer antes.** Clase 5 slides 33-62 otra vez, `documentacion/gramatica-minijava.md`.

**Trabajo.** Un método privado por no terminal, en el orden de la gramática. Los conjuntos de
Primeros van como constantes con nombre en un paquete `primeros/`, no inline, cada una con un
comentario que cite la producción, para que el código se lea contra el informe. Mensaje de error
según Pautas slide 25: `Error Sintactico en linea N: se esperaba X se encontro "Y"` más
`[Error:Y|N]`; el lexema de EOF es `$`.

**Terminada cuando.** Los 8 casos de la cátedra pasan con el compilador real, no sólo con el
simulador de la gramática.

### S4 — Robustez, testing y entrega

**Objetivo.** Modo pánico, el corpus de tests, la documentación y el informe final.

**Trabajo.**
1. **Recuperación en Modo Pánico** (logro): al detectar un error, descartar tokens hasta un token
   de sincronización —`;`, `}` o `{` según el contexto, Pautas slide 40— y reanudar. Detrás de su
   flag: apagado, el compilador corta en el primer error como pide la convención base.
2. **Corpus** en `compilador/resources/sintactico/{sinErrores,conErrores}/`, con el criterio de
   robustez de la regla 2 de este documento. El profesor autorizó **asumir programas léxicamente
   válidos** para los tests del sintáctico, así que el corpus no repite casos léxicos. Se reusa la
   mecánica de expectativas y volcado que ya existe (`herramientas/README.md`,
   `casos/VolcadoDeCasos.java`), y **las expectativas se predicen a mano antes de correr**.
3. Actualizar `guias/*.md` y `documentacion/*.md`, y cerrar el informe.

### S5 — Reserva

Colchón. Si no se usa, se dedica a subir cobertura y a buscar más casos raros.

---

## Backlog

Abierto. Lo que dice «consultar» requiere hablarlo con el usuario antes de resolverlo.

- [ ] **Explicar al arrancar la S2, antes de codear.** El flujo `match` ↔ métodos por no terminal,
      con un fuente MiniJava chico corrido paso a paso. Es un pedido explícito del usuario y
      quedó pendiente de la S1.
- [ ] **Revisar.** El usuario va a revisar la gramática antes de la S2. Si de esa revisión sale
      alguna corrección, se aplica sobre el `.g`, se regeneran los documentos y el informe, y se
      vuelve a correr la verificación completa.
- [ ] **Atender cuando llegue.** La corrección del léxico de la Etapa 1. Si aparecen fallos, hay
      que poder arreglarlos y reprobar el léxico aislado, sin tocar el sintáctico.
- [ ] Los logros de la Etapa 1 que sigan siendo alcanzables se mantienen; los de Etapa 2 no
      implementables por código son *Entrega Anticipada*, *Imbatibilidad* y *Well Done!*.

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
- El `this` dentro de una lambda lo rechaza el semántico, no el sintáctico.

---

## Verificación permanente

Todo esto tiene que pasar al cerrar cualquier sesión:

```bash
cd compilador
python3 herramientas/primeros.py gramatica/minijava-ll1-logros.g                # ES LL(1)
python3 herramientas/primeros.py gramatica/minijava-ll1-logros.g --combinatoria # 1024/1024
bash herramientas/casos-gramatica.sh                                            # 59/59
python3 herramientas/aceptar_fuente.py ../vendor/testersSintactico/resources/*/*.java  # 8/8
mvn verify                                                                      # suite + cobertura
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
