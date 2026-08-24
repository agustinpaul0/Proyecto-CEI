# Pendientes — Etapa 1

Sólo lo que falta verificar o implementar ahora mismo. Sin historial ni backlog — eso vive en
`git log` / GitHub.

## Implementar

- [ ] `ConfiguracionDelCompilador` con flags para los 4 logros de comportamiento (floats, unicode,
  reporte elegante, multi-detección) — hoy los cuatro están activos de forma incondicional, sin
  poder testear la combinación "logro desactivado".
- [ ] Informe entregable en PDF: portada minimalista, paleta monocromática, índice. Debe incluir la
  ER de **todos** los tokens sin omitir ninguno (incluidos los símbolos de un solo carácter — ya
  están completos en `documentacion/arquitectura.md` §6, falta trasladarlos al PDF), las decisiones
  ambiguas resueltas, y los logros perseguidos.
- [ ] Catálogo de casos de test (documento aparte que exige la consigna, junto al informe).
- [ ] Empaquetar la entrega final: sólo archivos `.java` + catálogo + informe PDF, según especifica
  la consigna.

## Verificar

- [ ] Correr `mvn -Pmutacion test` (PIT) al menos una vez y revisar el score de mutación; decidir
  qué mutantes sobrevivientes ameritan un test nuevo. Nota: el goal de PIT no está bindeado a una
  fase del profile `mutacion`, hay que invocarlo explícito:
  `mvn -Pmutacion org.pitest:pitest-maven:mutationCoverage`.
- [ ] Seguir ampliando el corpus de `compilador/resources/{sinErrores,conErrores}/` a medida que
  aparezcan casos límite nuevos — no es un número cerrado, es continuo mientras dure la Etapa 1.
