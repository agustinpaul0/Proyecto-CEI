# Etapa 2 — Analizador Sintáctico: estado de cierre

Índice: [`../AGENTS.md`](../../AGENTS.md).

**La etapa está terminada.** Este archivo deja lo que sigue vigente después del cierre: el estado
real del compilador, lo que falta antes de entregar, las reglas y decisiones que no se vuelven a
discutir, y el comando por comando de la verificación. El detalle de cómo se llegó hasta acá
—sesión por sesión— vive en `git log`, no acá.

- **Entrega**: 23/09/2026 a las 16:00, zip por Moodle.
- **Método fijado**: Analizador Descendente Recursivo Predictivo LL(1), **Esquema Simple** de
  Clase 5. Cualquier desvío se documenta con la alternativa evaluada y su razón, y se consulta con
  el usuario antes de aplicarlo.

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

El compilador **está completo**: los diez logros implementados, el corpus poblado, la
documentación y el informe cerrados.

```bash
cd compilador
mvn verify                                                 # 5859 tests, cobertura sobre el umbral
java -jar target/Compilador.jar programa1.java             # analiza sintácticamente
java -jar target/Compilador.jar --etapa=1 programa1.java   # vuelca tokens (Etapa 1)
java -jar target/Compilador.jar --logros=ninguno prog.java # sin ningún logro
```

Qué hay, en números:

| | |
|---|---|
| Gramática | 111 no terminales, 228 producciones, 60 terminales; LL(1) en las 1024 configuraciones |
| Ambigüedades | 3, todas declaradas con su regla de resolución |
| Analizador | una clase por sección de la gramática; cita 234 de 234 producciones |
| Corpus | 1879 fuentes, más 87 casos de gramática y los 8 de la cátedra |
| Suite | 5859 tests; cobertura 98,9 % de líneas y 94,4 % de ramas; mutación 90 % |
| Informe | `etapas/etapa-2/entregables/`, 60 páginas, se compila sin desbordes |

Lo que sostiene que el analizador reconozca **exactamente** el lenguaje de la gramática, y no uno
parecido: tres reconocedores independientes —el analizador en Java, el simulador en Python y un
parser generado por JavaCC— coinciden en veredicto y en token de rechazo sobre las 1879 fuentes;
y la gramática final con cero logros genera el mismo lenguaje que la de la cátedra, verificado con
un reconocedor de Earley y cobertura por producción.

### Estado de la Etapa 1

**Corregida: todos los logros alcanzados, sin errores ni observaciones.** Tiene que seguir siendo
ejecutable y testeable de forma aislada: `--etapa=1` en el `Main` y `mvn test -Pfase-lexico`.

---

## Backlog

Lo único que queda abierto.

- [ ] Los logros de la Etapa 1 que sigan siendo alcanzables se mantienen; los de Etapa 2 no
      implementables por código son *Entrega Anticipada*, *Imbatibilidad* y *Well Done!*.
- [ ] **Antes de entregar.** Armar el zip y medir cuánto tarda la corrida de los testers de la
      cátedra sobre las 1879 fuentes del corpus; si se va de escala, decidir con el usuario qué
      recortar.

---

## Qué se le consulta al usuario antes de ejecutar

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

Todo esto tiene que pasar al cerrar cualquier trabajo sobre la etapa:

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
