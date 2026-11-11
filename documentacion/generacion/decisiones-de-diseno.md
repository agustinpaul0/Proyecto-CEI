# Decisiones de diseño

Cómo quedan en el código las recomendaciones de la cátedra para esta etapa, y las alternativas que
se evaluaron. El criterio general es la simplicidad: la eficiencia no es un objetivo.

## 1. Una pasada separada después del chequeo

La Clase 16, slides 3-4, ofrece dos formas: generar **intercalado** con el chequeo de sentencias, o
en **una pasada separada** al terminar el análisis semántico. Se usa la separada, la que la slide 4
llama «la más recomendada». Cada nodo tiene un `generar` aparte de su `chequear`, que corre sólo si
todo el análisis terminó sin errores.

**Alternativa descartada:** el intercalado. Obliga a cuidar el orden de cada `chequear`, porque
generar código y delegar el chequeo en los hijos quedan mezclados (slide 3), y generaría
instrucciones para un programa que después resulta tener un error.

## 2. El análisis semántico deja todo resuelto

Clase 16, slides 5, 17 y 36. El chequeo es el que sabe a qué entidad corresponde cada nombre, así
que es el que guarda lo que la generación necesita:

- `NodoVariable` guarda una `Ubicacion`: en el registro de activación, con su desplazamiento, o en
  el CIR de `this`.
- Las llamadas guardan el método elegido y su desplazamiento en la VT del tipo estático del receptor.
- `NodoCreacionObjeto` guarda la clase y el constructor; `NodoAccesoAtributo`, el desplazamiento.
- `NodoBloque` guarda cuántas locales declara, y `NodoReturn`, cuántas hay vivas y su unidad.

Los desplazamientos de atributos y métodos se calculan al consolidar, después de los del ancestro
(slide 37), y los de las locales, cuando el `Contexto` las declara.

## 3. El lado izquierdo de una asignación, en un método aparte

La Clase 16, slides 22-29, le da a `NodoVar` una marca `esLadoIzqAsig`: según la marca, genera una
carga o un almacenamiento. Acá son dos métodos: `generar` para cargar el valor y
`generarAlmacenamiento` para guardarlo. Es el mismo pseudocódigo de la slide 29 partido en dos.

**Por qué:** la marca es un estado que alguien tiene que acordarse de poner antes de generar. Con
dos métodos, la asignación llama al que corresponde, y las expresiones que no son asignables no
tienen ese método. Lo que en la slide decide si «cadena es no nulo» lo resuelve que cada acceso
guarde su receptor ([`../sentencias/decisiones-de-diseno.md`](../sentencias/decisiones-de-diseno.md)
§3): el que se almacena es siempre el acceso de afuera.

## 4. Las rutinas de soporte, escritas a mano una sola vez

Crear un arreglo de varias dimensiones necesita un ciclo por dimensión, y controlar un índice
necesita una comparación doble. En lugar de generar esas secuencias en cada uso, se escriben una
vez en CeIASM, en `RutinasDeSoporte`, y el código generado las llama con la convención de
cualquier unidad estática (Apunte §8). El Apunte §12 lo sugiere para los arreglos («o bien una
rutina auxiliar recursiva»), y es lo mismo que hace con el heap (Apéndice B).

- `nuevo_arreglo(tamaño)`: controla el tamaño, aloja y carga la VT de `Object` y la longitud.
- `nuevo_arreglo_multi(tamaños, nivel)`: recibe los tamaños en un arreglo de enteros y crea cada
  nivel con una llamada recursiva. En el nivel 0 controla todos los tamaños antes de alojar nada.
- `componente(arreglo, índice)`: controla el arreglo nulo y el rango, y devuelve `arreglo+índice`.

Se prueban solas, con código CeIASM armado a mano (`PiezasDelGeneradorTest`), y en todos los
programas que crean o indexan arreglos.

## 5. El heap de la cátedra, con el bloque en cero

Se usan `simple_heap_init` y `simple_malloc` de `rutinasHeap.txt`: el heap sólo crece. Al malloc se
le agrega un ciclo que deja el bloque en cero: los atributos y las componentes arrancan en 0, `false`
o `null`, como en Java (JLS §4.12.5), y la CeIVM no garantiza memoria limpia en una zona por donde
pasó la pila. Un String nuevo aprovecha lo mismo: su terminador ya está en 0.

## 6. La celda del resultado, con `PUSH 0`

El Apunte (§8 y §10) reserva la celda del resultado de una llamada con `RMEM 1`. Pero `RMEM` sólo
mueve el tope de la pila: la celda conserva lo que había. Como MiniJava no exige que un método con
retorno devuelva un valor en todos los caminos (documento §12.5), un método que termina sin
`return` devolvería basura, por ejemplo el resultado de una llamada anterior. Con `PUSH 0` la celda
arranca en 0 y el resultado está definido. Las rutinas de soporte, que siempre escriben su
resultado, usan `RMEM 1` como el Apunte.

## 7. Los métodos predefinidos son unidades como las demás

`Object`, `String` y `System` están en la tabla como cualquier clase, y sus métodos se llaman con
la misma secuencia. Lo único distinto es el cuerpo, que no sale de un AST sino de `Predefinidos`,
con las instrucciones del Apunte §14. Así no hay un caso especial en las llamadas.

## 8. Los mensajes de error, en `.data`

Los mensajes de los errores de ejecución son cadenas de la CeIVM, `DW "…", 0` en `.data` (Apunte
§16), y no Strings de MiniJava: los imprime `SPRINT` directamente, sin crear un objeto. Los Strings
del programa van al heap, como pide el Apunte §13.
