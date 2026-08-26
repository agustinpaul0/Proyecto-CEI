# El Manejador de Archivo — por qué el provisto no alcanza y qué hace el reemplazo

Documento autocontenido. Cubre la pieza de más bajo nivel del compilador: la que convierte un archivo
en el suministro de caracteres que consume el Analizador Léxico. Es también uno de los 8 logros de la
Etapa 1 (*Manejador de Archivos Eficiente*) y, como se explica acá, en la práctica no es opcional.

## 1. El contrato provisto por la cátedra

La cátedra entrega una interfaz, `SourceManager`, y una implementación de referencia,
`SourceManagerImpl`. La interfaz es mínima:

```java
public interface SourceManager {
    void open(String filePath) throws FileNotFoundException;
    void close() throws IOException;
    char getNextChar() throws IOException;
    int getLineNumber();
    char END_OF_FILE = (char) 26;
}
```

El modelo es simple y correcto: el léxico pide caracteres de a uno, y cuando el archivo se termina
recibe un **centinela** — el carácter ASCII 26 — que le indica el fin del contenido.

Las dos clases están en el proyecto con su **contenido original intacto**: lo único que se les cambió
es la declaración de `package`, para agruparlas junto a la interfaz y la implementación propias. No
se corrigieron ni se reescribieron; quedan como referencia de lo que se recibió.

## 2. Por qué la implementación de referencia no sirve

`SourceManagerImpl` lee el archivo **línea por línea** con `BufferedReader.readLine()` y va sirviendo
los caracteres de la línea en curso. Cuando la línea se agota, decide qué hacer consultando
`reader.ready()`:

```java
if (lineIndexNumber < currentLine.length()) {
    currentChar = currentLine.charAt(lineIndexNumber);   // carácter de la línea actual
    lineIndexNumber++;
} else if (reader.ready()) {
    currentChar = '\n';                                   // hay más: devuelve el salto de línea
    mustReadNextLine = true;
} else {
    currentChar = END_OF_FILE;                            // no hay más: fin de archivo
}
```

Ese diseño tiene tres defectos, y el primero **hace fallar los propios casos de ejemplo de la
cátedra**.

### 2.1. El último salto de línea se pierde: el EOF queda en la línea equivocada

`reader.ready()` responde "¿hay bytes disponibles para leer sin bloquear?". Cuando el archivo termina
**con** un salto de línea, después de consumir el contenido de la última línea ya no queda nada por
leer: `ready()` devuelve `false` y el manejador entrega `END_OF_FILE` **sin haber entregado nunca ese
último `\n`**. Como el contador de líneas sólo avanza cuando se lee una línea nueva, el EOF se
reporta una línea antes de lo que corresponde.

Es exactamente lo que ocurre con `lexSinErrores01.java`, uno de los cinco casos de ejemplo que la
cátedra entrega junto a los testers:

| | |
|---|---|
| Saltos de línea en el archivo | 21 |
| Termina en salto de línea | sí |
| Oráculo esperado por el propio tester de la cátedra (`//#$,22`) | EOF en la línea **22** |
| Lo que reporta `SourceManagerImpl` | EOF en la línea **21** |

La regla correcta, verificada contra los tres casos de ejemplo, es
**`línea del EOF = cantidad de saltos de línea consumidos + 1`**:

| Caso | Saltos | Termina en `\n` | EOF esperado |
|---|---|---|---|
| `lexSinErrores01.java` | 21 | sí | 22 |
| `lexSinErrores02.java` | 12 | no | 13 |
| `lexSinErrores03.java` | 12 | sí | 13 |

### 2.2. Un archivo vacío lo hace explotar

Con un archivo de cero bytes, el primer `readLine()` devuelve `null`, `currentLine` queda en `null`,
y el `currentLine.length()` de la línea siguiente lanza `NullPointerException`. Un archivo vacío es
un fuente MiniJava perfectamente legal —debe producir un único token `EOF`— y no una caída del
compilador.

### 2.3. Lee de a líneas, que es justamente lo que el logro pide evitar

Pautas Etapa 1 describe el logro así: *"Se debe implementar otro manejador de archivos que administre
de forma más eficiente el fuente. En particular, en vez de leer de a líneas del fuente debe de a
caracteres. Para esto debe valerse sólo de las librerías de manejo IO de Java. El nuevo manejador
debe conformar con la interface `SourceManager`*"*, con la nota al pie: *"puede que por algún otro
logro sea necesario expandir la interface provista"*.

### Conclusión

Los defectos 2.1 y 2.2 no son cuestiones de eficiencia: son **fallas de corrección** contra los casos
de ejemplo de la propia cátedra. Por eso, aunque el logro figure como opcional, implementar el
manejador propio es un **requisito** para que el compilador pase los tests provistos.

## 3. La extensión de la interfaz

El *Reporte de Error Elegante* necesita dos datos que `SourceManager` no expone: en qué **columna**
está el carácter que se está mirando, y el **texto completo de la línea** donde ocurrió el error
(para poder imprimirla y dibujar el `^` debajo). La consigna autoriza explícitamente esta extensión.

```java
public interface ManejadorDeArchivo extends SourceManager {
    boolean estaEnFinDeArchivo();
    int getColumnNumber();
    String getCurrentLineText();
}
```

`estaEnFinDeArchivo()` merece su propia explicación, porque nace de un bug real y sutil.

### Por qué no alcanza con comparar contra el centinela

El centinela `END_OF_FILE` es el carácter ASCII 26 (`0x1A`, *substitute*). Pero `0x1A` es también un
byte perfectamente legítimo **dentro** de un archivo. Un fuente que contenga un `0x1A` embebido
—cosa que ocurre, por ejemplo, con un archivo binario pasado por error, o con un fuente guardado por
una herramienta vieja de DOS— hacía que el léxico creyera que el archivo terminó ahí, y **truncara
el análisis en silencio**: sin error, sin aviso, simplemente ignorando el resto del archivo.

`estaEnFinDeArchivo()` responde **por posición, no por valor**: es `true` sólo si el último
`getNextChar()` se pasó del final real del contenido. Con eso, un `0x1A` embebido llega al léxico
como cualquier otro carácter y se reporta como el error léxico que corresponde
(`no es un símbolo válido`), en vez de cortar el análisis.

## 4. Qué hace `ManejadorDeArchivoEficiente`

### 4.1. Lectura y normalización

En `open()`:

1. Lee el contenido completo del archivo como bytes y lo decodifica en UTF-8 con
   `new String(bytes, UTF_8)`.
2. Normaliza los tres convenios de fin de línea —`\r\n` (Windows), `\r` (Mac clásico) y `\n`
   (Unix)— a un único `\n`.
3. Guarda el resultado en un `char[]` y arranca los contadores.

**Por qué `new String(bytes, UTF_8)` y no `Files.readString()`**: `readString` lanza
`MalformedInputException` ante una secuencia UTF-8 inválida, y eso hacía que un archivo binario
reventara el compilador con una excepción no controlada. `new String(bytes, UTF_8)` reemplaza las
secuencias mal formadas por `U+FFFD`, así los bytes basura llegan al léxico como caracteres
desconocidos y se reportan como errores léxicos normales. La regla de fondo: **un fuente inválido
produce errores léxicos; nunca una caída del compilador**.

**Por qué normalizar los saltos de línea**: sin la normalización, el autómata tendría que contemplar
`\r`, `\n` y `\r\n` en cada estado que se interrumpe por un fin de línea (chars sin cerrar, strings
sin cerrar, comentarios de línea), y el conteo de líneas dependería del convenio con que se guardó el
archivo. Normalizando en la frontera, el léxico ve **un solo** tipo de salto y cuenta igual sin
importar de dónde venga el archivo.

### 4.2. El truco central: la posición del carácter *devuelto*, no del *próximo*

`getNextChar()` mantiene dos juegos de contadores en paralelo:

| Campo | Qué guarda |
|---|---|
| `columnaSiguiente`, `inicioDeLineaActual` | dónde está parado el cursor **para la próxima lectura** |
| `columnaDevuelta`, `inicioDeLineaDevuelta` | dónde estaba el carácter **que se acaba de devolver** |

`getColumnNumber()` y `getCurrentLineText()` responden con los **devueltos**. La razón: quien pregunta
es siempre el reporte de error, y lo que necesita saber es dónde está el carácter que tiene en la
mano, no dónde estará el siguiente.

El caso que obliga a esta separación —encontrado escribiendo el test antes que el código— es el
instante exacto en que se devuelve el `\n` que termina una línea. En ese momento el cursor ya avanzó
a la línea siguiente; si `getCurrentLineText()` respondiera con el cursor actual, el reporte de un
error ocurrido en ese `\n` mostraría **la línea siguiente**, con el `^` apuntando a texto que no tiene
nada que ver.

El contador de línea sigue el mismo criterio: se incrementa **después** de devolver el `\n`. Con eso,
los tres casos de EOF de §2.1 salen correctos sin ningún caso especial.

## 5. La decisión discutible: se carga el archivo entero en memoria

Hay que decirlo con todas las letras, porque es una desviación respecto de la lectura literal del
logro. `open()` hace **una sola** lectura del archivo completo (`Files.readAllBytes`) y lo guarda en
un `char[]`; recién a partir de ahí `getNextChar()` sirve carácter por carácter.

**A favor**:

- Cumple lo que el logro busca evitar: **no** hay ningún `readLine()`, y el léxico recibe el fuente
  carácter por carácter, que es la forma de acceso que la consigna pide.
- Es la variante más eficiente en cantidad de operaciones de entrada/salida: una sola lectura contra
  una por línea (o por bloque). Los archivos fuente de un trabajo práctico son de kilobytes.
- Hace triviales y exactos `getCurrentLineText()` y el reconteo de columnas, porque todo el contexto
  está disponible: recuperar la línea completa de un error es recorrer el array hasta el `\n`, sin
  buffers que ya se hayan descartado.
- `Files` es parte de las librerías de manejo de IO de Java, no una dependencia externa; la consigna
  prohíbe librerías de terceros, no un paquete estándar.

**En contra, y es honesto reconocerlo**: una lectura literal de *"en vez de leer de a líneas del
fuente debe de a caracteres"* podría interpretarse como que la lectura **física** debe ser
incremental (un buffer de bloques que se rellena a medida que se consume), y no una carga completa.
Con esta implementación, el consumo de memoria es proporcional al tamaño del fuente: un archivo de
cientos de megabytes lo haría fallar, cosa que un buffer de bloques resistiría.

**Cómo cambiarlo si hiciera falta**: el cambio está contenido en una sola clase y no toca al resto
del compilador. `ManejadorDeArchivo` es una interfaz, `AnalizadorLexico` la recibe por constructor, y
ningún estado del autómata sabe de dónde salen los caracteres. Sustituir la carga completa por un
buffer de bloques es reescribir `ManejadorDeArchivoEficiente` y nada más — con la única complicación
de que `getCurrentLineText()` tendría que retener la línea en curso aparte, porque el bloque que la
contenía puede haberse descartado.

## 6. Qué garantiza la suite sobre esta pieza

`ManejadorDeArchivoEficienteTest` (19 casos) cubre, entre otros:

- Los tres casos de EOF de §2.1, con los números de línea exactos de los oráculos de la cátedra.
- El archivo vacío (un solo `EOF`, sin excepción).
- Los tres convenios de salto de línea, verificando que el conteo de líneas coincide entre los tres.
- Un `0x1A` embebido: que se entregue como carácter común y **no** trunque el análisis.
- Bytes UTF-8 inválidos: que se decodifiquen a `U+FFFD` en vez de lanzar excepción.
- Que `getColumnNumber()` y `getCurrentLineText()` describan el carácter devuelto, incluido el caso
  del `\n` que cierra una línea.

## 7. Ver también

- [`arquitectura.md`](arquitectura.md) §2 y §7 — dónde encaja esta pieza en el conjunto.
- [`manejo-de-errores.md`](manejo-de-errores.md) — quién consume `getColumnNumber()` y
  `getCurrentLineText()`, y para qué.
- [`decisiones.md`](decisiones.md) — el catálogo completo de decisiones del proyecto.
