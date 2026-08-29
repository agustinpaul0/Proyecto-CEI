# AGENTS.md — Compilador MiniJava

Memoria operativa del proyecto entre sesiones. Léase antes de tocar código. Este documento (y los
archivos que enlaza bajo `agents/`) fija **decisiones nuevas** tomadas para este proyecto — no
reemplaza las consignas de la cátedra (`etapas/etapa-N/*.pdf`), las complementa.

## 0. Convención de lectura de material de la cátedra

Las diapositivas (`etapas/etapa-N/*.pdf`) se leen **como imágenes, página por página, nunca sólo con
extracción de texto** (`pdftotext` u otra herramienta de texto). La extracción de texto pierde
diagramas de autómatas, capturas de código superpuestas con globos de explicación, y el orden real
de las anotaciones — en estas slides eso es la mitad de la información técnica. Ejemplo concreto: sin
leer Clase 02 (slides 34-62) como imágenes se pierde el ejemplo completo de implementación de
`AnalizadorLexico` (el AF `e0`-`e5` y su traducción método por método).

**Regla**: al empezar una etapa nueva, releer todos los PDFs de esa etapa como imágenes antes de
diseñar, aunque ya se haya hecho una pasada de texto.

## 0.1. Ante la duda, como en Java — y cómo averiguarlo bien

Regla escrita explícitamente por la cátedra, última línea de `etapas/etapa-1/README.md`: *"todo lo
que NO esté especificado para MiniJava tiene comportamiento por defecto igual al de Java"*.

**El procedimiento completo y obligatorio para aplicar esta regla — primero chequear si MiniJava ya
lo resuelve, si no investigar la especificación real de Java (no de memoria), e informar antes de
implementar — está en [`agents/investigacion.md`](agents/investigacion.md). Léase entero antes de
tomar cualquier decisión de comportamiento no cubierta explícitamente en los archivos de `agents/`.**

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

**Reglas del usuario para todo el proyecto**: SOLID, código modular (una responsabilidad clara por
clase — sin límite numérico de líneas, ver [`agents/arquitectura.md`](agents/arquitectura.md)),
identificadores/comentarios en español, apuntar a **todos los logros desde el día 1** (no MVP primero
y logros después), **los tests son más importantes que el código** — máxima cobertura de líneas y de
ramas, máxima cantidad de casos, happy paths y edge cases siempre. Desarrollo pensado para abrirse y
correrse en **IntelliJ IDEA** (abrir la carpeta `compilador/`, donde está el `pom.xml`).

## 2. Qué leer según la tarea

**Siempre, para cualquier tarea**: [`agents/workflow.md`](agents/workflow.md) (workflow paso a paso,
estilo de comentarios, regla de deuda-cero) — es el único archivo que aplica sin excepción.

| Si la tarea es... | Leer |
|---|---|
| Cualquier duda de comportamiento que no aparece resuelta en ningún archivo de `agents/` | [`agents/investigacion.md`](agents/investigacion.md) — **antes** de escribir código |
| Tocar reconocimiento de un token, agregar/cambiar una regla léxica | [`agents/especificacion-lexica.md`](agents/especificacion-lexica.md) + [`documentacion/especificacion-lexica.md`](documentacion/especificacion-lexica.md) (ER formal) |
| Tocar un estado del AF, el driver, paquetes, o cualquier decisión estructural | [`agents/arquitectura.md`](agents/arquitectura.md) + [`documentacion/patron-state.md`](documentacion/patron-state.md) |
| Tocar `ExcepcionLexica`, el reporte de error, o el posicionamiento línea/columna | [`agents/manejo-de-errores.md`](agents/manejo-de-errores.md) |
| Escribir o revisar tests, decidir qué tipo de test corresponde | [`agents/testing.md`](agents/testing.md) |
| Verificar qué es negociable y qué no lo es (contrato de salida, invariantes duras) | [`agents/convenciones-catedra.md`](agents/convenciones-catedra.md) |
| Compilar, correr, empaquetar, o preparar la entrega | [`agents/comandos-y-entrega.md`](agents/comandos-y-entrega.md) |
| Explicarle el proyecto a alguien sin que lea código (onboarding) | todo `documentacion/*.md` — son autocontenidos, no dependen de `agents/` |

## 3. Índice completo de `agents/` (referencia)

| Archivo | Contenido |
|---|---|
| [`agents/investigacion.md`](agents/investigacion.md) | Protocolo obligatorio ante cualquier duda de comportamiento no resuelta explícitamente: MiniJava primero, Java verificado contra la especificación después, informar siempre. |
| [`agents/convenciones-catedra.md`](agents/convenciones-catedra.md) | Convenciones no negociables de la cátedra + invariantes técnicas duras (sin `System.exit()`, regla de línea del EOF, etc.). |
| [`agents/arquitectura.md`](agents/arquitectura.md) | Método por Estado (obligatorio), driver iterativo, `sealed interface` + `switch`, estructura de paquetes, por qué `Salida` es interfaz, por qué el `SourceManagerImpl` de la cátedra no sirve tal cual. |
| [`agents/manejo-de-errores.md`](agents/manejo-de-errores.md) | `ExcepcionLexica`, `PosicionLexica` (cuándo un error reporta el lookahead actual vs. una posición capturada antes), `FormateadorDeError`. |
| [`agents/especificacion-lexica.md`](agents/especificacion-lexica.md) | Tabla de tokens, decisiones ambiguas resueltas (con su fuente), los 8 logros y su estrategia. |
| [`agents/testing.md`](agents/testing.md) | Estrategia de testing completa: tipos de test, cuándo usar cada uno, taxonomía de errores, fuzzing. |
| [`agents/workflow.md`](agents/workflow.md) | Workflow paso a paso antes de implementar y ante un bug; estilo de comentarios; regla vital de deuda técnica cero + documentación actualizada al cerrar cualquier tarea. |
| [`agents/comandos-y-entrega.md`](agents/comandos-y-entrega.md) | Comandos Maven, versión de Java, formato del informe final. |

## 4. Documentación de onboarding (para explicar el proyecto sin leer código)

Escrita para que la lea el profesor: cada documento es autocontenido y explica **por qué** el
compilador hace lo que hace, no sólo qué hace. Empezar por el índice:

- [`documentacion/README.md`](documentacion/README.md) — índice, guía de lectura y resumen en una
  página.
- [`documentacion/arquitectura.md`](documentacion/arquitectura.md) — visión completa: componentes,
  ciclo de vida, decisiones estructurales.
- [`documentacion/especificacion-lexica.md`](documentacion/especificacion-lexica.md) — ER formal de
  los 56 tokens, con autómatas.
- [`documentacion/patron-state.md`](documentacion/patron-state.md) — cómo interactúan
  `EstadoLexico`/`ContextoLexico` (y sus piezas `BuilderDeLexema` y
  `FabricaDeExcepcionesLexicas`)/`Continuar`/`Emitir` y el driver, paso a paso.
- [`documentacion/manejo-de-errores.md`](documentacion/manejo-de-errores.md) — detección,
  recuperación, los dos formatos de reporte, a qué posición apunta la columna.
- [`documentacion/manejador-de-archivo.md`](documentacion/manejador-de-archivo.md) — por qué el
  `SourceManager` provisto no alcanza y qué hace el reemplazo.
- [`documentacion/decisiones.md`](documentacion/decisiones.md) — **el catálogo completo de
  decisiones**, cada una con su fuente, la alternativa descartada y su justificación.
- [`documentacion/testing.md`](documentacion/testing.md) — qué garantiza cada suite y las
  invariantes que sostiene.
- [`documentacion/logros.md`](documentacion/logros.md) — los 8 logros, dónde está cada uno y cómo
  verificarlo.

El historial de decisiones vive en `git log`, no en estos documentos.
