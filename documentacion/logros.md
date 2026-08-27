# Los 8 logros de la Etapa 1

Documento autocontenido: qué exige cada logro (con el texto de la consigna), cómo se resolvió, dónde
está implementado y **cómo verificarlo** ejecutando algo concreto.

El proyecto persigue **los ocho desde el primer día**, no como agregados posteriores sobre un mínimo
producto viable. Esa decisión tiene una consecuencia de diseño visible en todo el código: cuatro de
los logros cambian el comportamiento observable del compilador, así que cada uno vive detrás de un
interruptor de configuración y el comportamiento base que pide la consigna sigue siendo demostrable.

## Resumen

| Logro | Naturaleza | Estado |
|---|---|---|
| 1. Floats I | cambia el lenguaje reconocido | implementado, con interruptor |
| 2. Unicodes | cambia el lenguaje reconocido | implementado, con interruptor |
| 3. Multi-detección de Errores Léxicos | cambia la política ante errores | implementado, con interruptor |
| 4. Reporte de Error Elegante | agrega salida | implementado, con interruptor |
| 5. Manejador de Archivos Eficiente | reemplaza una pieza | implementado — y es un requisito, no un extra |
| 6. Imbatibilidad Léxica | consecuencia de la suite | los 142 casos y los 2 testers de la cátedra pasan |
| 7. Good Job! I | consecuencia de lo anterior | — |
| 8. Entrega Anticipada Léxica | hito de calendario | — |

Los cuatro interruptores viven en un único lugar:

```java
ConfiguracionDelCompilador(floatsHabilitados, unicodeHabilitado,
                           reporteElegante, multiDeteccionDeErrores)
```

Los cuatro en `true` por defecto. Existen para que cada logro sea **testeable por separado** y para
poder demostrar que el compilador cumple tanto la consigna base como los logros — sin el
interruptor, el comportamiento base quedaría sepultado bajo el del logro.

## 1. Floats I

> *"El Analizador Léxico es capaz de procesar correctamente Literales Floats. Estos deben tener la
> misma estructura que los floats de Java, con la salvedad que siempre deben comenzar con un
> dígito."*

**Cómo se resolvió.** Cinco estados del autómata (`EstadoFloatConPunto`, `EstadoFloatExponenteInicio`,
`EstadoFloatExponenteRequiereDigito`, `EstadoFloatExponenteDigitos`, más las transiciones desde
`EstadoEntero`) implementan la gramática completa de Java: parte entera, parte fraccionaria,
exponente con signo opcional y sufijo `f`/`F`/`d`/`D`.

**El desafío real no fue la gramática, fue derivarla.** `Aspectos Lexicos MiniJava 2026.pdf` **no
menciona floats en absoluto** — no están entre sus "cinco tipos de literales". Toda la ER se derivó
de esa única frase de Pautas, verificando contra `javac` qué significa "la misma estructura que los
floats de Java". Ver [`decisiones.md`](decisiones.md) §3.2 y §3.3.

**Detalles que salieron de esa verificación:**

- Los floats **no** tienen límite de dígitos, a diferencia del `intLiteral`.
- Un float fuera del rango representable (`3.5e38f`, `1e999`) es un literal **válido**: Java lo
  rechaza al convertirlo, que es un chequeo semántico, no léxico.
- `1e` y `1.5e+` son errores, sin retroceder a un token más corto.
- `1234567890.5` es válido con diez dígitos en la parte entera, porque el límite de 9 sólo aplica si
  la corrida de dígitos **no** continúa hacia un float.

**Cómo verificarlo:**

```bash
java -jar target/Compilador.jar resources/sinErrores/floatFueraDelRangoDeJava.java
mvn -o test -Dtest=LimitesNumericosTest
```

## 2. Unicodes

> *"El Analizador Léxico es capaz de procesar correctamente caracteres Unicode dentro de los
> literales de string y de caracter. Esto también requiere interpretar correctamente las secuencias
> escapadas Unicode (por ejemplo, `\u0041` que representa al caracter 'A')."*

**Son dos cosas distintas, y se resolvieron por separado:**

*Caracteres crudos*: los estados de `charLiteral` y `litString` aceptan cualquier carácter no
reservado, sin filtro ASCII. `'ñ'` y `"letra ü"` funcionan sin código especial.

*Secuencias escapadas*: `EstadoMarcadorUnicode` consume el marcador y `EstadoEscapeUnicode` valida
los cuatro dígitos hexadecimales que le siguen.

**El detalle que sólo aparece leyendo la especificación**: el marcador `u` se repite **una o más
veces**. `\u0041`, `\uu0041` y `\uuuu0041` son todos válidos y equivalentes (JLS §3.3). Es
exactamente el tipo de cosa que uno no adivina y que la regla de "verificar, no recordar" hace
aparecer.

Además, el escape se lee **dentro** de dos construcciones distintas, así que los dos estados del
escape necesitan saber a cuál volver al terminar — es una de las tres razones por las que existen
estados construidos con `new` en vez de singletones. Ver [`patron-state.md`](patron-state.md) §6.

**Qué controla el interruptor**: sólo la secuencia escapada, **no** los caracteres crudos. Gatear los
crudos exigiría meter clasificadores "sólo ASCII" en todo el autómata, ensuciando el léxico entero
para demostrar algo que el escape ya demuestra.

**Cómo verificarlo:**

```bash
mvn -o test -Dtest=AnalizadorLexicoTest
grep -l unicode resources/sinErrores/*.java resources/conErrores/*.java
```

## 3. Multi-detección de Errores Léxicos

> *"El compilador no finaliza la ejecución ante el primer error léxico (se recupera) y es capaz de
> reportar todos los errores léxicos que tenga el programa fuente en una corrida."*

**Este es el logro con más profundidad de diseño detrás**, porque la cátedra dice **qué** hay que
lograr y no dice nada sobre **cómo** recuperarse — su comportamiento base ni siquiera tiene
recuperación. Se resolvió aplicando la regla "ante la duda, como en Java", verificando contra `javac`
qué hace él ante cada tipo de error.

**El criterio, en una línea**: todo estado que lanza un error consume del fuente exactamente los
caracteres que reporta en el lexema, ni uno más. **No hay ningún paso de recuperación**: el Módulo
Principal captura la excepción, la reporta y vuelve a pedir un token; el análisis retoma solo en el
primer carácter que no formó parte del error.

Es una propiedad estructural, no un algoritmo. Y trae la garantía de terminación de regalo: el lexema
de un error nunca es vacío, así que cada error consumió al menos un carácter y el análisis siempre
avanza.

**El bug que este criterio arregló** — la versión anterior descartaba siempre el carácter que estaba
mirando, incluso cuando nunca había formado parte del error:

| fuente | antes | ahora |
|---|---|---|
| `v1 &v2` | se comía la `v` y emitía `(intLiteral,2,1)` | `[Error:&\|1]` + `(idMV,v2,1)` |
| `1e;` | el `;` desaparecía sin mensaje | `[Error:1e\|1]` + `(puntoYComa,;,1)` |
| `1234567890+1` | el `+` desaparecía | error + `(op+,+,1)` + `(intLiteral,1,1)` |

Todos eran invisibles en el corpus de entonces porque cada caso tenía un espacio detrás del lexema
malo. Los descubrió una **invariante**, no un caso: "la concatenación de los lexemas de todos los
tokens y errores reconstruye el fuente completo".

**El caso especial**: un escape mal formado dentro de un `litString` es el único error que descarta
más de lo que reporta — se resincroniza en la comilla de cierre del literal. Es lo contrario de una
excepción a la multi-detección: sin ese descarte, el resto del literal se re-tokenizaría y la comilla
de cierre abriría un literal nuevo, inventando errores que ocultarían a los reales. El detalle, con
los números, está en [`decisiones.md`](decisiones.md) §4.3.

**Cómo verificarlo:**

```bash
java -jar target/Compilador.jar resources/conErrores/stringConDosEscapesInvalidos.java
mvn -o test -Dtest=CriterioDeRecuperacionTest,PropiedadesDelReporteTest
```

## 4. Reporte de Error Elegante

> *"El compilador cuando reporta un error, además del número de línea y la razón del error, muestra
> la línea en cuestión y apunta al lugar donde se produjo."*

Ejemplo de la consigna, y salida real del compilador para ese mismo fuente:

```text
Error Léxico en línea 2, columna 4: # no es un símbolo válido
Detalle: v1 # v2
            ^
[Error:#|2]
```

**Dos cosas que van más allá de lo pedido:**

*La columna.* La consigna sólo exige la línea. Se agrega la columna al mensaje legible — **nunca**
dentro del código `[Error:Lexema|NroLinea]`, que es el contrato exacto que compara el tester y no
lleva columna.

*Un tipo de error por punto de falla.* Antes los once puntos de falla compartían una única frase
genérica; ahora cada uno dice qué construcción falló (`charLiteral sin cerrar`,
`intLiteral de más de 9 dígitos`). El mensaje de `CARACTER_DESCONOCIDO` conserva el texto literal del
ejemplo de la cátedra, porque es el único que ella fija — con la tilde de "válido" corregida.

**Dónde apunta el `^`** fue la parte que más trabajo dio, y también se resolvió verificando `javac`
caso por caso. La regla: si el mensaje habla de un carácter puntual, señala ese carácter; si habla de
la construcción entera que falló, señala dónde empieza. Los nueve diagnósticos comparables coinciden
con `javac`. Ver [`manejo-de-errores.md`](manejo-de-errores.md) §4.

**Tres bugs reales que este logro obligó a arreglar**, todos de la misma familia:

1. El `^` apuntaba a un carácter distinto del que nombraba el mensaje.
2. El `^` colgaba una posición más allá del final de la línea impresa, señalando el aire.
3. Un lexema con un salto de línea crudo **partía en dos** el código `[Error:...]`, rompiendo el
   contrato de la cátedra de un elemento por línea.

Los tres son hoy invariantes verificadas sobre entradas generadas.

**Cómo verificarlo:**

```bash
java -jar target/Compilador.jar resources/conErrores/lexConErrores01.java
mvn -o test -Dtest=PosicionDelCaretTest,FormateadorDeErrorTest,ReporteDeErrorEleganteTest
```

## 5. Manejador de Archivos Eficiente

> *"Se debe implementar otro manejador de archivos que administre de forma más eficiente el fuente.
> En particular, en vez de leer de a líneas del fuente debe de a caracteres. Para esto debe valerse
> sólo de las librerías de manejo IO de Java. El nuevo manejador debe conformar con la interface
> `SourceManager`."*

**Este logro figura como opcional y en la práctica no lo es.** La implementación de referencia que
provee la cátedra falla contra sus propios casos de ejemplo: reporta el `EOF` una línea antes de lo
que espera el oráculo cuando el archivo termina en salto de línea, y lanza `NullPointerException` con
un archivo vacío. Sin reemplazarla, el compilador no pasa los tests provistos.

`ManejadorDeArchivoEficiente` lo resuelve, y además extiende la interfaz —cosa que la consigna
autoriza explícitamente— con los dos datos que el Reporte de Error Elegante necesita:
`getColumnNumber()` y `getCurrentLineText()`.

**Hay una decisión discutible que conviene mirar**: el manejador carga el archivo completo en memoria
de una sola vez y recién después sirve carácter por carácter. Cumple con no leer de a líneas y con
entregar de a caracteres, pero una lectura estricta de la consigna podría pedir que la lectura física
también sea incremental. Está desarrollado, con sus argumentos a favor y en contra, en
[`manejador-de-archivo.md`](manejador-de-archivo.md) §5.

**Cómo verificarlo:**

```bash
mvn -o test -Dtest=ManejadorDeArchivoEficienteTest
```

## 6. Imbatibilidad Léxica

> *"El software entregado pasa correctamente toda la batería de prueba utilizada por la cátedra."*

No se resuelve escribiendo código, se resuelve escribiendo tests. La apuesta del proyecto es que una
suite propia mucho más exigente que la de la cátedra hace que la batería de la cátedra sea un
subconjunto de lo ya cubierto.

Los instrumentos que apuntan específicamente a esto:

- **142 archivos de caso** (65 sin errores, 77 con errores) contra los 5 de ejemplo provistos.
- Un **tester estricto propio** que verifica la salida completa de cada caso con error —todos los
  errores y todos los tokens intercalados—, donde el de la cátedra sólo busca el primer código.
- **Tests basados en propiedades** que verifican invariantes sobre entradas generadas, no sobre casos
  elegidos a mano.
- **Fuzzing** con bytes UTF-8 arbitrarios, y un corpus de regresión que conserva cada entrada que
  alguna vez hizo fallar una corrida.
- **Análisis de mutación** con PIT: mide qué tan buena es la suite introduciendo defectos y
  comprobando que algún test los detecte.

**Cómo verificarlo:**

```bash
mvn -o verify          # 512 casos + umbral de cobertura
mvn -o -Pmutacion test # los mutantes sobrevivientes son tests faltantes
```

## 7. Good Job! I

> *"Gran trabajo! La entrega tiene sólo algunos errores menores."*

Consecuencia de lo anterior, no un objetivo con implementación propia. Lo que el proyecto hace para
merecerlo, además de la suite: **deuda técnica cero** como regla de trabajo — nada queda "para
después", la documentación se actualiza al cerrar cada tarea, y todo bug encontrado se convierte en
un test de regresión permanente.

## 8. Entrega Anticipada Léxica

> *"La entrega debe realizarse 48hs antes de la fecha límite."* (No válido para la reentrega ni para
> las etapas subsiguientes.)

Hito de calendario, sin implicancia de código.

## Ver también

- [`decisiones.md`](decisiones.md) — el razonamiento detrás de cada logro que exigió una decisión.
- [`manejo-de-errores.md`](manejo-de-errores.md) — los logros 3 y 4 en detalle.
- [`manejador-de-archivo.md`](manejador-de-archivo.md) — el logro 5 en detalle.
- [`testing.md`](testing.md) — la maquinaria detrás de los logros 6 y 7.
