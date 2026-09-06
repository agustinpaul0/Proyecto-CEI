# Decisiones de diseño del Analizador Léxico

Por qué el compilador está construido como está: la técnica de implementación que
exige la cátedra, las decisiones estructurales y las de proceso. Cada una con su
fuente, la alternativa descartada y su justificación.

> Estas decisiones son de la **Etapa 1 (Analizador Léxico)**. Las de la Etapa 2 están en
> [`../sintactico/`](../sintactico/README.md).

## 1. Las dos reglas que gobiernan todo

**Regla 1 — Lo que la cátedra especifica se respeta al pie de la letra.** El formato de salida, el
código de error, la técnica de implementación y las expresiones regulares salen de
`Aspectos Lexicos MiniJava 2026.pdf`, `Pautas Etapa 1.pdf` y las clases 01 y 02. Cuando este
proyecto tiene una opinión distinta, gana la cátedra.

**Regla 2 — Lo que la cátedra no especifica se resuelve como en Java.** Está escrita explícitamente
por la cátedra, en la última línea del README de la Etapa 1: *"todo lo que NO esté especificado para
MiniJava tiene comportamiento por defecto igual al de Java"*.

La segunda regla tiene un procedimiento asociado, y es tan importante como la regla misma:

> **"Como en Java" nunca se decide de memoria.** Se verifica contra la especificación del lenguaje
> (JLS) o **ejecutando `javac`** sobre el caso concreto, y el resultado de esa ejecución queda
> registrado en la documentación y fijado en un test.

La razón es práctica: el comportamiento de Java en los bordes (escapes, literales octales, límites
numéricos, escapes Unicode) presenta casos no evidentes, y una suposición razonable resulta equivocada con
frecuencia. Varias de las decisiones de este documento **contradicen** lo que uno esperaría, y sólo
se descubrió al ejecutar el compilador de referencia.


## 2. Decisiones sobre la técnica de implementación

### 2.1. Método por Estado, implementado como patrón State

**Fuente**: cátedra (Clase 02). Presenta tres estrategias —Método por Estado, Switch/Case, Tabla de
transiciones— y fija la primera.

**Decisión**: un componente de software por cada estado del autómata, implementado como el patrón de
diseño State (GoF): una clase por estado, con un único método `procesar(contexto)`.

**Descartado**: las otras dos estrategias, y cualquier generador automático (JFlex, ANTLR, lex/flex),
que la consigna prohíbe expresamente.

### 2.2. Driver iterativo en vez de encadenamiento recursivo

**Fuente**: criterio propio. Es la **única** desviación respecto de lo que muestran las slides.

**El problema**: la cátedra encadena los estados con llamadas recursivas (`return e2()` dentro de
`e1()`). Cada carácter del lexema apila un marco de pila de la JVM, así que un comentario o un string
de decenas de miles de caracteres puede agotar la pila con `StackOverflowError`. Eso no es un error
léxico: es una caída del compilador ante una entrada legal.

**Decisión**: un driver central hace un bucle `while` y consulta al estado actual en cada vuelta. Se
conserva exactamente la estructura de "un componente por estado" que exige la técnica; sólo cambia el
mecanismo por el que se encadenan.

**Costo**: hay que materializar el resultado de cada transición como un objeto (`Continuar` o
`Emitir`) en vez de devolverlo por la pila. Son objetos inmutables de un solo uso, y el costo es
despreciable frente a la garantía de no caerse.

### 2.3. `ResultadoDeTransicion` es una `sealed interface`

**Fuente**: criterio propio.

**Decisión**: el tipo que devuelve cada estado es una interfaz sellada con exactamente dos
implementaciones permitidas (`Continuar`, `Emitir`). Eso permite que el driver use un `switch`
**exhaustivo sin rama `default`**: el compilador verifica que estén cubiertos todos los casos
posibles.

**Descartado**: comprobar con `instanceof` y castear el otro caso a mano. Funciona, pero si mañana
apareciera un tercer tipo de resultado el código seguiría compilando y fallaría en tiempo de
ejecución con un `ClassCastException`. Con el `switch` sellado, directamente no compila hasta cubrir
el caso nuevo.

### 2.4. Los 49 tokens de lexema fijo se nombran con una tabla, no con estados

**Fuente**: cátedra (Clase 02, slides 62-63), que recomienda explícitamente esta técnica para las
palabras clave. Acá se generaliza al resto de los tokens que están en la misma situación.

**El punto de partida**: de los 56 tokens, **49 tienen lexema fijo** —las 21 palabras clave, los 10
símbolos de puntuación, los 17 operadores y `EOF`—, en el sentido de que el tipo y el lexema se
determinan mutuamente. Los otros 7 (`idClase`, `idGen`, `idMV` y los cuatro literales) leen su
lexema del fuente.

**Decisión**: una única tabla, `TablaDeLexemas`, con las dos direcciones — lexema → tipo para emitir,
tipo → lexema para el `$` del `EOF`, que no se lee del fuente. Ningún estado del autómata lleva un
`TipoDeToken` encima: delimita el lexema y consulta la tabla. Las palabras clave son un subconjunto
de esa tabla y siguen funcionando igual que siempre: se reconoce un identificador común y, antes de
emitir, se consulta.

**Lo que la tabla no puede hacer, y por eso el autómata sigue haciendo falta**: el *maximal munch*.
La tabla no sabe si un `>` es `op_mayor` o el principio de un `op_mayorIgual`; eso lo decide el
autómata mirando el carácter siguiente, y recién cuando terminó de consumir consulta. El reparto es
**el autómata delimita, la tabla nombra**.

**Qué se gana**: la correspondencia tipo ↔ lexema deja de estar repetida en seis lugares. Como
efecto concreto, `EstadoOperadorConSufijoOpcional` pasa de seis instancias a tres —lo único que
distinguía a `>` de `<` era el tipo de token— y `EstadoOperadorDoble` de dos a una. Y habilita dos
tests que antes no eran expresables: que ningún tipo de token quede sin entrada en la tabla, y que
cada uno de los 49 lexemas, suelto en un fuente, produzca exactamente el tipo que la tabla promete.

**Descartado**: un autómata dedicado por palabra clave. Requeriría casi un estado por letra de cada
palabra, fusionados con los estados de identificador — decenas de estados frágiles para algo que una
tabla resuelve en tiempo constante.


## 5. Decisiones de arquitectura

Desarrolladas en [`arquitectura.md`](arquitectura-del-analizador-lexico.md) §5; resumen con su justificación:

| Decisión | Por qué |
|---|---|
| `Salida` es una interfaz, no `System.out` directo | Permite testear el Módulo Principal completo con `assertEquals` contra una lista de líneas, sin capturar el stream global de la JVM. Es la decisión de mayor impacto en testeabilidad del proyecto. |
| Todas las dependencias por constructor | Cada pieza se sustituye en tests sin frameworks de mocking. |
| Los cuatro logros de comportamiento detrás de flags | Sin interruptor, el comportamiento base que pide la consigna quedaría sepultado y sería imposible demostrar que también se cumple. |
| Sin `System.exit()` ni estado estático mutable | Los testers de la cátedra invocan `main()` in-process y repetidamente: un `exit` mataría la corrida y un estático mutable contaminaría un caso con el anterior. |
| Un estado parametrizado para los seis operadores con sufijo | Seis clases idénticas salvo dos caracteres no aportan nada. |
| Puntuación de un carácter con un `Map`, no una clase por símbolo | Ídem: diez clases triviales contra una tabla. |
| `Token` es un `record` inmutable | Un token no cambia después de reconocido; hacerlo inmutable elimina toda una clase de errores. |
| `TipoDeToken` es un `enum`, no un `String` | El compilador mantiene los casos sincronizados en vez de confiar en la memoria de quien edita. |
| Las clases de la cátedra quedan intactas | Sólo se les cambió el `package`. El reemplazo es una clase nueva; las originales quedan como referencia de lo recibido. |


## 6. Decisiones de proceso

### 6.1. Los tests importan más que el código de producción

Es una regla explícita del proyecto, no una aspiración. Todo código nuevo lleva tests; todo defecto
encontrado se convierte en un test de regresión permanente que **nunca** se borra ni se renumera.
Un error léxico ante un programa inválido es comportamiento esperado; una excepción no controlada
—cualquier cosa que no sea `ExcepcionLexica`— es siempre un defecto y hace fallar el test.

Detalle completo en [`testing.md`](../compartido/estrategia-de-testing.md).

### 6.2. Nunca se ajusta un test para acomodar una implementación equivocada

Si un test falla, se arregla el código. Si la especificación cambió legítimamente, se actualiza el
test **y se documenta el motivo**. Las expectativas de columna que cambiaron al adoptar el criterio
de `javac` (§4.4) son un ejemplo: la expectativa vieja era la incorrecta, y quedó dicho por qué.

### 6.3. El material de la cátedra se lee como imágenes, no con extracción de texto

Las slides tienen diagramas de autómatas, capturas de código con globos de explicación y anotaciones
superpuestas. Extraer sólo el texto pierde la mitad de la información técnica — por ejemplo, el
ejemplo completo de implementación del analizador de la Clase 02 es un diagrama.

