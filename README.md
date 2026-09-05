# Proyecto CEI — Compilador MiniJava

Trabajo práctico de la materia **Compiladores e Intérpretes** (UNS). Compilador para el lenguaje
**MiniJava**, desarrollado en 5 etapas. Este repositorio contiene la **Etapa 1: Analizador Léxico**.

## Abrir el proyecto

Desde IntelliJ IDEA: `File → Open` y seleccionar la carpeta **`compilador/`** (ahí vive el `pom.xml`).
IntelliJ importa el proyecto Maven automáticamente y la suite de tests corre desde la GUI sin
configuración adicional (click derecho sobre `src/test/java` → `Run 'All Tests'`).

## Estructura del repositorio

```
AGENTS.md             memoria operativa del proyecto: convenciones, arquitectura, estrategia
                      de testing y workflow — punto de partida para entender cualquier decisión
CLAUDE.md             importa AGENTS.md para que Claude Code lo cargue solo en cada sesión
guias/                las guías normativas que AGENTS.md indexa: qué hacer y qué no, para quien
                      escribe código (humano o agente)
documentacion/        documentación técnica autocontenida (arquitectura, especificación léxica,
                      decisiones, testing, logros) — para explicar el proyecto sin leer código
etapas/               consignas originales, material y entregables de la cátedra, una carpeta por etapa
vendor/               copia de referencia intacta de los zips originales de la cátedra
compilador/           proyecto Maven — el código fuente y los tests
.claude/              configuración de Claude Code: permisos, hook de tests, comandos y skills
```

`guias/` y `documentacion/` comparten cuatro nombres de archivo (`arquitectura.md`,
`especificacion-lexica.md`, `testing.md`, `manejo-de-errores.md`) porque cubren los mismos temas
desde dos ángulos: `guias/` es **normativo** (qué hacer), `documentacion/` es **explicativo** (por
qué). Al citar uno de esos archivos, incluir siempre la carpeta.

## Ante la duda, como en Java

Regla explícita de la cátedra (`etapas/etapa-1/README.md`): *"todo lo que NO esté especificado para
MiniJava tiene comportamiento por defecto igual al de Java"*. Cuando el comportamiento léxico de un
caso puntual no está resuelto explícitamente por la consigna ni por una decisión ya registrada en
`AGENTS.md`, el criterio de desempate es replicar la semántica real de Java (ver `AGENTS.md` §0.1) —
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
java -jar target/Compilador.jar resources/sinErrores/lexSinErrores01.java
```

## Más información

- [`AGENTS.md`](AGENTS.md) — memoria operativa del proyecto e índice hacia `guias/` (convenciones
  de la cátedra, protocolo de investigación, arquitectura, testing, workflow).
- [`documentacion/README.md`](documentacion/README.md) — la documentación técnica completa: cómo
  funciona el compilador componente por componente, la especificación léxica formal, el catálogo de
  decisiones con su justificación, la estrategia de testing y los 8 logros. Sin necesidad de leer
  código.
- [`etapas/etapa-1/entregables/Informe-Etapa1-Lexico.pdf`](etapas/etapa-1/entregables/Informe-Etapa1-Lexico.pdf) —
  el informe de entrega: los 56 tokens con su ER, los autómatas, las decisiones tomadas y los logros.
  Se regenera con `latexmk -pdf informe.tex` desde `etapas/etapa-1/entregables/informe/`.
