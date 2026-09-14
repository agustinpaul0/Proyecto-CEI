# Documentación del Compilador MiniJava

Proyecto de la materia **Compiladores e Intérpretes** (Departamento de Ciencias e Ingeniería de la
Computación, Universidad Nacional del Sur, 2° cuatrimestre 2026). El compilador se desarrolla en
**cinco etapas**.

Toda esta carpeta es **autocontenida**: se puede leer entera sin abrir una sola línea de código.
Está escrita para que un lector externo entienda no sólo *qué* hace el compilador, sino **por qué
hace cada cosa de la forma en que la hace**, y de dónde sale cada decisión.

## Cómo está organizada

| Carpeta | Qué hay adentro |
|---|---|
| [`lexico/`](lexico/README.md) | **Etapa 1 — Analizador Léxico.** Arquitectura, especificación de los tokens, decisiones y logros. |
| [`sintactico/`](sintactico/README.md) | **Etapa 2 — Analizador Sintáctico.** La gramática (original, transformación y final), arquitectura, decisiones y logros. |
| [`compartido/`](compartido/README.md) | Lo que **no pertenece a una sola etapa**: la estrategia de testing, el manejo de errores y el manejador de archivo. |
| [`proyecto/`](proyecto/README.md) | Cómo se trabaja: el **plan de la etapa activa**, el flujo de trabajo, el protocolo de investigación, las convenciones de la cátedra y los comandos. |
| [`minijava/`](minijava/README.md) | Los PDF de la cátedra que describen **el lenguaje**, no una etapa: aspectos léxicos y reglas de sintaxis. |

## Por dónde empezar

**Si querés entender el compilador**, en este orden:

1. [`lexico/arquitectura-del-analizador-lexico.md`](lexico/arquitectura-del-analizador-lexico.md)
   — qué hace el proyecto y cómo está construido.
2. [`sintactico/README.md`](sintactico/README.md) — la etapa en curso.
3. [`compartido/manejo-de-errores.md`](compartido/manejo-de-errores.md) — cómo se reporta un error.

**Si vas a escribir código**, primero:

1. [`proyecto/README.md`](proyecto/README.md) — las cuatro reglas que no se negocian y el índice
   de los documentos normativos.
2. [`proyecto/flujo-de-trabajo.md`](proyecto/flujo-de-trabajo.md) — las reglas que aplican sin
   excepción.

## Estado de las etapas

| Etapa | Qué es | Estado |
|---|---|---|
| 1 | Analizador Léxico + Módulo Principal | entregada y corregida, sin observaciones |
| 2 | Analizador Sintáctico Descendente Recursivo | **completa** — gramática, analizador, los diez logros y el corpus |
| 3 a 5 | Semántico y siguientes | sin especificación todavía |

## Las dos reglas que gobiernan todas las decisiones

1. **Lo que la cátedra especifica, se respeta al pie de la letra.** Cuando un documento de acá
   afirma algo de la cátedra, cita la fuente: el PDF y la slide.
2. **Lo que la cátedra no especifica, se resuelve como en Java** — regla escrita explícitamente por
   ellos. Y «como en Java» nunca se resuelve de memoria: se verifica contra la especificación o
   **ejecutando `javac`** sobre el caso concreto, y el resultado queda registrado y fijado en un
   test.

Las dos reglas dejan un residuo: los casos donde MiniJava **diverge** de Java a propósito, porque
la cátedra especificó algo distinto. Están enumerados en
[`lexico/decisiones-de-logica.md`](lexico/decisiones-de-logica.md), porque son exactamente los que
un evaluador querría revisar.
