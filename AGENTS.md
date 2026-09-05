# AGENTS.md — Compilador MiniJava

Memoria operativa del proyecto entre sesiones. Léase antes de tocar código. Este documento (y los
archivos que enlaza bajo `guias/`) fija **decisiones nuevas** tomadas para este proyecto — no
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
implementar — está en [`guias/investigacion.md`](guias/investigacion.md). Léase entero antes de
tomar cualquier decisión de comportamiento no cubierta explícitamente en los archivos de `guias/`.**

## 1. Contexto y alcance

Materia **Compiladores e Intérpretes** (UNS, 2do cuatrimestre 2026). Proyecto: compilador completo
para **MiniJava** — subconjunto estricto de Java, sin excepciones ni hilos — a lo largo de 5 etapas.

- **Etapa 1** (entregada, pendiente de corrección): Analizador Léxico + Módulo Principal cliente
  por consola. Tiene que seguir siendo ejecutable y testeable de forma aislada.
- **Etapa 2** (activa): Analizador Sintáctico Descendente Recursivo Predictivo LL(1). Entrega
  23/09/2026. **El plan, el estado y el backlog están en
  [`guias/plan-etapa-2.md`](guias/plan-etapa-2.md) — leerlo antes que nada.**
- Etapas 3-5: sin especificación todavía (aparecerán en `etapas/etapa-3/` etc. cuando se publiquen).
- Consignas: `etapas/etapa-N/README.md` y `Pautas Etapa N.pdf`, más las clases de esa etapa.
- Material técnico del lenguaje, común a todas las etapas:
  `documentacion/minijava-tecnico/Aspectos Lexicos MiniJava 2026.pdf` y
  `documentacion/minijava-tecnico/Reglas de Sintaxis MiniJava 2026.pdf`.
- Material provisto por la cátedra (no modificar la interfaz, sí se puede reemplazar/extender la
  implementación): `SourceManager`/`SourceManagerImpl` (contenido original intacto; viven en
  `minijava.io`, agrupadas con la interfaz/implementación propia — no en un paquete aparte),
  `testersLexico/` (2 testers JUnit 4 + 5 casos de ejemplo + guía de uso).

**Reglas del usuario para todo el proyecto**: SOLID, código modular (una responsabilidad clara por
clase — sin límite numérico de líneas, ver [`guias/arquitectura.md`](guias/arquitectura.md)),
identificadores/comentarios en español, apuntar a **todos los logros desde el día 1** (no MVP primero
y logros después), **los tests son más importantes que el código** — máxima cobertura de líneas y de
ramas, máxima cantidad de casos, happy paths y edge cases siempre. Desarrollo pensado para abrirse y
correrse en **IntelliJ IDEA** (abrir la carpeta `compilador/`, donde está el `pom.xml`).

## 2. Qué leer según la tarea

**Siempre, para cualquier tarea**, dos archivos:

1. [`guias/plan-etapa-2.md`](guias/plan-etapa-2.md) — el plan, el estado y el backlog de la etapa
   **activa**. Dice qué se hizo, qué falta, qué material leer, qué explicarle al usuario antes de
   ejecutar y qué consultarle. Se actualiza al cerrar cada sesión de trabajo.
2. [`guias/workflow.md`](guias/workflow.md) — workflow paso a paso, estilo de comentarios, regla
   de deuda-cero.

| Si la tarea es... | Leer |
|---|---|
| Retomar la etapa activa después de un tiempo, o arrancar una sesión nueva | [`guias/plan-etapa-2.md`](guias/plan-etapa-2.md) — **primero, siempre** |
| Tocar la gramática, el parser, los Primeros o el informe de la Etapa 2 | [`guias/plan-etapa-2.md`](guias/plan-etapa-2.md) + [`documentacion/guia-de-la-gramatica.md`](documentacion/guia-de-la-gramatica.md) |
| Cualquier duda de comportamiento que no aparece resuelta en ningún archivo de `guias/` | [`guias/investigacion.md`](guias/investigacion.md) — **antes** de escribir código |
| Tocar reconocimiento de un token, agregar/cambiar una regla léxica | [`guias/especificacion-lexica.md`](guias/especificacion-lexica.md) + [`documentacion/especificacion-lexica.md`](documentacion/especificacion-lexica.md) (ER formal) |
| Tocar un estado del AF, el driver, paquetes, o cualquier decisión estructural | [`guias/arquitectura.md`](guias/arquitectura.md) + [`documentacion/patron-state.md`](documentacion/patron-state.md) |
| Tocar `ExcepcionLexica`, el reporte de error, o el posicionamiento línea/columna | [`guias/manejo-de-errores.md`](guias/manejo-de-errores.md) |
| Escribir o revisar tests, decidir qué tipo de test corresponde | [`guias/testing.md`](guias/testing.md) |
| Verificar qué es negociable y qué no lo es (contrato de salida, invariantes duras) | [`guias/convenciones-catedra.md`](guias/convenciones-catedra.md) |
| Compilar, correr, empaquetar, o preparar la entrega | [`guias/comandos-y-entrega.md`](guias/comandos-y-entrega.md) |
| Explicarle el proyecto a alguien sin que lea código (onboarding) | todo `documentacion/*.md` — son autocontenidos, no dependen de `guias/` |

## 3. Índice completo de `guias/` (referencia)

| Archivo | Contenido |
|---|---|
| [`guias/investigacion.md`](guias/investigacion.md) | Protocolo obligatorio ante cualquier duda de comportamiento no resuelta explícitamente: MiniJava primero, Java verificado contra la especificación después, informar siempre. |
| [`guias/convenciones-catedra.md`](guias/convenciones-catedra.md) | Convenciones no negociables de la cátedra + invariantes técnicas duras (sin `System.exit()`, regla de línea del EOF, etc.). |
| [`guias/arquitectura.md`](guias/arquitectura.md) | Método por Estado (obligatorio), driver iterativo, `sealed interface` + `switch`, estructura de paquetes, por qué `Salida` es interfaz, por qué el `SourceManagerImpl` de la cátedra no sirve tal cual. |
| [`guias/manejo-de-errores.md`](guias/manejo-de-errores.md) | `ExcepcionLexica`, `PosicionLexica` (cuándo un error reporta el lookahead actual vs. una posición capturada antes), `FormateadorDeError`. |
| [`guias/especificacion-lexica.md`](guias/especificacion-lexica.md) | Tabla de tokens, decisiones ambiguas resueltas (con su fuente), los 8 logros y su estrategia. |
| [`guias/testing.md`](guias/testing.md) | Estrategia de testing completa: tipos de test, cuándo usar cada uno, taxonomía de errores, fuzzing. |
| [`guias/workflow.md`](guias/workflow.md) | Workflow paso a paso antes de implementar y ante un bug; estilo de comentarios; regla vital de deuda técnica cero + documentación actualizada al cerrar cualquier tarea. |
| [`guias/comandos-y-entrega.md`](guias/comandos-y-entrega.md) | Comandos Maven, versión de Java, formato del informe final. |
| [`guias/plan-etapa-2.md`](guias/plan-etapa-2.md) | Plan, estado y backlog de la etapa activa; las cuatro reglas que la gobiernan; qué leer y qué consultar en cada sesión. |

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
- [`documentacion/logros.md`](documentacion/logros.md) — los 8 logros de la Etapa 1, dónde está
  cada uno y cómo verificarlo.
- [`documentacion/guia-de-la-gramatica.md`](documentacion/guia-de-la-gramatica.md) — la gramática
  original de MiniJava explicada regla por regla: para qué sirve cada una, por qué está escrita
  así y dónde está rota.
- [`documentacion/gramatica-base-minijava.md`](documentacion/gramatica-base-minijava.md) —
  transcripción fiel del documento de la cátedra, la fuente de verdad.
- [`documentacion/transformacion-gramatica.md`](documentacion/transformacion-gramatica.md) — el
  paso a paso atómico de la corrección a LL(1) y de las extensiones por logro.
- [`documentacion/gramatica-minijava.md`](documentacion/gramatica-minijava.md) — la gramática
  final. **Se genera** desde `compilador/gramatica/minijava-ll1-logros.g`; no se edita a mano.

El historial de decisiones vive en `git log`, no en estos documentos.
