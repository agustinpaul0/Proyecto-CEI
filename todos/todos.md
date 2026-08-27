# Pendientes — Etapa 1

Sólo lo que falta verificar o implementar ahora mismo. Sin historial ni backlog — eso vive en
`git log` / GitHub.

## Implementar

- [x] `ConfiguracionDelCompilador` con flags para los 4 logros de comportamiento (floats, unicode,
  reporte elegante, multi-detección). `unicodeHabilitado` gatea sólo el escape `\uXXXX`, no los
  caracteres no-ASCII crudos — decisión documentada en `agents/especificacion-lexica.md`.
- [ ] Informe entregable en PDF: portada minimalista, paleta monocromática, índice. Debe incluir la
  ER de **todos** los tokens sin omitir ninguno (incluidos los símbolos de un solo carácter — ya
  están completos en `documentacion/especificacion-lexica.md`, falta trasladarlos al PDF), las
  decisiones ambiguas resueltas, y los logros perseguidos.
- [ ] **"Catálogo de casos de test" = el propio directorio de tests, no un documento aparte.** El
  usuario va a entregar todo el repositorio tal cual está en GitHub (código + `resources/sinErrores`
  + `resources/conErrores` + `resources/regresion`), a confirmar con el profesor antes. Lo que falta
  no es redactar un catálogo — es seguir reforzando la cantidad y variedad de casos de test.
- [ ] Confirmar con el profesor el formato de entrega final antes de la fecha límite (repositorio
  completo vs. sólo `.java` + informe PDF, como decía la consigna original).

## Verificar

- [ ] **Confirmar con el profesor el criterio de escapes mal formados en `litString`** (viernes
  28/08/2026). Hoy: al primer escape mal formado se reporta el error y se descarta el literal hasta
  su comilla de cierre, sin emitir token — indicación verbal del profesor, implementada y
  documentada en `documentacion/decisiones.md` §4.3. El criterio se unificó con el escape Unicode
  mal formado dentro de un `litString`, que antes no resincronizaba. Si el profesor lo rechaza, el
  procedimiento completo de vuelta atrás está en
  `documentacion/rollback-escapes-invalidos.md` — borrar ese archivo cuando el criterio quede
  confirmado.

- [ ] **Consultar al profesor si el rango de un `floatLiteral` debe validarse en el léxico.** Hoy no
  se valida: `3.5e38f` y `1e999` son `floatLiteral` válidos en MiniJava y en Java no compilan
  (`floating-point number too large`). La consigna pide "la misma estructura que los floats de
  Java", y en Java el chequeo de rango es semántico, no léxico — por eso se dejó afuera. Está
  documentado en `agents/especificacion-lexica.md` y fijado por
  `resources/sinErrores/floatFueraDelRangoDeJava.java`; si el profesor lo quiere en el léxico, es un
  chequeo puntual en los estados terminales de float.

- [ ] Seguir ampliando el corpus de `compilador/resources/{sinErrores,conErrores}/` a medida que
  aparezcan casos límite nuevos — no es un número cerrado, es continuo mientras dure la Etapa 1.
- [ ] Re-correr PIT (`mvn -Pmutacion org.pitest:pitest-maven:mutationCoverage`) después de cambios
  grandes al léxico para vigilar que el score de mutación (92% al momento de escribir esto, ver
  `agents/testing.md`) no baje. No hace falta perseguir el 100%: varios sobrevivientes son mutantes
  equivalentes o bordes de I/O real deliberadamente sin testear (documentado en `agents/testing.md`).
