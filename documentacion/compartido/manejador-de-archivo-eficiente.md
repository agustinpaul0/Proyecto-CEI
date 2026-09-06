# El Manejador de Archivos Eficiente — requisitos y diseño

Documento autocontenido. Cubre la pieza de más bajo nivel del compilador: la que convierte un archivo
en el suministro de caracteres que consume el Analizador Léxico. Es uno de los 8 logros de la
Etapa 1 (*Manejador de Archivos Eficiente*).

## 1. El contrato

La cátedra entrega la interfaz `SourceManager`, que fija la forma del contrato:

```java
public interface SourceManager {
    void open(String filePath) throws FileNotFoundException;
    void close() throws IOException;
    char getNextChar() throws IOException;
    int getLineNumber();
    char END_OF_FILE = (char) 26;
}
```

Y Pautas Etapa 1 describe el logro así:

> *"Se debe implementar otro manejador de archivos que administre de forma más eficiente el fuente.
> En particular, en vez de leer de a líneas del fuente debe de a caracteres. Para esto debe valerse
> sólo de las librerías de manejo IO de Java. El nuevo manejador debe conformar con la interface
> `SourceManager`"*, con la nota al pie *"puede que por algún otro logro sea necesario expandir la
> interface provista"*.

## 2. Los requisitos que tiene que cumplir

Además de servir caracteres, esta pieza es la **única** fuente de la información de posición que
aparece en todo reporte de error. De ahí salen cinco requisitos concretos, cada uno fijado por un
test.

### 2.1. La línea del EOF

**`línea del EOF = cantidad de saltos de línea consumidos + 1`.** Es la regla que hacen cumplir los
tres casos de ejemplo que la cátedra entrega junto a los testers, y el caso que la vuelve delicada es
el archivo que **termina** en salto de línea: ese último `\n` tiene que entregarse al léxico como
cualquier otro, porque si no el EOF queda una línea antes de lo que el oráculo espera.

| Caso | Saltos | Termina en `\n` | EOF esperado |
|---|---|---|---|
| `lexSinErrores01.java` | 21 | sí | 22 |
| `lexSinErrores02.java` | 12 | no | 13 |
| `lexSinErrores03.java` | 12 | sí | 13 |

### 2.2. Un archivo vacío es un fuente válido

Cero bytes es un fuente MiniJava perfectamente legal: debe producir un único token `EOF`, no una
caída del compilador. Vale la regla de fondo de todo el proyecto: **un fuente inválido produce
errores léxicos; nunca una excepción que escape.**

### 2.3. Los tres convenios de fin de línea cuentan igual

Aspectos Léxicos §3.1 lo pide explícitamente: LF, CR y CRLF son **cada uno un solo** salto de línea,
y *"el analizador léxico de MiniJava tiene que ser capaz de reconocer correctamente cualquiera de
estas convenciones y reportar números de línea equivalentes para archivos fuente que difieran
únicamente en la forma de representar sus saltos de línea"*.

### 2.4. El fin de archivo son dos condiciones, no una

El centinela `END_OF_FILE` es el ASCII 26 (`0x1A`, *substitute*), pero `0x1A` es también un byte
perfectamente legítimo **dentro** de un archivo — aparece, por ejemplo, en un binario pasado por
error o en un fuente guardado por una herramienta vieja de DOS. La interfaz provista transporta por
un mismo canal dos cosas de naturaleza distinta: los caracteres del fuente y la señal de que los
caracteres se terminaron. Recibir el centinela es entonces condición **necesaria pero no
suficiente**.

Si el fin de archivo se decidiera sólo con esa condición, un `0x1A` embebido truncaría el análisis
ahí **en silencio**: sin error, sin aviso, informando `[SinErrores]` sobre un fuente que nunca se
terminó de leer.

Por eso la interfaz se extiende con `estaEnFinDeArchivo()`, que aporta la segunda condición: es
`true` sólo si la última lectura se pasó del final real del contenido. El fin de archivo es la
**conjunción** de las dos:

```text
fin de archivo  ⇔  llegó el centinela  ∧  la posición confirma que se acabó el contenido
```

**Por qué en el código aparece un solo chequeo.** El manejador devuelve el centinela en una única
rama, que es exactamente la misma en la que prende la bandera. Es decir que la segunda condición
**implica** a la primera, y la conjunción se colapsa: escribir
`c == END_OF_FILE && estaEnFinDeArchivo()` da lo mismo que escribir `estaEnFinDeArchivo()` a secas.
El `AND` queda implícito, y en los estados sólo se testea el conjunto que discrimina.

La implicación no vale al revés, y ahí está todo el asunto: un `0x1A` embebido cumple la primera
condición y no la segunda. Como no hay ninguna rama que lo atrape, cae hasta el final de
`EstadoInicial` y se reporta como carácter desconocido, que es lo que corresponde.

### 2.5. Ubicar el error no puede costar una segunda pasada

El *Reporte de Error Elegante* necesita dos datos que `SourceManager` no expone: la **columna** del
carácter que se está mirando y el **texto completo de la línea** del error, para imprimirla y dibujar
el `^` debajo. La condición que puso la cátedra es que obtenerlos **no** obligue a releer el archivo.

```java
public interface ManejadorDeArchivo extends SourceManager {
    boolean estaEnFinDeArchivo();
    int getColumnNumber();
    String getCurrentLineText();
}
```

## 3. El diseño

### 3.1. Lectura y normalización

En `open()`:

1. Lee el contenido completo del archivo como bytes y lo decodifica en UTF-8 con
   `new String(bytes, UTF_8)`.
2. Normaliza los tres convenios de fin de línea a un único `\n` (requisito 2.3).
3. Guarda el resultado en un `char[]` y arranca los contadores.

**Por qué `new String(bytes, UTF_8)` y no `Files.readString()`**: `readString` lanza
`MalformedInputException` ante una secuencia UTF-8 inválida, con lo que un archivo binario reventaría
el compilador con una excepción no controlada. `new String(bytes, UTF_8)` reemplaza las secuencias
mal formadas por `U+FFFD`, así los bytes residuales llegan al léxico como caracteres desconocidos y se
reportan como errores léxicos normales (requisito 2.2).

**Por qué normalizar los saltos de línea en la frontera**: sin la normalización, el autómata tendría
que contemplar `\r`, `\n` y `\r\n` en cada estado que se interrumpe por un fin de línea —chars sin
cerrar, strings sin cerrar, comentarios de línea— y el conteo de líneas dependería del convenio con
que se guardó el archivo. Normalizando acá, el léxico ve **un solo** tipo de salto y cuenta igual sin
importar de dónde venga el archivo.

### 3.2. La posición del carácter *devuelto*, no la del *próximo*

`getNextChar()` mantiene dos juegos de contadores en paralelo:

| Campo | Qué guarda |
|---|---|
| `columnaSiguiente`, `inicioDeLineaActual` | dónde está parado el cursor **para la próxima lectura** |
| `columnaDevuelta`, `inicioDeLineaDevuelta` | dónde estaba el carácter **que se acaba de devolver** |

`getLineNumber()`, `getColumnNumber()` y `getCurrentLineText()` responden los tres con los
**devueltos**. La razón: quien pregunta es siempre el reporte de error, y lo que necesita saber es
dónde está el carácter que tiene en la mano, no dónde estará el siguiente.

El caso que obliga a esta separación —encontrado escribiendo el test antes que el código— es el
instante exacto en que se devuelve el `\n` que termina una línea. En ese momento el cursor ya avanzó
a la línea siguiente; si `getCurrentLineText()` respondiera con el cursor actual, el reporte de un
error ocurrido en ese `\n` mostraría **la línea siguiente**, con el `^` apuntando a texto que no
tiene nada que ver.

Los tres datos se fotografían juntos al entrar a `getNextChar()`, antes de tocar los contadores
internos, así que mientras el `\n` es el último devuelto la línea sigue siendo la suya. Con eso los
tres casos de EOF de §2.1 salen correctos sin ningún caso especial, y el analizador no necesita
llevar su propio contador de líneas en paralelo.

### 3.3. Por qué ubicar el error es barato (requisito 2.5)

Los dos accesores que usa el reporte de error no hacen ninguna búsqueda hacia atrás:

| Operación | Costo | Cómo |
|---|---|---|
| `getColumnNumber()` | **O(1)** | es un contador que `getNextChar()` incrementa, y que se reinicia a 1 en cada `\n` |
| `getCurrentLineText()` | **O(largo de la línea)** | `inicioDeLineaDevuelta` es otro contador incremental; sólo hay que recorrer desde ahí hasta el `\n` siguiente |

No hay una segunda pasada sobre el archivo en ningún punto, ni un buffer de las últimas *N* líneas
que haya que mantener: la información de posición se construye **mientras** se lee, con dos enteros.
Y el recorrido de `getCurrentLineText()` está acotado por el largo de la línea, que es exactamente el
texto que el reporte va a imprimir de todos modos.

`ManejadorDeArchivoEficienteTest` fija esa propiedad donde más se notaría: un fuente de 200.000
líneas con el error en la última, y una línea suelta de un millón de caracteres.

## 4. La decisión discutible: se carga el archivo entero en memoria

Hay que decirlo con todas las letras, porque es una desviación respecto de la lectura más literal del
logro. `open()` hace **una sola** lectura del archivo completo (`Files.readAllBytes`) y lo guarda en
un `char[]`; recién a partir de ahí `getNextChar()` sirve carácter por carácter.

**A favor**:

- Cumple lo que el logro busca: **no** hay ningún `readLine()`, y el léxico recibe el fuente carácter
  por carácter, que es la forma de acceso que la consigna pide.
- Es la variante más eficiente en cantidad de operaciones de entrada/salida: una sola lectura contra
  una por línea o por bloque. Los archivos fuente de un trabajo práctico son de kilobytes.
- Hace exactos y baratos `getCurrentLineText()` y el conteo de columnas (§3.3), porque todo el
  contexto sigue disponible: recuperar la línea de un error es recorrer el array hasta el `\n`, sin
  buffers que ya se hayan descartado.
- `Files` es parte de las librerías de manejo de IO de Java, no una dependencia externa; la consigna
  prohíbe librerías de terceros, no un paquete estándar.

**En contra, y es honesto reconocerlo**: una lectura literal de *"en vez de leer de a líneas del
fuente debe de a caracteres"* podría entenderse como que la lectura **física** debe ser incremental
—un buffer de bloques que se rellena a medida que se consume— y no una carga completa. Con esta
implementación el consumo de memoria es proporcional al tamaño del fuente: un archivo de cientos de
megabytes lo haría fallar, cosa que un buffer de bloques resistiría.

**Cómo cambiarlo si hiciera falta**: el cambio está contenido en una sola clase y no toca al resto
del compilador. `ManejadorDeArchivo` es una interfaz, `AnalizadorLexico` la recibe por constructor, y
ningún estado del autómata sabe de dónde salen los caracteres. Sustituir la carga completa por un
buffer de bloques es reescribir `ManejadorDeArchivoEficiente` y nada más — con la única complicación
de que `getCurrentLineText()` tendría que retener la línea en curso aparte, porque el bloque que la
contenía puede haberse descartado.

## 5. Qué garantiza la suite sobre esta pieza

`ManejadorDeArchivoEficienteTest` (26 casos) cubre, entre otros:

- Los tres casos de EOF de §2.1, con los números de línea exactos de los oráculos de la cátedra.
- El archivo vacío: un solo `EOF`, sin excepción.
- Los tres convenios de salto de línea, verificando que la numeración coincide entre los tres y que
  nunca llega un `\r` crudo al léxico.
- Un `0x1A` embebido: que se entregue como carácter común y **no** trunque el análisis.
- Bytes UTF-8 inválidos: que se decodifiquen a `U+FFFD` en vez de lanzar excepción.
- Que `getLineNumber()`, `getColumnNumber()` y `getCurrentLineText()` describan siempre el mismo
  carácter —el devuelto—, incluido el caso del `\n` que cierra una línea.
- El costo de ubicar el error: 200.000 líneas con el error en la última, y una línea de un millón de
  caracteres.

## 6. Ver también

- [`arquitectura.md`](../lexico/arquitectura-del-analizador-lexico.md) §2 y §7 — dónde encaja esta pieza en el conjunto.
- [`manejo-de-errores.md`](manejo-de-errores.md) — quién consume `getColumnNumber()` y
  `getCurrentLineText()`, y para qué.
- [`decisiones.md`](../lexico/decisiones-de-logica.md) — el catálogo completo de decisiones del proyecto.
