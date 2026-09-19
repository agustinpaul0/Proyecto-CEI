# Arquitectura del Analizador Sintáctico

Cómo está construido el analizador de la Etapa 2, cómo se conecta con el léxico de la Etapa 1 y
cómo queda organizado el código para que las cinco etapas convivan.

> **Estado.** La etapa está **terminada**: los 97 no terminales implementados, los nueve logros
> —Modo Pánico incluido— y los 8 casos de la cátedra pasando con el token exacto. La verificación
> completa está en [`README.md`](README.md).

## 1. La técnica: Descendente Recursivo Predictivo, Esquema Simple

No es una elección abierta. La cátedra fija el método en la Clase 5 (slides 33-62) y el profesor
dijo que siguiéndolo la solución es correcta. Cualquier desvío se documenta con la alternativa
evaluada y su razón.

La idea: **simular la derivación a izquierda mediante una cadena de llamadas recursivas**, donde
cada llamada es un paso de la derivación.

- **Un método por cada no terminal** de la gramática.
- En el cuerpo de ese método se codifican las partes derechas de sus producciones: para un **no
  terminal** se llama a su método, para un **terminal** se hace `match`.
- Cuando el no terminal tiene **varias** producciones, se elige la que tenga al token actual en los
  **Primeros** de su cuerpo.

## 2. Las tres formas de método

Salen de Clase 5, slides 36, 52-54 y 55. Todo método del analizador es una de las tres.

**Una sola producción** — traducción directa, sin decisión (slide 55):

```java
// Inicial -> ListaClases eof
public void inicial() throws ExcepcionLexica, ExcepcionSintactica {
    listaClases();
    match(TipoDeToken.EOF);
}
```

**Varias producciones, ninguna anulable** — cadena de `if` sobre Primeros; el `else` es error
(slide 36):

```java
// TipoPrimitivo -> boolean | char | int | float
public void tipoPrimitivo() throws ExcepcionLexica, ExcepcionSintactica {
    if (estaEn(PrimerosDeTipos.TIPO_PRIMITIVO_BOOLEAN)) {
        match(TipoDeToken.PR_BOOLEAN);
    } else if (estaEn(PrimerosDeTipos.TIPO_PRIMITIVO_CHAR)) {
        match(TipoDeToken.PR_CHAR);
    } else if (estaEn(PrimerosDeTipos.TIPO_PRIMITIVO_INT)) {
        match(TipoDeToken.PR_INT);
    } else if (estaEn(PrimerosDeTipos.TIPO_PRIMITIVO_FLOAT)) {
        match(TipoDeToken.PR_FLOAT);
    } else {
        throw errorEsperando(PrimerosDeTipos.TIPO_PRIMITIVO_BOOLEAN, /* … las otras tres … */);
    }
}
```

**Varias producciones, alguna anulable** — mismo esquema, pero el `else` **no hace nada**
(slides 52-54):

```java
// RestoExpresionCompuesta -> OperadorBinario ExpresionBasica RestoExpresionCompuesta | EPSILON
private void restoExpresionCompuesta() throws ExcepcionLexica, ExcepcionSintactica {
    if (estaEn(PrimerosDeExpresiones.RESTO_EXPRESION_COMPUESTA_OPERADOR_BINARIO)) {
        operadorBinario();
        expresionBasica();
        restoExpresionCompuesta();
    } else {
        // RestoExpresionCompuesta -> EPSILON
    }
}
```

Derivar a vacío no es un error: si el token no sirve, lo detecta quien llamó.

Esa tercera forma es la que le permite al Esquema Simple prescindir de los Siguientes.

## 3. `match`, el único punto que consume entrada

```java
public void match(TipoDeToken esperado) {
    if (actual.tipo() != esperado) throw new ExcepcionSintactica(actual, nombreDe(esperado));
    avanzar();
}
```

Vive en `EntradaDeTokens`, que es el lookahead del analizador y **el único punto que consume
entrada**. Como las reglas están repartidas en nueve secciones, cada una llega a él por un método
puente de una línea en `ParteDelAnalizador`:

```java
protected final void match(TipoDeToken esperado) { sintaxis.entrada().match(esperado); }
```

Así cada método de no terminal se escribe `match(TipoDeToken.PR_CLASS)`, igual que en el ejemplo de
Clase 5 slide 57, sin que ninguna sección tenga acceso al analizador léxico.

Cuatro cosas que conviene tener presentes:

- **Ningún método omite el `else` final de su cadena de Primeros.** Si el no terminal no es
  anulable, ese `else` es el error (Clase 5, slide 39). Si alguna de sus partes derechas deriva a
  ε, es un `else` **vacío** con la producción citada al lado, que es literalmente lo que muestra la
  slide 52: *«else // No hago nada :)»*. Podría omitirse y el analizador haría lo mismo, pero
  entonces «acá se derivó a ε» dejaría de estar escrito en ningún lado — y es justo el punto donde
  la Etapa 3 va a colgar la traducción de la producción vacía.
- **El token actual *es* el lookahead.** El analizador nunca mira más de un token hacia adelante; el
  «token actual» ya es el que todavía no se consumió.
- **`match` es el único lugar que avanza**, junto con `descartar()`, que usa el Modo Pánico
  justamente para tirar entrada. Ningún método toca el léxico directamente, y por eso la posición
  del error es siempre la del token actual, sin bookkeeping extra.
- **La recursión a derecha se traduce con recursión**, como en Clase 5 slide 59, en los diez lugares
  donde la gramática tiene `X -> α X | ε`. Un `while` derivaría lo mismo, pero alejaría el código
  del esquema que se extiende en el semántico. El analizador no se toma **ninguna**
  libertad respecto del Esquema Simple.

## 4. El flujo completo

```
Main
 └─ ModuloPrincipalSintactico
     └─ AnalizadorSintactico(analizadorLexico)
          analizar():  entrada.cargarPrimerToken()      ← carga el primero
                       sintaxis.clases().inicial()      ← arranca por el símbolo inicial

          un método por NT ─── NT en el cuerpo ──→ llamada al método de ese NT
                           └── terminal ─────────→ match(TIPO)
                                                    ├ coincide → avanza al próximo token
                                                    └ no       → ExcepcionSintactica
```

El analizador recibe el léxico como dependencia; `analizar()` carga el primer token y llama al
símbolo inicial (Clase 5, slide 61). Devuelve los errores encontrados, en el orden en que
aparecieron: una lista vacía significa que el programa es sintácticamente válido.

## 5. Los conjuntos de Primeros en el código

No van escritos a mano dentro de cada `if`. Van como **constantes con nombre** en un paquete
`primeros/`, cada una con un comentario que cite la producción de la que sale:

```java
// ExpresionBasica -> OperadorUnario Operando
public static final ConjuntoDePrimeros EXPRESION_BASICA_OPERADOR_UNARIO = de(
        TipoDeToken.OP_NEGACION, TipoDeToken.OP_SUMA, TipoDeToken.OP_RESTA);
```

El motivo es que el código se pueda leer contra la gramática del informe renglón por renglón, y que
un cambio en la gramática tenga un único lugar donde reflejarse.

Las 197 constantes —una por producción— **se generan**, no se escriben:

```bash
python3 herramientas/primeros_a_java.py gramatica/minijava-ll1-final.g
```

Generarlas es lo único que garantiza que lo que el analizador usa para elegir producción sea
exactamente lo que el verificador calcula sobre la gramática. Las clases `PrimerosDeX` —una por
sección de la gramática, la misma división que `partes/`— no se editan a mano; si
la gramática cambia, se regenera.

## 5.1. Cómo se divide el analizador

Un analizador descendente recursivo tiene un método por no terminal, y con 97 no terminales una
sola clase se vuelve inmanejable. El código está dividido **por sección de la gramática**, una clase
por sección:

```
sintactico/
  AnalizadorSintactico    el arranque y el contrato de uso
  EntradaDeTokens         el lookahead y el único punto que consume entrada
  Sintaxis                cablea las nueve partes
  errores/                ExcepcionSintactica
  primeros/               ConjuntoDePrimeros + las nueve PrimerosDeX (generadas)
  partes/                 AnalizadorDeClases, DeMiembros, DeTipos, DeSentencias,
                          DeFors, DeExpresiones, DeReferencias, DeCreacion, DeArgumentos
  recuperacion/           RecuperadorDePanico, ContextoDeSincronizacion (Modo Pánico)
```

**Por qué hay una fachada `Sintaxis` y no inyección directa.** La gramática tiene recursión mutua
entre secciones: una sentencia contiene expresiones, una expresión contiene referencias, y una
expresión parentizada contiene otra expresión. Con inyección por constructor eso es un ciclo que no
se puede construir. Cada parte recibe la fachada y le pide el colaborador **en el momento de usarlo**, que es
cuando ya está construido; así no hacen falta setters ni construcción en dos fases.

**Por qué `EntradaDeTokens` está separada.** Es el único objeto con estado de posición —el token
actual y cuántos se consumieron—. Sacarlo de las reglas deja a cada parte sin más estado que el que
necesita para decidir, y concentra en un solo lugar la invariante de que sólo `match` avanza.

## 6. Organización del código

El repositorio está organizado **por fase**, para que las cinco etapas convivan:

```
compilador/src/main/java/minijava/
  comun/         io/  salida/                         compartido por todas las etapas
  lexico/        AnalizadorLexico, automata/, estados/, contexto/, recuperacion/,
                 tokens/, errores/
  sintactico/    AnalizadorSintactico, EntradaDeTokens, Sintaxis, partes/, primeros/,
                 recuperacion/, errores/
  aplicacion/    Main + OpcionesDeInvocacion + ModuloPrincipalLexico + ModuloPrincipalSintactico
```

El árbol de tests espeja esa estructura. En `resources/`, el corpus de la etapa que se entrega vive
en la raíz —`resources/{sinErrores,conErrores}`, donde lo buscan los testers de la cátedra— y el de
la Etapa 1 en `resources/lexico/{sinErrores,conErrores}`.

`errores/` cuelga de cada fase y no de `comun/`: el reporte léxico usa `TipoDeErrorLexico`, la
columna y la línea del fuente, que son de esa fase, y dejarlo en `comun/` obligaría a que `comun`
dependa de `lexico`.

**Un solo `Main`, con selector de etapa.** Corre la Etapa 2 por defecto y acepta `--etapa=1` para
el volcado de tokens del léxico. Un único jar, que sigue cumpliendo el contrato de invocación de las
Pautas (slide 28): `java -jar Compilador.jar programa1.java`. El motivo de que sea uno solo y no
dos jars es que la Etapa 1 se corrige por separado y tiene que seguir siendo ejecutable sin
recompilar nada distinto.

**Los tests se pueden correr por fase**, con etiquetas JUnit 5 y perfiles Maven:

```bash
mvn test                    # todo
mvn test -Pfase-lexico      # sólo léxico
mvn test -Pfase-sintactico  # sólo sintáctico
```

## 6.1. Que el analizador y la gramática no se separen

Que el analizador esté escrito a mano contra una gramática verificada deja una pregunta abierta:
¿reconoce el mismo lenguaje, o uno parecido? La respuesta es verificable:

```bash
python3 herramientas/comparar_con_gramatica.py 'ruta/*.java'
```

Corre el analizador en Java y el simulador del Esquema Simple sobre los mismos fuentes y exige el
**mismo veredicto** y, cuando los dos rechazan, el **mismo token**. Sobre las **10.405 fuentes** del
corpus versionado coinciden en todas.

Y hay un tercero. `herramientas/gramatica_a_javacc.py` genera, desde la misma gramática, un parser
con **JavaCC**: un generador de terceros que con `LOOKAHEAD = 1` usa exactamente la misma técnica.
Es el oráculo con el que se etiqueta el corpus generado, así que cada caso del corpus lleva la
expectativa de una implementación distinta de la que la verifica.

Ninguna divergencia es admisible: el analizador no tiene chequeos fuera de la gramática, así que
cualquier diferencia de veredicto o de token hace fallar la herramienta.

Una cuarta verificación cierra el otro extremo: `herramientas/equivalencia_de_lenguajes.py`
comprueba con un reconocedor de Earley que el núcleo sin logros (`minijava-ll1.g`) y la gramática de
la cátedra **generen el mismo lenguaje**, derivando una cadena por cada producción de cada una, y que
la gramática final acepte todas esas cadenas: los logros agregan lenguaje, no quitan. Es la
respuesta a la exigencia de Clase 4 de adaptar la gramática «sin cambiar el lenguaje que genera».

## 7. Los logros no son configurables

La gramática es una sola, y ni la línea de comandos ni los tests tienen forma de elegir logros.
`--etapa=N` es la única opción, y elige entre el módulo principal léxico y el sintáctico.

El núcleo sin logros existe como **gramática**, no como configuración:
`compilador/gramatica/minijava-ll1.g`. El compilador no lo consume; está para que
`equivalencia_de_lenguajes.py` demuestre por Earley que corregir la gramática de la cátedra a LL(1)
no cambió el lenguaje.

## 8. Impacto sobre el Analizador Léxico

Tres logros exigen tokens que **no figuran en «Aspectos Léxicos de MiniJava»**:

| Elemento | Logro | Estado en la Etapa 1 |
|---|---|---|
| `for` | Fors! E2 | se reconoce como `idMetVar` |
| `private` | Visibilidad Mejorada E2 | se reconoce como `idMetVar` |
| `?` | Operador Ternario E2 | es un error léxico |

Se agregan al vocabulario sin condición; la columna de la derecha describe el léxico de la Etapa 1
tal como se entregó. `TablaDeLexemas` conoce el vocabulario completo, y
quien reconoce cada uno es el estado que corresponde: `EstadoIdMV` para `for` y `private`, y
`EstadoInicial` para el `?`. **No toca** el patrón
State, ni el driver, ni el manejo de errores, ni el `SourceManager`, y no introduce estado mutable.

Los tokens `:`, `++` y `--` ya existían desde la Etapa 1; `++` y `--` estaban en el léxico sin
ninguna regla de la gramática que los usara, y el logro de posfijos es el que se los da.

## Ver también

- [`decisiones-de-diseno.md`](decisiones-de-diseno.md) — por qué la gramática está transformada
  como está.
- [`decisiones-de-deteccion-de-errores.md`](decisiones-de-deteccion-de-errores.md) — dónde falla el
  análisis, qué se reporta y cómo se recupera.
- [`../lexico/arquitectura-del-analizador-lexico.md`](../lexico/arquitectura-del-analizador-lexico.md)
  — la etapa anterior.
