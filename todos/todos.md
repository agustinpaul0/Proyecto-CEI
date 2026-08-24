# Pendientes — Etapa 1

Sólo lo que falta verificar o implementar ahora mismo. Sin historial ni backlog — eso vive en
`git log` / GitHub.

## Implementar

- [ ] `ConfiguracionDelCompilador` con flags para los 4 logros de comportamiento (floats, unicode,
  reporte elegante, multi-detección) — hoy los cuatro están activos de forma incondicional, sin
  poder testear la combinación "logro desactivado".
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

- [ ] Seguir ampliando el corpus de `compilador/resources/{sinErrores,conErrores}/` a medida que
  aparezcan casos límite nuevos — no es un número cerrado, es continuo mientras dure la Etapa 1.
- [ ] Re-correr PIT (`mvn -Pmutacion org.pitest:pitest-maven:mutationCoverage`) después de cambios
  grandes al léxico para vigilar que el score de mutación (92% al momento de escribir esto, ver
  `agents/testing.md`) no baje. No hace falta perseguir el 100%: varios sobrevivientes son mutantes
  equivalentes o bordes de I/O real deliberadamente sin testear (documentado en `agents/testing.md`).
