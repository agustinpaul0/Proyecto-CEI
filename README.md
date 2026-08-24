# Proyecto CEI — Compilador MiniJava

Trabajo práctico de la materia **Compiladores e Intérpretes** (UNS). Compilador para el lenguaje
**MiniJava**, desarrollado en 5 etapas. Este repositorio contiene la **Etapa 1: Analizador Léxico**.

## Abrir el proyecto

Desde IntelliJ IDEA: `File → Open` y seleccionar la carpeta **`compilador/`** (ahí vive el `pom.xml`).
IntelliJ importa el proyecto Maven automáticamente y la suite de tests corre desde la GUI sin
configuración adicional (click derecho sobre `src/test/java` → `Run 'All Tests'`).

## Estructura del repositorio

```
AGENTS.md            memoria operativa del proyecto: convenciones, arquitectura, estrategia
                      de testing y workflow — punto de partida para entender cualquier decisión
documentacion/        onboarding autocontenido de la arquitectura (para explicar el proyecto
                      sin leer código)
todos/                pendientes a verificar/implementar (sin historial — eso vive en git log)
etapas/               consignas originales de la cátedra, una carpeta por etapa
vendor/               material de referencia de la cátedra sin modificar (SourceManager, testers)
entregables/          lo que efectivamente se entrega por etapa
compilador/           proyecto Maven — el código fuente y los tests
```

## Compilar y testear

```bash
cd compilador
mvn test        # suite completa (tests propios + testers de la cátedra)
mvn verify       # + chequeo de umbral de cobertura (JaCoCo)
mvn package      # genera compilador/target/Compilador.jar
java -jar target/Compilador.jar resources/sinErrores/lexSinErrores01.java
```

## Más información

- [`AGENTS.md`](AGENTS.md) — convenciones de la cátedra, arquitectura, estrategia de testing.
- [`documentacion/arquitectura.md`](documentacion/arquitectura.md) — cómo funciona el compilador,
  componente por componente.
- [`todos/todos.md`](todos/todos.md) — qué queda pendiente.
