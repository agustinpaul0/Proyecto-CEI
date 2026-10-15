# Prompt para la auditoría de la Etapa 4

Copiá lo que sigue a otro agente, abierto en la raíz del repositorio.

---

Sos un auditor. Revisá la implementación de la **Etapa 4 (chequeo de sentencias)** de este
compilador de MiniJava y su suite de tests. **No modifiques ningún archivo.** Tu entregable es un
informe en markdown.

## Contexto que tenés que leer primero

1. `AGENTS.md` y `documentacion/proyecto/README.md`: las reglas del proyecto. Hay dos que no se
   discuten: donde la cátedra no especifica, se hace lo que hace Java, verificado con `javac` y no
   de memoria; y donde la cátedra fija algo, eso manda aunque Java haga otra cosa.
2. `documentacion/minijava/MiniJava-Semantica-Completa-2026.pdf`, «el documento». Leelo **como
   imágenes, página por página**, no con extracción de texto. Es la especificación.
3. `etapas/etapa-4/Pautas Etapa 4.pdf` y las Clases 09, 10 y 11 de `etapas/etapa-4/`, también como
   imágenes.
4. `documentacion/sentencias/` entero y `CONVENCIONES-CATEDRA.md`.
5. `etapas/etapa-4/revision/guia-de-revision.md`: el mapa de los archivos.

## Qué auditar

**A. El código contra el documento, sección por sección (§3 a §14).** Para cada regla del
documento, ubicá el código que la implementa y al menos un caso del corpus que la prueba. Reportá:
- reglas sin implementar, o implementadas distinto de como dice el documento;
- reglas sin un caso que las ejercite, tanto el caso correcto como el erróneo;
- controles del código que el documento no pide.

**B. Los tokens de error.** Para cada fila de
`documentacion/sentencias/decisiones-de-deteccion-de-errores.md`, armá un fuente mínimo, compilalo
con `javac` 25 (está en el PATH) y con `java -jar compilador/target/Compilador.jar` (antes corré
`mvn -q package -DskipTests` en `compilador/`), y compará el token. Reportá cada diferencia que no
esté justificada por un caso de la cátedra o por una decisión documentada.

**C. Casos borde que falten.** Buscá activamente programas que el compilador acepte y deba
rechazar, o al revés, y programas que lo hagan caer (cualquier excepción es un defecto). Pistas:
- genéricos con herencia de tres niveles, con y sin argumento, y la `T` conservada de un ancestro;
- `this`, atributos y métodos implícitos en métodos estáticos, también dentro de paréntesis y
  argumentos;
- encadenados largos que mezclan llamadas, atributos, índices y creaciones;
- paréntesis como base de un encadenado, en asignaciones y en sentencias de llamada;
- `length` de arreglos multidimensionales, filas como destino, arreglos de `T` y de genéricos;
- locales en bloques anidados y hermanos, y su relación con parámetros y atributos;
- `return` en constructores, en métodos `void`, con valores `void` o `null`;
- interfaces como receptor, como tipo base de arreglos, y con métodos heredados;
- las predefinidas `Object`, `String` y `System`, y su herencia;
- programas sin errores muy grandes.

Para cada hallazgo, dá el fuente mínimo, la salida actual y la esperada con su justificación
(sección del documento, o `javac` con su salida real).

**D. La suite.** Corré `cd compilador && mvn verify` y reportá el resultado y la cobertura
(`target/site/jacoco/index.html`). Revisá:
- si cada clase de `minijava.semantico.ast` tiene tests que ejerciten cada rama;
- si el modelo de `herramientas/sentencias/generar_corpus_sentencias.py` coincide con el documento
  §9 y §11.2;
- si las propiedades de `SentenciasPropiedadesTest` prueban algo no trivial;
- si algún caso del corpus tiene una expectativa equivocada.

**E. La simplicidad.** El usuario pidió código simple, sin sobreingeniería, y legible en una
revisión manual. Señalá lo que se podría simplificar sin perder claridad, y los comentarios que
sobren o que narren historia («ahora», «ya no»). El proyecto pide cero comentarios salvo los que
expliquen algo que el código no puede decir.

## Formato del informe

Una sección por letra (A a E). Cada hallazgo lleva su severidad (crítico: cambia la nota; mayor:
puede fallar un caso de la cátedra; menor: estilo o documentación), su ubicación (`archivo:línea`)
y la evidencia. Al final, una lista corta con lo más importante para arreglar primero. Si no
encontrás nada en una sección, decilo y explicá qué revisaste.
