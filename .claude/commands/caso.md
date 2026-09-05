---
description: Agrega un caso al corpus de resources/ con sus expectativas
argument-hint: "[sinErrores|conErrores] [descripción del caso]"
---

Caso pedido: $ARGUMENTS

Agregá el caso a `compilador/resources/sinErrores/` o `compilador/resources/conErrores/`
siguiendo las convenciones de `compilador/herramientas/README.md`:

- **Predecí las expectativas a mano, antes de correr nada.** Si el caso después falla, lo
  primero a decidir es si el equivocado es el oráculo o el compilador — varios casos del corpus
  salieron justamente de haber predicho mal y haber ido a mirar por qué.
- Cuidá las dos cuentas fáciles de errar: la línea del token `EOF` (saltos de línea consumidos
  + 1, y depende de cuántas líneas de expectativa lleva el propio archivo) y el desplazamiento
  que la cabecera de comentarios le mete al fuente en los casos de `conErrores/`.
- Nombre del archivo en el mismo estilo camelCase descriptivo que el resto del corpus.
- Si el caso cubre una regla léxica discutible, referenciá en `guias/especificacion-lexica.md`
  la decisión que lo respalda; si no existe, seguí `guias/investigacion.md` primero.

Después corré `mvn test -Dvolcado=resumen` desde `compilador/` y confirmá que el caso pasa con
las expectativas que predijiste.
