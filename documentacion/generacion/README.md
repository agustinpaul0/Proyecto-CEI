# Etapa 5 — Generación de código para la CeIVM

Documentación de la **generación de código**: la traducción del programa, ya chequeado por las
etapas anteriores, a CeIASM, el lenguaje de la máquina virtual de la cátedra (CeIVM). Entrega
20/11/2026 a las 16:00. Las fuentes son el *Apunte CeIVM 2026*, en
[`../minijava/`](../minijava/README.md), Pautas Etapa 5 y las Clases 12 a 16 en `etapas/etapa-5/`.

| | |
|---|---|
| Alcance | Todo el MiniJava base que chequea la Etapa 4: clases, interfaces, genéricos, herencia, estáticos, constructores, locales, `if`, `while`, `return`, llamadas, `new`, arreglos de cualquier dimensión, Strings y los predefinidos. |
| Invocación | `java -jar Compilador.jar programa.java salida.txt`, con cualquier extensión. Es la etapa por defecto. |
| Diseño | Una pasada separada después del chequeo: cada nodo del AST y cada unidad generan sus instrucciones. Los desplazamientos los fija el análisis semántico. |
| Ejecución | `java -jar CeIVM-cei2011.jar salida.txt`, con la CeIVM que está en `compilador/`. |
| Logros | Sólo los que no piden código: Entrega Anticipada, Imbatibilidad y Congrats! |
| Lo que contestó el profesor | [`../../CONVENCIONES-CATEDRA.md`](../../CONVENCIONES-CATEDRA.md) §1 a §6: confirmó las cinco propuestas de [`../../etapas/etapa-5/consultas/dudas.pdf`](../../etapas/etapa-5/consultas/dudas.pdf) y que el `.out` de ejemplo está mal donde contradice al documento. |

## Documentos

| Documento | Qué responde |
|---|---|
| [`arquitectura-de-la-generacion.md`](arquitectura-de-la-generacion.md) | El flujo, las estructuras en ejecución, los desplazamientos y qué genera cada nodo. |
| [`decisiones-de-diseno.md`](decisiones-de-diseno.md) | Cómo quedan en el código las recomendaciones de la cátedra, y las alternativas evaluadas. |
| [`decisiones-de-logica.md`](decisiones-de-logica.md) | Qué se traduce, qué contestó el profesor y los defectos del material. |
| [`decisiones-de-deteccion-de-errores.md`](decisiones-de-deteccion-de-errores.md) | El error del punto de entrada y los errores de ejecución. |
| [`resolucion-de-logros.md`](resolucion-de-logros.md) | Los logros de la etapa y por qué se persiguen sólo tres. |

## Verificación

Desde `compilador/`:

```bash
mvn test -Pfase-generacion                       # los tests de la etapa y los comunes
mvn test -Pfase-generacion -Ddiferencial.programas=3000
java -jar target/Compilador.jar resources/catedra/generacion/gen-04.java salida.txt
java -jar CeIVM-cei2011.jar salida.txt
```

El oráculo nunca es el propio compilador: la salida esperada sale de la cátedra, de Java o de la
cuenta hecha a mano.

| Batería | Casos |
|---|---|
| Cátedra (`resources/catedra/generacion`) | los 4 casos, con su tester, que lanza la CeIVM en un proceso |
| Corpus a mano (`resources/sinErrores`) | 55 programas: la salida de su primera línea, en orden, y la misma que en `javac`/`java` 25 |
| Errores de compilación (`resources/conErrores`) | 10 del punto de entrada, y los 376 de la Etapa 4 corridos por la Etapa 5 |
| Errores de ejecución (`resources/errores-de-ejecucion`) | 18: rango, tamaño negativo, referencias nulas, división por cero |
| Diferencial | 300 programas generados al azar con semillas fijas, comparados con Java; 3.000 con la propiedad |
| Dirigidos | disposición de las VT y los CIR, desplazamientos, etiquetas, escapes, rutinas de soporte |
| Robustez | los ~40.000 fuentes de las etapas anteriores, con un `main` agregado: nunca se cae, y lo generado ensambla |
