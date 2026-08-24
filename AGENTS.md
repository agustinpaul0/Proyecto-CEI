# AGENTS.md — Compilador MiniJava

Memoria operativa del proyecto entre sesiones. Léase antes de tocar código. Este documento fija
**decisiones nuevas** tomadas para este proyecto — no reemplaza las consignas de la cátedra
(`etapas/etapa-N/*.pdf`), las complementa.

## 0. Convención de lectura de material de la cátedra

Las diapositivas (`etapas/etapa-N/*.pdf`) se leen **como imágenes, página por página, nunca sólo con
extracción de texto** (`pdftotext` u otra herramienta de texto). La extracción de texto pierde
diagramas de autómatas, capturas de código superpuestas con globos de explicación, y el orden real
de las anotaciones — en estas slides eso es la mitad de la información técnica. Ejemplo concreto: sin
leer Clase 02 (slides 34-62) como imágenes se pierde el ejemplo completo de implementación de
`AnalizadorLexico` (el AF `e0`-`e5` y su traducción método por método).

**Regla**: al empezar una etapa nueva, releer todos los PDFs de esa etapa como imágenes antes de
diseñar, aunque ya se haya hecho una pasada de texto.

## 1. Contexto y alcance

Materia **Compiladores e Intérpretes** (UNS, 2do cuatrimestre 2026). Proyecto: compilador completo
para **MiniJava** — subconjunto estricto de Java, sin excepciones ni hilos — a lo largo de 5 etapas.

- **Etapa 1** (activa): Analizador Léxico + Módulo Principal cliente por consola.
- Etapas 2-5: sin especificación todavía (aparecerán en `etapas/etapa-2/` etc. cuando se publiquen).
- Consignas de Etapa 1: `etapas/etapa-1/README.md`, `Pautas Etapa 1.pdf`,
  `Aspectos Lexicos MiniJava 2026.pdf`, `Clase 01 - Lex1 - Introduccion.pdf`,
  `Clase 02 - Lex2 - Implementacion Lexico.pdf`.
- Material provisto por la cátedra (no modificar la interfaz, sí se puede reemplazar/extender la
  implementación): `SourceManager`/`SourceManagerImpl` (contenido original intacto; viven en
  `minijava.io`, agrupadas con la interfaz/implementación propia — no en un paquete aparte),
  `testersLexico/` (2 testers JUnit 4 + 5 casos de ejemplo + guía de uso).
- **Calendario**: entrega anticipada (logro) **31/08/2026 16:00**; límite **02/09/2026 16:00**.

**Reglas del usuario para todo el proyecto**: SOLID, archivos ≤ 80 líneas, código reusable y modular,
identificadores/comentarios en español, apuntar a **todos los logros desde el día 1** (no MVP primero
y logros después), **los tests son más importantes que el código** — máxima cobertura de líneas y de
ramas, máxima cantidad de casos, happy paths y edge cases siempre. Desarrollo pensado para abrirse y
correrse en **IntelliJ IDEA**.

## 2. Convenciones de la cátedra — romperlas desaprueba la entrega

Estas NO son negociables, están tomadas literal de `Pautas Etapa 1.pdf` (slides 16-27):

- Salida de un token reconocido, una línea por token: `(NombreToken,Lexema,NroLinea)`.
  Ejemplo real de las slides: `(litString,"hola",1)`, `(idMV,v1,2)`, `(op+,+,2)`, `(pr_if,if,3)`.
- Análisis exitoso → última línea `[SinErrores]`.
- Error léxico → mensaje legible + código de error `[Error:Lexema|NroLinea]` como última línea.
  El código de error usa exactamente el lexema armado hasta el momento del error y la línea del
  fuente MiniJava donde se produjo. Ejemplo: `[Error:#|2]`.
- **Toda** la salida por `System.out` (nada de `System.err`, loggers, etc.).
- El comportamiento por defecto ante un error es **reportar y finalizar la ejecución** — la
  recuperación multi-error es un logro que cambia ese default, no el comportamiento base.
- Invocación: `java -jar Compilador.jar <archivo-fuente>` — debe aceptar **cualquier extensión** de
  archivo, no sólo `.java`.
- Entrega: sólo archivos `.java` (código fuente), un catálogo con los casos de test usados, y el
  informe en PDF.
- Implementación con alguna de las técnicas del Tema 2, **sin herramientas de generación automática**
  (nada de JFlex, ANTLR-lexer, lex/flex, etc.).

## 3. Invariantes técnicas duras

- **Sin `System.exit()` en ningún punto del código de producción.** Los dos testers de la cátedra
  (`TesterDeCasosSinErrores`, `TesterDeCasosConErrores`) invocan `Main.main()` **in-process y
  repetidamente**, una vez por caso parametrizado, dentro del mismo proceso JVM. Un `System.exit()`
  mataría la corrida completa de tests a la primera invocación.
- **Sin estado estático mutable.** Por la misma razón: el proceso de test es compartido entre
  invocaciones sucesivas de `main()`.
- **Regla de línea del EOF**: `línea = cantidad de saltos de línea consumidos + 1`. Verificada contra
  los 3 casos de ejemplo de la cátedra: `lexSinErrores01.java` (21 `\n`, termina en newline → EOF en
  línea **22**), `lexSinErrores02.java` (12 `\n`, no termina en newline → EOF en línea **13**),
  `lexSinErrores03.java` (12 `\n`, termina en newline → EOF en línea **13**).
- **El carácter ofensivo va incluido en el lexema reportado del error.** Confirmado en Clase 02,
  slide 43: el código de ejemplo llama `actualizarLexema()` (que agrega el carácter actual al lexema)
  **inmediatamente antes** de `throw new ExcepcionLexica(lexema, nroLinea)`.
- `Main` vive en `minijava.aplicacion.Main` (ver §4). El FQN exacto **no** es un requisito de la
  cátedra: la guía de testers autoriza explícitamente editar el `import` y el tipo de la variable
  `init` en los testers para apuntar a cualquier clase con `public static void main` — se aprovechó
  esa libertad para evitar el nesting `main/java/main/` (ver bitácora, entrada de aplanamiento).
- El *working directory* de los tests debe ser la raíz del proyecto Maven (`compilador/`), porque los
  testers usan paths relativos duros: `resources/sinErrores/`, `resources/conErrores/`.
- **El código de error `[Error:Lexema|NroLinea]` no lleva columna** — es el contrato exacto de la
  cátedra y el tester lo compara literal. La columna (ver §6, Reporte de Error Elegante) va sólo en
  el mensaje legible que antecede a ese código, nunca dentro de él.

## 4. Arquitectura

### Técnica de implementación — obligatoria, no es una elección abierta

La cátedra presenta tres estrategias en Clase 02 (Método por Estado, Switch/Case, Tabla) y aclara
"nosotros vamos a concentrarnos en esta estrategia" refiriéndose a Método por Estado. **El usuario
confirmó explícitamente que para este proyecto la técnica es Método por Estado y ninguna otra.**

Se implementa como **patrón State (GoF)**: un componente por cada estado del AF que simula el
comportamiento del autómata estando en ese estado — el esquema exacto de las slides 40-41 de Clase 02
(`Si caracterActual = c1 → agregar a lexema, avanzar, retornar resultado de e1() ...`).

**Único margen de decisión propio**: la cátedra encadena llamadas recursivamente (`return e1()`,
slides 38-58 de Clase 02); eso sigue siendo Método por Estado, pero tiene profundidad de pila
proporcional al largo del lexema (`StackOverflowError` real con un string o comentario largo). Se
sustituye el encadenamiento recursivo por un **driver iterativo** (`while`) que invoca la lógica de
transición de cada estado sin llamada recursiva — sigue siendo un componente por estado simulando el
AF, sólo cambia el mecanismo de encadenamiento. Esta decisión y su justificación van explícitas en el
informe para que quede claro que sigue siendo Método por Estado.

```java
public Token proximoToken() throws ExcepcionLexica {
    contexto.iniciarNuevoToken();
    EstadoLexico estado = EstadoInicial.INSTANCIA;
    while (true) {
        ResultadoDeTransicion resultado = estado.procesar(contexto);
        if (resultado instanceof Emitir emitir) return emitir.token();
        estado = ((Continuar) resultado).siguiente();
    }
}
```

### Paquetes (`compilador/src/main/java/`)

```
minijava/
├── aplicacion/     Main (punto de entrada), ModuloPrincipal, FormateadorDeError
├── io/              SourceManager + SourceManagerImpl (interfaz e impl ORIGINALES de la
│                     cátedra, contenido sin tocar — sólo se movió el `package` al mudarlas
│                     acá junto a la interfaz/impl propia que las extiende), ManejadorDeArchivo
│                     (interfaz propia) y ManejadorDeArchivoEficiente (logro, implementación real)
├── lexico/          AnalizadorLexico, contexto léxico, tabla de palabras clave
│   └── estados/     un componente por estado del AF (patrón State)
├── tokens/          Token (record inmutable), TipoDeToken
├── errores/         ExcepcionLexica, tipos de error, formateadores, estrategia de recuperación
└── salida/          interfaz Salida + implementaciones (real / en memoria para tests)
```

Tests bajo `compilador/src/test/java/`: `catedra/` (los 2 testers de la cátedra, paquete propio —
no son entregables, se pueden editar libremente) y `minijava/...` (espejo de la estructura de
producción).

**Por qué `Salida` es una interfaz** (no `System.out` llamado directo): es la decisión de mayor
impacto en la testeabilidad del proyecto — permite testear el Módulo Principal completo sin capturar
el stream global de la JVM, con una implementación `SalidaEnMemoria` para asserts directos.

**Reglas de diseño**: ningún archivo supera 80 líneas; toda dependencia externa a una clase se inyecta
por constructor (`Salida`, `AnalizadorLexico`, `SourceManager`, formateador de error) — permite
sustituir cualquier pieza en tests sin mocks frameworks pesados.

**Reuso concreto para no explotar en clases**: un solo componente `EstadoOperadorConSufijoOpcional`
parametrizado cubre los seis pares `>`/`>=`, `<`/`<=`, `!`/`!=`, `=`/`==`, `+`/`++`, `-`/`--`; los
símbolos de puntuación de un carácter se resuelven con una tabla `Map<Character,TipoDeToken>` en el
estado inicial, sin una clase por símbolo.

## 5. Especificación léxica completa

| Elemento | Regla | Token(s) |
|---|---|---|
| Palabras clave (20) | lookup tras armar un identificador (Clase 02, slide 62-63: **no** se modelan como AF propio, para evitar fusionar decenas de estados con los de identificador) | `pr_class`, `pr_extends`, `pr_interface`, `pr_implements`, `pr_static`, `pr_boolean`, `pr_char`, `pr_int`, `pr_void`, `pr_public`, `pr_if`, `pr_else`, `pr_while`, `pr_return`, `pr_var`, `pr_this`, `pr_new`, `pr_null`, `pr_true`, `pr_false` |
| idClase / idInterfaz | letra mayúscula + uno o más de (letra, dígito, `_`) — mínimo 2 caracteres | `idClase` |
| idGen (parám. genérico) | una única letra mayúscula | `idGen` |
| idMV (método/variable) | letra minúscula + cero o más de (letra, dígito, `_`) | `idMV` |
| Entero | uno a nueve dígitos (se valida el límite de 9 tras consumir la corrida completa) | `intLiteral` |
| Char | `'x'` (x ≠ `\`, `'`) o `'\x'` (x cualquiera) | `charLiteral` |
| String | `"..."` con `\` escapando el carácter siguiente, sin saltos de línea | `litString` |
| Float (logro) | igual que Java, **siempre** empieza con dígito | `floatLiteral` |
| Puntuación | `( ) { } [ ] ; , . :` | `parenA`/`parenC`, `llaveA`/`llaveC`, `corcheteA`/`corcheteC`, `puntoYComa`, `coma`, `punto`, `dosPuntos` |
| Operadores | `> < ! = == >= <= != && \|\| % + - * / ++ --` | `op_mayor`, `op_menor`, `op_negacion`, `op_asignacion`, `op_igual`, `op_mayorIgual`, `op_menorIgual`, `op_distinto`, `op_and`, `op_or`, `op_modulo`, `op+`, `op-`, `op_mult`, `op_div`, `op_incremento`, `op_decremento` |
| EOF | fin de archivo, no consume carácter (Clase 02, estado especial `e5`) | `EOF`, lexema `$` |

Las expresiones regulares exactas de cada token (todas, incluidos los símbolos de un solo carácter)
y sus autómatas están en `documentacion/arquitectura.md` §Especificación léxica formal.

**Corrección de una imprecisión anterior**: en un intento previo se descartó un archivo pensado para
pegar en una herramienta externa de generación de autómatas (no se va a usar esa herramienta — no
aporta valor). De paso, ese archivo afirmaba que el EOF, el comentario multilínea y el límite de 9
dígitos de `intLiteral` "no eran expresables" con precisión como ER/AF. **Eso es incorrecto** y quedó
corregido en `documentacion/arquitectura.md`:
- El EOF es una ER trivial de un símbolo sobre el carácter centinela (`SourceManager.END_OF_FILE`);
  lo único especial es que esa transición no avanza la lectura (detalle del driver, no del lenguaje).
- El comentario multilínea tiene una ER exacta y estándar (la misma que usa `lex`/`flex` para
  comentarios de C): `/\*([^*]|\*+[^*/])*\*+/`. No es una aproximación.
- El límite de 9 dígitos es trivialmente regular (toda repetición acotada lo es): una cadena lineal
  de 9 estados terminales sin bucle, o la ER `D D? D? D? D? D? D? D? D?`.

### Decisiones ambiguas resueltas (documentar también en el informe)

- **`&` y `|` solos son error.** No están en la lista de operadores de `Aspectos Lexicos MiniJava
  2026.pdf` §2.4.2 — sólo `&&` y `||`. Sus estados iniciales son no terminadores.
- **Escapes en string: `\` consume el siguiente carácter** (semántica Java estándar) → `"a\\"` cierra
  correctamente. La lectura literal de la consigna ("la comilla final nunca estará antecedida por
  `\`") daría un string sin cerrar en ese caso; se prioriza la regla general del README
  ("todo lo no especificado se comporta como Java").
- **`'\x'` con `x` arbitrario es válido** — está dicho explícitamente en `Aspectos Lexicos MiniJava
  2026.pdf` §2.3.2. `'\q'` es un `charLiteral` válido con valor `q`.
- **El lexema reportado es siempre el texto crudo del fuente.** Confirmado por
  `testersLexico/Guia_de_uso_testers.md`: se espera `//#'\n',3` (4 caracteres literales), no el
  salto de línea decodificado. El valor decodificado (para etapas posteriores) se guarda aparte en
  el `Token`, nunca reemplaza el lexema.
- **Límite de 9 dígitos**: se consume la corrida completa de dígitos y recién ahí se valida — así no
  se rompe un float largo (`1234567890.5` es válido, la parte entera sola no debe cortar antes).
- **`1e` / `1.5e` sin dígitos después del exponente → error**, sin backtracking a un token más corto.
- **`/*` sin cerrar → error**, con lexema `/*` y la línea de **apertura** del comentario.
- **`1abc` → dos tokens**: `intLiteral(1)` seguido de `idMV(abc)` (maximal munch estándar, igual que
  el lexer de Java).

## 6. Los 8 logros y su estrategia

| Logro | Estrategia |
|---|---|
| Manejador de Archivos Eficiente | **Requisito, no un extra**: ver §7 "por qué el manejador de la cátedra está roto". Char a char sobre buffer de bloques, sólo `java.io`. |
| Multi-detección de Errores | recuperación en el Módulo Principal; invariante a testear con property-based: cada recuperación consume ≥1 carácter o llega a EOF (garantiza terminación). |
| Reporte de Error Elegante | además de línea y razón, mostrar la línea completa del fuente y un `^` apuntando a la posición — **y también columna**, decisión del usuario más allá de lo pedido por la cátedra. |
| Floats I | activable/testeable por flag, ver `ConfiguracionDelCompilador`. |
| Unicodes | caracteres no-ASCII crudos dentro de literales char/string + decodificación de `\uXXXX`. |
| Imbatibilidad Léxica | consecuencia de la suite masiva + corpus de `resources/`. |
| Good Job! I | consecuencia de lo anterior. |
| Entrega Anticipada | hito de calendario: **31/08/2026 16:00**. |

Los cuatro logros de comportamiento (floats, unicode, reporte elegante, multi-detección) se activan
vía `ConfiguracionDelCompilador(floatsHabilitados, unicodeHabilitado, reporteElegante,
multiDeteccionDeErrores)` — todos `true` por defecto — para poder testear cada combinación por
separado y no acoplar el comportamiento base al de los logros.

## 7. Por qué el `SourceManagerImpl` de la cátedra no sirve tal cual

`SourceManagerImpl.java` (contenido original de la cátedra, hoy en `minijava.io`) lee **por líneas**
(`reader.readLine()`) y usa
`reader.ready()` para decidir si hay más contenido. Ese diseño **falla contra los propios casos de
ejemplo de la cátedra**: `testersLexico/resources/sinErrores/lexSinErrores01.java` tiene 21 saltos de
línea y termina en newline; el oráculo (`//#$,22`) espera el EOF en la línea 22, pero
`reader.ready()` hace que el manejador devuelva EOF en la línea 21. También lanza
`NullPointerException` con un archivo vacío (`currentLine` queda `null` tras el primer
`readLine()`).

**Conclusión**: el logro "Manejador de Archivos Eficiente" (leer char a char con un buffer propio,
sólo `java.io`) es un requisito para pasar los tests de la cátedra, no un extra opcional. La interfaz
`SourceManager` se extiende (autorizado explícitamente en Pautas, slide 45: *"puede que por algún
otro logro sea necesario expandir la interface provista"*) con `getColumnNumber()` y
`getCurrentLineText()`, necesarios para el Reporte de Error Elegante.

**Semántica a implementar en el reemplazo**: el contador de línea se incrementa **después** de
devolver un `\n`; con eso los tres casos de EOF de arriba salen correctos sin casos especiales. Los
tres convenios de salto de línea (`\n`, `\r`, `\r\n`) se normalizan a `\n` antes de entregar el
carácter al léxico, así el autómata sólo ve un tipo de salto y cuenta líneas igual sin importar el
convenio del archivo fuente.

## 8. Estrategia de testing

Los tests son la prioridad número uno del proyecto — más importantes que el código de producción en
sí. Esta sección es la que más se va a consultar durante el desarrollo.

### Fases del compilador (marco para las 5 etapas)

```
Fuente → [Léxico] → [Sintáctico] → [AST] → [Tabla de Símbolos / Resolución de Nombres]
       → [Semántico] → [Chequeo de Tipos] → [Generación de Código] → [Ejecución]
```

Etapa 1 cubre sólo **Léxico**; el resto queda `N/A todavía — Etapa N` hasta que se publique esa
etapa. Cuando falla un test end-to-end, tiene que ser posible aislar en qué fase está el problema
usando los tests de esa fase específica — de ahí que los tests se ubiquen y nombren por fase.

### Tipos de test — cuándo corresponde cada uno (no se aplican todos a todo)

| Tipo | Cuándo usarlo | En Etapa 1 |
|---|---|---|
| **Unit** | unidades aisladas y deterministas | cada estado del AF, `Token`, tabla de palabras clave, clasificador de caracteres, formateadores de error, manejador de archivo |
| **Data-driven** | muchas entradas evaluadas con la misma lógica | fuente → stream de tokens esperado, un archivo de tabla por familia léxica (identificadores, enteros, chars, strings, operadores, comentarios, floats, unicode) |
| **Integration** | interacción entre componentes/fases | `SourceManager → AnalizadorLexico`, `AnalizadorLexico → ModuloPrincipal` |
| **E2E** | pipeline completo | `Main.main()` sobre los archivos de `resources/`, más los 2 testers de la cátedra sin modificar |
| **Negative** | programas inválidos deben rechazarse correctamente | un caso por cada `TipoDeErrorLexico` |
| **Regression** | todo bug relevante encontrado | append-only, nunca se borra ni se renumera un caso |
| **Golden/snapshot** | resultados estructurados grandes | salida completa del compilador por archivo de `resources/`, comparada entera |
| **Property-based** | invariantes que deben sostenerse sobre muchas entradas | terminación garantizada, líneas nunca decrecen, round-trip lexema+blancos = fuente original |
| **Fuzz** | robustez del léxico ante entradas inesperadas | bytes/UTF-8 aleatorios: cualquier resultado que no sea `ExcepcionLexica` (o éxito) es un fallo |
| **Mutation** | medir qué tan buena es la suite, no correr en cada cambio | PIT en un profile Maven aparte (`-Pmutacion`) |

**Prioridad**: Nivel 1 obligatorio siempre (unit, data-driven, integration, negative, regression);
Nivel 2 obligatorio cuando existe pipeline completo (E2E — ya existe en Etapa 1); Nivel 3
complementario (golden, property-based, fuzz); Nivel 4 evaluación periódica, no continua (mutation).
No convertir esto en regla rígida si la arquitectura real de una etapa futura pide otra distribución.

### Reglas generales

- Todo código nuevo lleva tests apropiados; no se escriben tests sólo para subir el número de
  cobertura.
- Los tests verifican comportamiento, contratos e invariantes — no implementación interna.
- Deterministas, independientes entre sí, sin depender del orden de ejecución, sin `Thread.sleep`.
- Nunca ocultar un error para hacer pasar un test, ni modificar un test para acomodar una
  implementación que está mal — si la especificación cambia legítimamente, se actualiza el test y se
  documenta el motivo (bitácora, §13).
- Los tests viven cerca de la fase que validan (mismo paquete espejado bajo `src/test/java`), salvo
  los E2E que viven en su propio módulo de tests de integración completa.
- **Todo bug relevante se convierte en test de regresión permanente**: reproducir → escribir el test
  que reproduce el bug → confirmar que falla → arreglar → confirmar que pasa → el caso queda en la
  suite para siempre.

### Taxonomía de errores (severidad distinta para el test)

`error léxico` / `sintáctico` (etapa 2+) / `semántico` (etapa 3+) / `de tipos` (etapa 3+) /
**`interno del compilador`**. Los primeros cuatro son comportamiento **esperado** ante un programa
inválido — nunca deben tratarse como crash. Un **error interno del compilador es siempre un defecto**
(una excepción no controlada, un `NullPointerException`, cualquier cosa que no sea
`ExcepcionLexica`) y **debe hacer fallar el test**, sin excepción.

Los errores esperados se validan por **tipo y localización** (línea, columna), nunca por el texto
completo del mensaje — salvo el código de contrato de la cátedra `[Error:Lexema|NroLinea]`, que sí
se compara literal porque es lo que verifica el tester.

### Fuzzing reproducible

Semilla fija por corrida; todo input que produzca un fallo se persiste como caso permanente en
`compilador/resources/regresion/` (no se descarta al terminar la corrida de fuzzing).

## 9. Workflow del agente

**Antes de implementar algo**: identificar qué fase del compilador toca → identificar qué tipo(s) de
test corresponde(n) según la tabla de §8 → revisar tests existentes relacionados → implementar →
agregar/actualizar tests → correr los tests relevantes → correr la suite completa cuando corresponda
→ investigar y explicar cualquier regresión. **No declarar una tarea terminada sin haber corrido los
tests relevantes, ni con un fallo sin explicar.**

**Ante un bug encontrado**: reproducirlo → escribir el test de regresión → confirmar que falla antes
del fix (cuando sea posible) → implementar el fix → confirmar que el test pasa → correr tests
relacionados → correr la suite completa cuando corresponda.

## 10. Comandos (convención nueva de este proyecto — no existe build todavía)

```bash
cd compilador
mvn test                 # suite completa: la nuestra + los 2 testers de la cátedra (vintage engine)
mvn verify                # + chequeo de umbral de cobertura JaCoCo (falla si baja del umbral)
mvn -Pmutacion test       # PIT — mutantes sobrevivientes = tests faltantes
mvn package                # genera target/Compilador.jar
java -jar target/Compilador.jar resources/sinErrores/lexSinErrores01.java
```

## 11. Formato del informe entregable (para cuando se redacte — no en esta tarea)

El informe pedido por la cátedra (tokens + ER que los define, decisiones relevantes para probar el
compilador, logros perseguidos — Pautas, slide 39) se entrega como **PDF final**; si el paso
intermedio Markdown→PDF resulta más trabajoso que escribir el PDF directamente, se genera el PDF
directamente, sin pasar por Markdown. Requisitos de diseño fijados por el usuario:
**paleta de colores monocromática**, **portada minimalista**, **índice/tabla de contenidos**.

## 12. Regla vital al terminar cualquier feature

Ninguna tarea se considera terminada hasta que se cumplen **las dos** condiciones:

1. **Deuda técnica en 0**: sin TODOs colgados, sin tests deshabilitados o comentados, sin
   implementaciones a medias, sin código muerto dejado "por las dudas". Si algo queda pendiente a
   propósito (por alcance, no por falta de tiempo), se anota explícitamente en `todos/todos.md`, no
   se deja implícito en el código.
2. **Documentación actualizada**: `AGENTS.md` y `documentacion/arquitectura.md` reflejan el estado
   real del código en ese momento — ninguno de los dos puede describir algo que ya cambió o algo que
   todavía no existe. Esto se verifica en cada sesión antes de dar una tarea por cerrada, no sólo al
   final del proyecto.

El historial de decisiones y el calendario **no viven en este archivo**: viven en `git log` (mensajes
de commit descriptivos) y en el repositorio de GitHub. `AGENTS.md` describe el estado actual del
proyecto, no cómo se llegó a él.
