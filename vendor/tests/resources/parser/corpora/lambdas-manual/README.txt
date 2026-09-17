Casos de prueba escritos a mano para la etapa 2 (analizador sintactico), version "lambdas".
Separados del corpus de fuzzing (resources/parser/corpora/lambdas), que solo contiene casos
generados (fuzz_pos_*.java / fuzz_neg_*.java).

Formato (mismo que usan los testers de la catedra):

- sinErrores/*.java: programas validos. La salida del compilador tiene que
  CONTENER el string "[SinErrores]".

- conErrores/*.java: programas invalidos. La PRIMERA linea del archivo tiene
  el formato "///[Error:<lexema>|<linea>]". Le sacas el prefijo de 3 caracteres
  ("///") y lo que queda ("[Error:<lexema>|<linea>]") tiene que aparecer en la
  salida del compilador.

Se puede activar con ./mj corpus lambdas-manual, independientemente de la gramatica activa.