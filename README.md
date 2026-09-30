# Proyecto CEI — Compilador MiniJava

Trabajo práctico de la materia **Compiladores e Intérpretes** (UNS). Compilador para el lenguaje
**MiniJava**, desarrollado en 5 etapas. Este repositorio contiene la **Etapa 1: Analizador Léxico**,
la **Etapa 2: Analizador Sintáctico** y la **Etapa 3: Analizador Semántico, Parte I** (tabla de
símbolos y chequeo de declaraciones).

## Abrir el proyecto

Desde IntelliJ IDEA: `File → Open` y seleccionar la carpeta **`compilador/`** (ahí vive el `pom.xml`).
IntelliJ importa el proyecto Maven automáticamente, junto con las configuraciones de ejecución de
`compilador/.run/`. **«Suite Etapa 3»** corre los tests de la etapa que se entrega (los propios y
los testers de la cátedra) más los comunes; «Suite Etapa 3 (Maven)» hace lo mismo con
`mvn test -Pfase-semantico`. La suite entera de las tres etapas sigue corriendo con click derecho
sobre `src/test/java` → `Run 'All Tests'`.

## Estructura del repositorio

```
AGENTS.md             memoria operativa del proyecto: convenciones, arquitectura, estrategia
                      de testing y workflow — punto de partida para entender cualquier decisión
CLAUDE.md             importa AGENTS.md para que Claude Code lo cargue solo en cada sesión
ESTADO.md             dónde está cada etapa y qué queda pendiente
dudas.md              las consultas abiertas para el profesor
documentacion/        toda la documentación, organizada por fase del compilador:
                        lexico/       Etapa 1 — analizador léxico
                        sintactico/   Etapa 2 — analizador sintáctico y la gramática
                        semantico/    Etapa 3 — tabla de símbolos y chequeo de declaraciones
                        compartido/   lo que cruza etapas: testing, errores, manejador de archivo
                        proyecto/     cómo se trabaja: flujo, protocolo, convenciones y comandos
                        minijava/     los PDF de la cátedra que describen el lenguaje
etapas/               consignas, material y entregables de la cátedra, una carpeta por etapa
vendor/               material de terceros tal como llegó, no versionado: sólo para copiar de ahí
compilador/           proyecto Maven — el código fuente, los tests y las gramáticas
.claude/              configuración de Claude Code: permisos, hook de tests, comandos y skills
.agents/, .codex/     las mismas skills y el mismo hook, para otros agentes (Codex)
                      — todo lo usado para desarrollar con IA se versiona, a pedido del profesor
```

El índice de la documentación es [`documentacion/README.md`](documentacion/README.md), y cada
subcarpeta tiene su propio README con la guía de lectura.

## Ante la duda, como en Java

Regla explícita de la cátedra (`etapas/etapa-1/README.md`): *"todo lo que NO esté especificado para
MiniJava tiene comportamiento por defecto igual al de Java"*. Cuando el comportamiento de un caso
puntual no está resuelto explícitamente por la consigna ni por una decisión ya registrada en
`documentacion/`, el criterio de desempate es replicar la semántica real de Java (ver `AGENTS.md` §0.1) —
nunca inventar una regla propia. Si la cátedra publica después una regla explícita que diga otra
cosa, esa regla explícita gana.

## Compilar y testear

```bash
cd compilador
mvn test        # suite completa (tests propios + testers de la cátedra)
mvn verify       # + chequeo de umbral de cobertura (JaCoCo)

# La corrida imprime, por cada caso de resources/, la salida real del compilador.
mvn test -Dvolcado=resumen   # una línea por caso + sus códigos de error
mvn test -Dvolcado=off       # sin volcado
mvn package      # genera compilador/target/Compilador.jar
java -jar target/Compilador.jar resources/sinErrores/herenciaGenericaConArgumento.java
java -jar target/Compilador.jar --etapa=2 resources/sintactico/sinErrores/todosLosLogrosJuntos.java
java -jar target/Compilador.jar --etapa=1 resources/lexico/sinErrores/lexSinErrores01.java
```

## Más información

- [`AGENTS.md`](AGENTS.md) — memoria operativa del proyecto e índice hacia el resto (convenciones
  de la cátedra, protocolo de investigación, arquitectura, testing, workflow).
- [`documentacion/README.md`](documentacion/README.md) — la documentación técnica completa: cómo
  funciona el compilador componente por componente, la especificación léxica formal, el catálogo de
  decisiones con su justificación, la estrategia de testing y los logros de cada etapa. Sin
  necesidad de leer código.
- [`etapas/etapa-1/entregables/Informe-Etapa1-Lexico.pdf`](etapas/etapa-1/entregables/Informe-Etapa1-Lexico.pdf) —
  el informe de entrega: los 56 tokens con su ER, los autómatas, las decisiones tomadas y los logros.
  Se regenera con `latexmk -pdf informe.tex` desde `etapas/etapa-1/entregables/informe/`.
- [`etapas/etapa-2/entregables/Informe-Etapa2-Sintactico.pdf`](etapas/etapa-2/entregables/Informe-Etapa2-Sintactico.pdf) —
  el informe de la Etapa 2: la transformación de la gramática a LL(1), la gramática final, los
  errores que detecta el analizador y los logros. Se regenera igual, desde
  `etapas/etapa-2/entregables/informe/`.
- [`etapas/etapa-3/entregables/Informe-Etapa3-Semantico.pdf`](etapas/etapa-3/entregables/Informe-Etapa3-Semantico.pdf) —
  el informe de la Etapa 3: cómo compilar y correr, el diagrama de clases de la tabla de símbolos y
  los logros. Se regenera igual, desde `etapas/etapa-3/entregables/informe/`.
