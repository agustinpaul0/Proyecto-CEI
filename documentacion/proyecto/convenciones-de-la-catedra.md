# Convenciones de la cátedra e invariantes técnicas duras

Índice: [`../AGENTS.md`](../../AGENTS.md). Protocolo si algo no está cubierto acá:
[`protocolo-de-investigacion.md`](protocolo-de-investigacion.md).

## Convenciones de la cátedra — romperlas desaprueba la entrega

Estas NO son negociables, están tomadas literal de `Pautas Etapa 1.pdf` (slides 16-27):

- Salida de un token reconocido, una línea por token: `(NombreToken,Lexema,NroLinea)`.
  Ejemplo real de las slides: `(litString,"hola",1)`, `(idMV,v1,2)`, `(op+,+,2)`, `(pr_if,if,3)`.
- Análisis exitoso → última línea `[SinErrores]`.
- Error léxico → mensaje legible + código de error `[Error:Lexema|NroLinea]` como última línea de
  cada reporte de error.
  El código de error usa exactamente el lexema armado hasta el momento del error y la línea del
  fuente MiniJava donde se produjo. Ejemplo: `[Error:#|2]`.
- **Toda** la salida por `System.out` (nada de `System.err`, loggers, etc.).
- El comportamiento por defecto ante un error es **reportar y finalizar la ejecución** — la
  recuperación multi-error es un logro que cambia ese default, no el comportamiento base.
- Invocación: `java -jar Compilador.jar <archivo-fuente>` — debe aceptar **cualquier extensión** de
  archivo, no sólo `.java`.
- Entrega: sólo archivos `.java` (código fuente), un catálogo con los casos de test usados, y el
  informe en PDF.
- Implementación con alguna de las técnicas del Tema 2, **sin herramientas de generación automática**
  (nada de JFlex, ANTLR-lexer, lex/flex, etc.).

La Etapa 2 agrega las suyas, de `Pautas Etapa 2.pdf`:

- El sintáctico es un **Analizador Descendente Recursivo**, con el esquema simple visto en clase o
  el avanzado (slide 10).
- Error sintáctico → mensaje con el lexema del token con el que se produjo y su línea, y al final
  `[Error:Lexema|NroLinea]` con esos mismos datos. Ejemplo de la slide 25: `class {` produce
  `Error Sintactico en linea 1: se esperaba un id de clase se encontro "{"` y `[Error:{|1]`.
- Si el error se produce en el fin de archivo, **el lexema del EOF es `$`** (slide 26).
- Análisis exitoso → un mensaje adecuado y después `[SinErrores]`; el ejemplo de la slide 27 es
  `Compilacion Exitosa`.
- Invocación: la misma de la Etapa 1, `java -jar Compilador.jar <archivo-fuente>` (slide 28).

## Invariantes técnicas duras

- **Sin `System.exit()` en ningún punto del código de producción.** Los testers de la cátedra
  (`TesterDeCasosSinErrores` y `TesterDeCasosConErrores`, los del léxico y los del sintáctico)
  invocan `Main.main()` **in-process y repetidamente**, una vez por caso parametrizado, dentro del mismo proceso JVM. Un `System.exit()`
  mataría la corrida completa de tests a la primera invocación.
- **Sin estado estático mutable.** Por la misma razón: el proceso de test es compartido entre
  invocaciones sucesivas de `main()`.
- **Regla de línea del EOF**: `línea = cantidad de saltos de línea consumidos + 1`. Verificada contra
  los 3 casos de ejemplo de la cátedra: `lexSinErrores01.java` (21 `\n`, termina en newline → EOF en
  línea **22**), `lexSinErrores02.java` (12 `\n`, no termina en newline → EOF en línea **13**),
  `lexSinErrores03.java` (12 `\n`, termina en newline → EOF en línea **13**).
- **El carácter ofensivo va incluido en el lexema reportado del error.** Confirmado en Clase 02,
  slide 43: el código de ejemplo llama `actualizarLexema()` (que agrega el carácter actual al lexema)
  **inmediatamente antes** de `throw new ExcepcionLexica(lexema, nroLinea)`.
- `Main` vive en `minijava.aplicacion.Main` (ver [`arquitectura-del-analizador-lexico.md`](../lexico/arquitectura-del-analizador-lexico.md)). El FQN exacto
  **no** es un requisito de la cátedra: la guía de testers autoriza explícitamente editar el `import`
  y el tipo de la variable `init` en los testers para apuntar a cualquier clase con
  `public static void main` — se aprovechó esa libertad para evitar el nesting `main/java/main/`.
- El *working directory* de los tests debe ser la raíz del proyecto Maven (`compilador/`), porque los
  testers usan paths relativos duros. Los testers **de la cátedra** esperan `resources/sinErrores/`
  y `resources/conErrores/`, sin subcarpeta, así que **ahí vive el corpus de la etapa que se está
  entregando** y armar el zip no requiere copiar nada. El de la etapa anterior queda archivado en
  `resources/lexico/` (ver [`comandos-compilacion-y-entrega.md`](comandos-compilacion-y-entrega.md)).
- **El código de error `[Error:Lexema|NroLinea]` no lleva columna** — es el contrato exacto de la
  cátedra y el tester lo compara literal. La columna (Reporte de Error Elegante,
  [`especificacion-lexica-de-minijava.md`](../lexico/especificacion-lexica-de-minijava.md)) va sólo en el mensaje legible que antecede
  a ese código, nunca dentro de él.
