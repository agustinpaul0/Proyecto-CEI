# Etapa 3 — Analizador Semántico, Parte I: chequeo de declaraciones

Documentación de la **tabla de símbolos** y del **chequeo de declaraciones**. Entrega 09/10/2026.
La especificación es «MiniJava-Semantica-2026-Parte 1», en
[`../minijava/`](../minijava/README.md), junto con Pautas Etapa 3 y las Clases 6, 7 y 8 en
`etapas/etapa-3/`.

| | |
|---|---|
| Alcance | Las declaraciones: clases, interfaces, atributos, encabezados de métodos y constructores, y parámetros. Los cuerpos quedan para la Etapa 4. |
| Construcción | Acciones de un Esquema de Traducción sobre la gramática LL(1) de la Etapa 2 |
| Chequeo | Nombres repetidos al parsear; después `estaBienDeclarado()` y `consolidar()` sobre la tabla |
| Controles | 20, más el constructor por defecto |
| Logros | Sólo los que no piden código: Entrega Anticipada, Imbatibilidad y Kudos |
| Corpus | 90 casos con error, 43 correctos y los 8 de la cátedra |
| Lo que contestó el profesor | [`../../CONVENCIONES-CATEDRA.md`](../../CONVENCIONES-CATEDRA.md) §1 a §6 |

## Documentos

| Documento | Qué responde |
|---|---|
| [`arquitectura-del-analizador-semantico.md`](arquitectura-del-analizador-semantico.md) | Cómo está hecha la tabla, el diagrama de clases y el flujo completo de una compilación. |
| [`esquema-de-traduccion.md`](esquema-de-traduccion.md) | La EDT: qué acción lleva cada producción de las declaraciones. |
| [`decisiones-de-diseno.md`](decisiones-de-diseno.md) | Tabla estática, repetidos diferidos, retornar o delegar, y los dos atributos de entrada. |
| [`decisiones-de-logica.md`](decisiones-de-logica.md) | Qué se controla, qué no, y lo que se resolvió como en Java. |
| [`decisiones-de-deteccion-de-errores.md`](decisiones-de-deteccion-de-errores.md) | Cada error con su token y el orden en que se detectan. |
| [`resolucion-de-logros.md`](resolucion-de-logros.md) | Los logros de la etapa y por qué se persiguen sólo tres. |

## Verificación

Desde `compilador/`:

```bash
mvn test -Pfase-semantico     # los tests de la etapa y los comunes
mvn test                      # la suite completa, las tres etapas
java -jar target/Compilador.jar resources/catedra/semantico/conErrores/semIError03.java
```
