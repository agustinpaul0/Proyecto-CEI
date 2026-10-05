# AGENTS.md — Compilador MiniJava

Memoria operativa del proyecto entre sesiones. Léase antes de tocar código. Este documento (y los
archivos que enlaza bajo `documentacion/proyecto/`) fija **decisiones nuevas** tomadas para este proyecto — no
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

Las slides y las Pautas de `etapas/` **no se versionan** (están en el `.gitignore`): viven sólo en la
copia local, que es de donde se leen.

## 0.1. Ante la duda, como en Java — y cómo averiguarlo bien

Regla escrita explícitamente por la cátedra, última línea de `etapas/etapa-1/README.md`: *"todo lo
que NO esté especificado para MiniJava tiene comportamiento por defecto igual al de Java"*.

**El procedimiento completo y obligatorio para aplicar esta regla — primero chequear si MiniJava ya
lo resuelve, si no investigar la especificación real de Java (no de memoria), e informar antes de
implementar — está en [`documentacion/proyecto/protocolo-de-investigacion.md`](documentacion/proyecto/protocolo-de-investigacion.md). Léase entero antes de
tomar cualquier decisión de comportamiento no cubierta explícitamente en los archivos de `documentacion/proyecto/`.**

## 1. Contexto y alcance

Materia **Compiladores e Intérpretes** (UNS, 2do cuatrimestre 2026). Proyecto: compilador completo
para **MiniJava** — subconjunto estricto de Java, sin excepciones ni hilos — a lo largo de 5 etapas.

- **Etapa 1** (entregada y **corregida**, sin errores ni observaciones): Analizador Léxico + Módulo Principal cliente
  por consola. Tiene que seguir siendo ejecutable y testeable de forma aislada.
- **Etapa 2** (entregada y **corregida**, sin observaciones): Analizador Sintáctico Descendente Recursivo
  Predictivo LL(1). **El estado y la verificación están en
  [`documentacion/sintactico/README.md`](documentacion/sintactico/README.md); las decisiones
  cerradas, en
  [`documentacion/sintactico/decisiones-de-diseno.md`](documentacion/sintactico/decisiones-de-diseno.md).**
- **Etapa 3** (entregada el 05/10/2026 como Entrega Anticipada, falta la corrección): Analizador
  Semántico, Parte I: tabla de símbolos y chequeo de declaraciones. Sin logros de código, por
  decisión del usuario: sólo Entrega Anticipada, Imbatibilidad y Kudos. **El estado y las
  decisiones están en [`documentacion/semantico/README.md`](documentacion/semantico/README.md); lo
  que contestó el profesor, en [`CONVENCIONES-CATEDRA.md`](CONVENCIONES-CATEDRA.md).**
- Etapas 4-5: sin especificación todavía (aparecerán en `etapas/etapa-4/` etc. cuando se publiquen).
- Consignas: `etapas/etapa-N/README.md` y `Pautas Etapa N.pdf`, más las clases de esa etapa.
- Material técnico del lenguaje, común a todas las etapas:
  `documentacion/minijava/Aspectos Lexicos MiniJava 2026.pdf`,
  `documentacion/minijava/Reglas de Sintaxis MiniJava 2026.pdf` y
  `documentacion/minijava/MiniJava-Semantica-2026-Parte 1.pdf`.
- Material provisto por la cátedra (no modificar la interfaz, sí se puede reemplazar/extender la
  implementación): `SourceManager`/`SourceManagerImpl` (contenido original intacto; viven en
  `minijava.comun.io`, agrupadas con la interfaz/implementación propia — no en un paquete aparte),
  `testersLexico/` (2 testers JUnit 4 + 5 casos de ejemplo + guía de uso), `testersSintactico/`
  (2 testers JUnit 4 + 8 casos de ejemplo + guía de uso) y `testersSemantico1/` (2 testers JUnit 4
  + 8 casos de ejemplo, sin guía). Los originales están en `vendor/`, que **no se versiona** y no
  lo lee nada del proyecto: lo que se usa está copiado adentro de `compilador/` (los testers en
  `src/test/java/catedra/`, los casos de ejemplo en `resources/catedra/sintactico/` y
  `resources/catedra/semantico/`).
- Material ajeno de un compañero: `compilador/resources/corpus-externo/`, el corpus de
  fuzzing de Giuliano Taormina ya podado, que corre `CorpusExternoTest`. La atribución está en su
  `CREDITOS.md`; la poda, en `documentacion/compartido/estrategia-de-testing.md`.
- Lo que el profesor indicó explícitamente está reunido en
  [`CONVENCIONES-CATEDRA.md`](CONVENCIONES-CATEDRA.md).

**Reglas del usuario para todo el proyecto**: SOLID, código modular (una responsabilidad clara por
clase — sin límite numérico de líneas, ver [`documentacion/lexico/arquitectura-del-analizador-lexico.md`](documentacion/lexico/arquitectura-del-analizador-lexico.md)),
identificadores/comentarios en español, **los tests son más importantes que el código** — máxima
cobertura de líneas y de ramas, máxima cantidad de casos, happy paths y edge cases siempre.

**Comentarios: cero por defecto.** El objetivo es que el código se explique solo; un comentario se
escribe **únicamente** cuando el código no puede decir lo que hace falta por sí mismo — una decisión
no obvia, el motivo de un workaround, una referencia externa que justifica el comportamiento (slide
de la cátedra, sección del JLS), o una invariante que se rompería en silencio si alguien la tocara
sin saberla. Un comentario que repite el nombre del método o de la variable es ruido: siempre que se
pueda, en lugar de comentar hay que renombrar o extraer. Ante la duda, no comentar. El detalle está
en [`documentacion/proyecto/flujo-de-trabajo.md`](documentacion/proyecto/flujo-de-trabajo.md).

Desarrollo pensado para abrirse y correrse en **IntelliJ IDEA** (abrir la carpeta `compilador/`,
donde está el `pom.xml`). La configuración versionada «Suite Etapa 3», de `compilador/.run/`, corre
sólo los tests de la etapa que se entrega; no va en el zip de entrega (`.gitattributes`).

## 2. Qué leer según la tarea

**Siempre, para cualquier tarea**, dos archivos:

1. [`documentacion/proyecto/README.md`](documentacion/proyecto/README.md) — las tres reglas que
   no se negocian y el índice de los documentos normativos.
2. [`documentacion/proyecto/flujo-de-trabajo.md`](documentacion/proyecto/flujo-de-trabajo.md) — workflow paso a paso, estilo de comentarios, regla
   de deuda-cero.

| Si la tarea es... | Leer |
|---|---|
| Retomar el proyecto después de un tiempo | [`documentacion/README.md`](documentacion/README.md) y [`documentacion/proyecto/README.md`](documentacion/proyecto/README.md) — **primero, siempre** |
| Tocar la gramática, el parser, los Primeros o el informe de la Etapa 2 | [`documentacion/sintactico/README.md`](documentacion/sintactico/README.md) + [`documentacion/sintactico/decisiones-de-diseno.md`](documentacion/sintactico/decisiones-de-diseno.md) (las decisiones cerradas) |
| Entender Primeros, LL(1), factorización, o implementar el parser | `etapas/etapa-2/Clase 3 - Sint1…pdf`, `Clase 4 - Sint2…pdf` y `Clase 5 - Sint3…pdf` (slides 33-62) |
| Consultar el contrato de salida, los logros o la invocación de la Etapa 2 | `etapas/etapa-2/Pautas Etapa 2.pdf` (errores 25-26, invocación 28, logros 34-44) |
| Tocar la tabla de símbolos, la EDT o un control semántico | [`documentacion/semantico/README.md`](documentacion/semantico/README.md) + `documentacion/minijava/MiniJava-Semantica-2026-Parte 1.pdf` (los controles) + `etapas/etapa-3/Clase 6…8` |
| Consultar el contrato de salida, el token de cada error o los logros de la Etapa 3 | `etapas/etapa-3/Pautas Etapa 3.pdf` (errores 28-30, éxito 31, informe 36, logros 38-41) |
| Cualquier duda de comportamiento que no aparece resuelta en ningún archivo de `documentacion/proyecto/` | [`documentacion/proyecto/protocolo-de-investigacion.md`](documentacion/proyecto/protocolo-de-investigacion.md) — **antes** de escribir código |
| Tocar reconocimiento de un token, agregar/cambiar una regla léxica | [`documentacion/lexico/especificacion-lexica-de-minijava.md`](documentacion/lexico/especificacion-lexica-de-minijava.md) (tokens, ER formal y decisiones) |
| Tocar un estado del AF, el driver, paquetes, o cualquier decisión estructural | [`documentacion/lexico/arquitectura-del-analizador-lexico.md`](documentacion/lexico/arquitectura-del-analizador-lexico.md) + [`documentacion/lexico/patron-state-metodo-por-estado.md`](documentacion/lexico/patron-state-metodo-por-estado.md) |
| Tocar `ExcepcionLexica`, el reporte de error, o el posicionamiento línea/columna | [`documentacion/compartido/manejo-de-errores.md`](documentacion/compartido/manejo-de-errores.md) |
| Escribir o revisar tests, decidir qué tipo de test corresponde | [`documentacion/compartido/estrategia-de-testing.md`](documentacion/compartido/estrategia-de-testing.md) |
| Verificar qué es negociable y qué no lo es (contrato de salida, invariantes duras) | [`documentacion/proyecto/convenciones-de-la-catedra.md`](documentacion/proyecto/convenciones-de-la-catedra.md) |
| Compilar, correr, empaquetar, o preparar la entrega | [`documentacion/proyecto/comandos-compilacion-y-entrega.md`](documentacion/proyecto/comandos-compilacion-y-entrega.md) |
| Explicarle el proyecto a alguien sin que lea código (onboarding) | todo `documentacion/*.md` — son autocontenidos, no dependen de `documentacion/proyecto/` |

## 3. Índice de `documentacion/proyecto/` — las normas de trabajo

| Archivo | Contenido |
|---|---|
| [`documentacion/proyecto/protocolo-de-investigacion.md`](documentacion/proyecto/protocolo-de-investigacion.md) | Protocolo obligatorio ante cualquier duda de comportamiento no resuelta explícitamente: MiniJava primero, Java verificado contra la especificación después, informar siempre. |
| [`documentacion/proyecto/convenciones-de-la-catedra.md`](documentacion/proyecto/convenciones-de-la-catedra.md) | Convenciones no negociables de la cátedra, de las tres etapas, + invariantes técnicas duras (sin `System.exit()`, regla de línea del EOF, etc.). |
| [`documentacion/proyecto/flujo-de-trabajo.md`](documentacion/proyecto/flujo-de-trabajo.md) | Workflow paso a paso antes de implementar y ante un bug; estilo de comentarios; regla vital de deuda técnica cero + documentación actualizada al cerrar cualquier tarea. |
| [`documentacion/proyecto/comandos-compilacion-y-entrega.md`](documentacion/proyecto/comandos-compilacion-y-entrega.md) | Comandos Maven, versión de Java, armado del zip y formato del informe. |

El estado de cada etapa y lo que queda abierto está en [`ESTADO.md`](ESTADO.md).

## 4. Documentación técnica (para explicar el proyecto sin leer código)

Escrita para que la lea el profesor: cada documento es autocontenido y explica **por qué** el
compilador hace lo que hace, no sólo qué hace. Empezar por el índice,
[`documentacion/README.md`](documentacion/README.md), que tiene la guía de lectura; cada carpeta
tiene además su propio README.

Etapa 1, en [`documentacion/lexico/`](documentacion/lexico/README.md):

- [`arquitectura-del-analizador-lexico.md`](documentacion/lexico/arquitectura-del-analizador-lexico.md)
  — componentes, ciclo de vida, decisiones estructurales.
- [`especificacion-lexica-de-minijava.md`](documentacion/lexico/especificacion-lexica-de-minijava.md)
  — la ER formal de los tokens, con autómatas.
- [`patron-state-metodo-por-estado.md`](documentacion/lexico/patron-state-metodo-por-estado.md) —
  cómo interactúan `EstadoLexico`/`ContextoLexico` (y sus piezas `BuilderDeLexema` y
  `FabricaDeExcepcionesLexicas`)/`Continuar`/`Emitir` y el driver, paso a paso.
- [`decisiones-de-diseno.md`](documentacion/lexico/decisiones-de-diseno.md),
  [`decisiones-de-logica.md`](documentacion/lexico/decisiones-de-logica.md) y
  [`decisiones-de-deteccion-de-errores.md`](documentacion/lexico/decisiones-de-deteccion-de-errores.md)
  — cada decisión con su fuente, la alternativa descartada y su justificación.
- [`resolucion-de-logros.md`](documentacion/lexico/resolucion-de-logros.md) — los 8 logros de la
  Etapa 1, dónde está cada uno y cómo verificarlo.

Etapa 2, en [`documentacion/sintactico/`](documentacion/sintactico/README.md):

- [`gramatica-original-de-la-catedra.md`](documentacion/sintactico/gramatica-original-de-la-catedra.md)
  — transcripción fiel del documento de la cátedra, la fuente de verdad.
- [`defectos-de-la-gramatica-original.md`](documentacion/sintactico/defectos-de-la-gramatica-original.md)
  — la gramática original explicada regla por regla: para qué sirve cada una y dónde está rota.
- [`transformacion-a-ll1-paso-a-paso.md`](documentacion/sintactico/transformacion-a-ll1-paso-a-paso.md)
  — el paso a paso atómico de la corrección a LL(1) y de las extensiones por logro.
- [`gramatica-ll1-final.md`](documentacion/sintactico/gramatica-ll1-final.md) — la gramática final.
  **Se genera** desde `compilador/gramatica/minijava-ll1-final.g`; no se edita a mano.
- [`arquitectura-del-analizador-sintactico.md`](documentacion/sintactico/arquitectura-del-analizador-sintactico.md),
  [`decisiones-de-diseno.md`](documentacion/sintactico/decisiones-de-diseno.md),
  [`decisiones-de-logica.md`](documentacion/sintactico/decisiones-de-logica.md),
  [`decisiones-de-deteccion-de-errores.md`](documentacion/sintactico/decisiones-de-deteccion-de-errores.md)
  y [`resolucion-de-logros.md`](documentacion/sintactico/resolucion-de-logros.md).

Etapa 3, en [`documentacion/semantico/`](documentacion/semantico/README.md):
[`arquitectura-del-analizador-semantico.md`](documentacion/semantico/arquitectura-del-analizador-semantico.md)
(con el diagrama de clases de la tabla),
[`esquema-de-traduccion.md`](documentacion/semantico/esquema-de-traduccion.md),
[`decisiones-de-diseno.md`](documentacion/semantico/decisiones-de-diseno.md),
[`decisiones-de-logica.md`](documentacion/semantico/decisiones-de-logica.md),
[`decisiones-de-deteccion-de-errores.md`](documentacion/semantico/decisiones-de-deteccion-de-errores.md)
y [`resolucion-de-logros.md`](documentacion/semantico/resolucion-de-logros.md).

Compartida entre etapas, en [`documentacion/compartido/`](documentacion/compartido/README.md):
[`estrategia-de-testing.md`](documentacion/compartido/estrategia-de-testing.md),
[`manejo-de-errores.md`](documentacion/compartido/manejo-de-errores.md) y
[`manejador-de-archivo-eficiente.md`](documentacion/compartido/manejador-de-archivo-eficiente.md).

El historial de decisiones vive en `git log`, no en estos documentos.
