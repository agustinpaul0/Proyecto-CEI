# Decisiones de diseño

Las recomendaciones del profesor para esta etapa, y cómo quedan en el código.

## 1. La tabla de símbolos es estática, sin singleton

Es la recomendación del profesor y lo que piden las Pautas: la tabla es global, visible para todos
los módulos (slides 6, 13 y 17). `TablaDeSimbolos` tiene campos y métodos `static` y no se
instancia.

Los testers de la cátedra llaman a `main()` muchas veces en la misma JVM, así que un estado estático
que sobreviviera de una compilación a otra mezclaría los programas. Por eso la tabla es **el único
estado estático** del compilador, y `inicializar()` la deja limpia al empezar cada compilación. La
llaman los dos módulos principales que parsean. Un inicializador estático la deja cargada desde que
se usa la clase, así que los tests que construyen el parser directamente también la encuentran
válida.

## 2. Los repetidos se detectan al parsear y se lanzan al chequear

El documento §8 pone los nombres repetidos en la primera pasada, la que construye la tabla. Cada
`agregar…` controla su tabla y, ante un repetido, no lo inserta: lo **registra**. El primero
registrado se lanza al empezar `chequearDeclaraciones()`, antes que cualquier otro error.

La alternativa era lanzarlo desde la acción, en medio del parseo. Se descartó por dos razones:
- el semántico sólo corre si el sintáctico terminó sin errores (Pautas slide 20), y un error lanzado
  a mitad del parseo taparía un error sintáctico posterior;
- el parser recupera en Modo Pánico, y una excepción de otra clase atravesaría esa recuperación.

Para un programa sintácticamente correcto, las dos opciones dan la misma salida.

## 3. Retornar lo chico, delegar lo complejo

El profesor recomienda retornar cuando el resultado es simple, y delegar cuando no, accediendo en
forma global a la entidad actual en lugar de pasarla por parámetro:
- **se retorna**: el `Tipo` de `tipo()`, la cantidad de `dimensionesOpcionales()`, el parámetro de
  `genericidadOpcional()`, el argumento de `tipoGenericoOpcional()` y el miembro que arma
  `miembroSinModificadores()`;
- **se delega**: nadie devuelve una lista. Cada miembro se agrega a `TS.claseActual()` y cada
  parámetro a `TS.unidadActual()`. La clase y la interfaz se crean apenas se lee su nombre, y el
  resto de su información se les agrega a medida que aparece.

## 4. Los únicos atributos de entrada

La recomendación es no usar atributos de entrada, y la EDT tiene tres, todos en el mismo lugar:
`MiembroDesdeIdClase.idClase`, `AtributoOMetodo.tipo` y `AtributoOMetodo.nombre`.

La factorización de la Etapa 2 dejó el tipo y el nombre de un miembro **antes** del punto en que se
sabe si es un atributo, un método o un constructor. Sin esos parámetros, habría dos salidas y las
dos son peores:
- guardarlos en la tabla como estado global, que es un atributo de entrada disfrazado;
- crear el miembro sin nombre y completarlo después. En ese caso el método no tendría nombre
  mientras recibe sus parámetros, y el error de un parámetro repetido no podría nombrarlo.

## 5. Una jerarquía de tipos sellada

`Tipo` es una interfaz sellada con cinco variantes. `Unidad` y `ClaseOInterfaz` son clases
selladas, y `Miembro` es una interfaz sellada. Así, cada `switch` sobre ellas es exhaustivo y lo
controla el compilador de Java.

## 6. Tablas propias y tablas consolidadas

Cada tipo guarda sus métodos propios y, aparte, la tabla consolidada, que suma los heredados. Hasta
consolidar son la misma tabla. La tabla propia hace falta después de consolidar para dos cosas:
- decidir si un método que implementa mal una interfaz es propio, y entonces el error va en su
  nombre, o heredado, y entonces va en el de la clase;
- saber qué comparar contra lo heredado.
