# Créditos del corpus de la Etapa 4

Los casos `salamanca*.java` de `sinErrores/` y `conErrores/` son de **Santiago Salamanca**,
compañero de la materia, que compartió su suite de la Etapa 4. Se importaron con el criterio de
`documentacion/compartido/estrategia-de-testing.md`, «Verificación cruzada contra la suite de un
compañero»:

- Se dejaron afuera sus casos 01 a 05, que son los mismos de la cátedra (están en
  `catedra/sentencias/`), y su `semIICorrecto56`: llama a `crear()` desde una clase que no lo
  declara, así que no es correcto.
- 106 entran tal cual.
- 20 entran con otro token en la primera línea. En 18, donde la cátedra no fija el token, se usa
  el que marca `javac` 25, verificado caso por caso. En `semIIError20` (`this = x;` con `x` sin
  declarar), el error es la `x`, porque la Clase 11 chequea los dos lados de la asignación antes
  que la asignabilidad. En la herencia circular (`semIIError61`), se mantiene el criterio de la
  Etapa 3. Ver
  `documentacion/sentencias/decisiones-de-deteccion-de-errores.md`.

El resto del contenido de cada archivo, incluida su descripción, es el original; sólo cambian los
saltos de línea, que pasan a LF.
