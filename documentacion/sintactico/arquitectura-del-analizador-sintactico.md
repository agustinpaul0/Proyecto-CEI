# Arquitectura del Analizador Sintáctico

Cómo está construido el analizador de la Etapa 2, cómo se conecta con el léxico de la Etapa 1 y
cómo queda organizado el código para que las cinco etapas conviyan.

> **Estado.** La etapa está **terminada**: los 109 no terminales implementados, los diez logros
> —Modo Pánico incluido—, los 8 casos de la cátedra pasando con el token exacto y las dos
> restricciones de MiniLambdas que la gramática no puede expresar. La verificación completa está en
> [`README.md`](README.md).

## 1. La técnica: Descendente Recursivo Predictivo, Esquema Simple

No es una elección abierta. La cátedra fija el método en la Clase 5 (slides 33-62) y el profesor
dijo que siguiéndolo la solución es correcta. Cualquier desvío se documenta con la alternativa
evaluada y su razón.

La idea: **simular la derivación a izquierda mediante una cadena de llamadas recursivas**, donde
cada llamada es un paso de la derivación.

- **Un método privado por cada no terminal** de la gramática.
- En el cuerpo de ese método se codifican las partes derechas de sus producciones: para un **no
  terminal** se llama a su método, para un **terminal** se hace `match`.
- Cuando el no terminal tiene **varias** producciones, se elige la que tenga al token actual en los
  **Primeros** de su cuerpo.

## 2. Las tres formas de método

Salen de Clase 5, slides 36, 52-54 y 55. Todo método del analizador es una de las tres.

**Una sola producción** — traducción directa, sin decisión (slide 55):

```java
private void inicial() {           // <Inicial> ::= <ListaClases> eof
    listaClases();
    match(EOF);
}
```

**Varias producciones, ninguna anulable** — cadena de `if` sobre Primeros; el `else` es error
(slide 36):

```java
private void tipoPrimitivo() {     // boolean | char | int | float
    if      (esta(tokenActual, PR_BOOLEAN)) match(PR_BOOLEAN);
    else if (esta(tokenActual, PR_CHAR))    match(PR_CHAR);
    else if (esta(tokenActual, PR_INT))     match(PR_INT);
    else if (esta(tokenActual, PR_FLOAT))   match(PR_FLOAT);
    else throw new ExcepcionSintactica(tokenActual, PRIMEROS_TIPO_PRIMITIVO);
}
```

**Varias producciones, alguna anulable** — mismo esquema, pero el `else` **no hace nada**
(slides 52-54):

```java
private void restoExpresionCompuesta() {   // <OperadorBinario> <ExpresionBasica> <Resto…> | ε
    if (PRIMEROS_OPERADOR_BINARIO.contiene(tokenActual)) {
        operadorBinario();
        expresionBasica();
        restoExpresionCompuesta();
    }
    // else: derivó a vacío. No es un error: lo resuelve quien llamó.
}
```

Esa tercera forma es la que le permite al Esquema Simple prescindir de los Siguientes.

## 3. `match`, el único punto que consume entrada

```java
public void match(TipoDeToken esperado) {
    if (actual.tipo() != esperado) throw new ExcepcionSintactica(actual, nombreDe(esperado));
    avanzar();
}
```

Vive en `EntradaDeTokens`, que es el lookahead del analizador y **el único punto que consume
entrada**. Como las reglas están repartidas en diez secciones, cada una llega a él por un método
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
  donde la gramática tiene `X -> α X | ε`. Hubo una versión con `while` y la revirtió el profesor:
  aleja el código del esquema que se extiende en el semántico. El analizador no se toma **ninguna**
  libertad respecto del Esquema Simple.

## 4. El flujo completo

```
Main
 └─ ModuloPrincipalSintactico
     └─ AnalizadorSintactico(analizadorLexico)
          constructor:  tokenActual = lexico.proximoToken()   ← carga el primero
                        inicial()                             ← arranca por el símbolo inicial

          un método por NT ─── NT en el cuerpo ──→ llamada al método de ese NT
                           └── terminal ─────────→ match(TIPO)
                                                    ├ coincide → tokenActual = lexico.proximoToken()
                                                    └ no       → ExcepcionSintactica
```

El constructor recibe el léxico como dependencia, carga el primer token y llama al símbolo inicial
(Clase 5, slide 61). Cuando `inicial()` retorna sin excepción y se consumió el `eof`, el programa
es sintácticamente válido.

## 5. Los conjuntos de Primeros en el código

No van escritos a mano dentro de cada `if`. Van como **constantes con nombre** en un paquete
`primeros/`, cada una con un comentario que cite la producción de la que sale:

```java
// <ExpresionBasica> ::= <OperadorUnario> <Operando> | <Operando>
static final ConjuntoDePrimeros PRIMEROS_EXPRESION_BASICA = de(
        OP_SUMA, OP_RESTA, OP_NEGACION, /* … los de <Operando> … */);
```

El motivo es que el código se pueda leer contra la gramática del informe renglón por renglón, y que
un cambio en la gramática tenga un único lugar donde reflejarse.

Las 220 constantes —una por producción— **se generan**, no se escriben:

```bash
python3 herramientas/primeros_a_java.py gramatica/minijava-ll1-final.g
```

Generarlas es lo único que garantiza que lo que el analizador usa para elegir producción sea
exactamente lo que el verificador calcula sobre la gramática. Las clases `PrimerosDeX` —una por
sección de la gramática, la misma división que `partes/`— no se editan a mano; si
la gramática cambia, se regenera.

## 5.1. Cómo se divide el analizador

Un analizador descendente recursivo tiene un método por no terminal, y con 109 no terminales una
sola clase se vuelve inmanejable. El código está dividido **por sección de la gramática**, una clase
por sección:

```
sintactico/
  AnalizadorSintactico    el arranque y el contrato de uso
  EntradaDeTokens         el lookahead y el único punto que consume entrada
  RestriccionesDeLambda   el estado de los chequeos extra-gramaticales
  Sintaxis                cablea las nueve partes
  errores/                ExcepcionSintactica
  primeros/               ConjuntoDePrimeros + Primeros (generado)
  partes/                 AnalizadorDeClases, DeMiembros, DeTipos, DeSentencias,
                          DeFors, DeExpresiones, DeReferencias, DeLambdas, DeCreacion
```

**Por qué hay una fachada `Sintaxis` y no inyección directa.** La gramática tiene recursión mutua
entre secciones: una sentencia contiene expresiones, una expresión contiene lambdas, y el cuerpo de
una lambda vuelve a ser una expresión. Con inyección por constructor eso es un ciclo que no se puede
construir. Cada parte recibe la fachada y le pide el colaborador **en el momento de usarlo**, que es
cuando ya está construido; así no hacen falta setters ni construcción en dos fases.

**Por qué `EntradaDeTokens` está separada.** Es el único objeto con estado de posición —el token
actual y cuántos se consumieron—. Sacarlo de las reglas deja a cada parte sin más estado que el que
necesita para decidir, y concentra en un solo lugar la invariante de que sólo `match` avanza.

## 6. Organización del código

El repositorio está organizado **por fase**, para que las cinco etapas convivan:

```
compilador/src/main/java/minijava/
  comun/         io/  salida/  config/                compartido por todas las etapas
  lexico/        AnalizadorLexico, automata/, estados/, contexto/, recuperacion/,
                 tokens/, errores/
  sintactico/    AnalizadorSintactico, primeros/, errores/
  aplicacion/    Main + OpcionesDeInvocacion + ModuloPrincipalLexico + ModuloPrincipalSintactico
```

El árbol de tests espeja esa estructura, y `resources/` se separa igual:
`resources/{lexico,sintactico}/{sinErrores,conErrores}`.

`errores/` cuelga de cada fase y no de `comun/`: el reporte léxico usa `TipoDeErrorLexico`, la
columna y la línea del fuente, que son de esa fase, y dejarlo en `comun/` obligaría a que `comun`
dependa de `lexico`.

**Un solo `Main`, con selector de etapa.** Corre la Etapa 2 por defecto y acepta `--etapa=1` para
volver al volcado de tokens. Un único jar, que sigue cumpliendo el contrato de invocación de las
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
**mismo veredicto** y, cuando los dos rechazan, el **mismo token**. Sobre las **1953 fuentes** del
corpus versionado coinciden en todas.

Y hay un tercero. `herramientas/gramatica_a_javacc.py` genera, desde la misma gramática, un parser
con **JavaCC**: un generador de terceros que con `LOOKAHEAD = 1` usa exactamente la misma técnica.
Es el oráculo con el que se etiqueta el corpus generado, así que cada caso del corpus lleva la
expectativa de una implementación distinta de la que la verifica.

Las únicas divergencias posibles son las de los chequeos que una gramática libre de contexto no
puede expresar, donde el analizador es deliberadamente más restrictivo, y van **declaradas** en la
herramienta con su motivo: una divergencia sin declarar la hace fallar. Es el mismo criterio que el
registro de ambigüedades del verificador.

Una cuarta verificación cierra el otro extremo: `herramientas/equivalencia_de_lenguajes.py`
comprueba con un reconocedor de Earley que la gramática final con cero logros y la de la cátedra
**generen el mismo lenguaje**, derivando una cadena por cada producción de cada una. Es la respuesta
a la exigencia de Clase 4 de adaptar la gramática «sin cambiar el lenguaje que genera».

## 7. Los logros no son configurables

**El compilador funciona exclusivamente con todos los logros de la Etapa 1 y de la Etapa 2,
siempre. La gramática está escrita en base a todos los logros y sólo así: no tiene reglas dinámicas
que aparecen y desaparecen según los logros.**

Lo dictaminó el profesor, revirtiendo la decisión anterior. El argumento es de mantenimiento: la
gramática se complejizaba sin necesidad, y cada etapa nueva obligaría a rehacer el trabajo —volver a
marcar producción por producción— con más chance de error.

En la práctica desapareció bastante código: el paquete `minijava.comun.config` entero
(`Logro`, `ConfiguracionDelCompilador`, `ParserDeConfiguracion`), las anotaciones de test
`@RequiereLogro` / `@RequiereLogroApagado` con su `ExecutionCondition`, los manifiestos
`requisitos-de-logros.txt` del corpus, la opción `--logros=` y la propiedad `-Dlogros`, y las marcas
`@+`/`@-` del `.g` con el modo `--combinatoria` que las recorría. `--etapa=N` sobrevive, porque es
lo que elige entre el módulo principal léxico y el sintáctico.

El núcleo sin logros sigue existiendo como **gramática**, no como configuración:
`compilador/gramatica/minijava-ll1.g`. El compilador no lo consume; está para que
`equivalencia_de_lenguajes.py` demuestre por Earley que corregir la gramática de la cátedra a LL(1)
no cambió el lenguaje.

## 8. Impacto sobre el Analizador Léxico

Cuatro logros exigen tokens que **no figuran en «Aspectos Léxicos de MiniJava»**:

| Elemento | Logro | Estado en la Etapa 1 |
|---|---|---|
| `for` | Fors! E2 | se reconoce como `idMetVar` |
| `private` | Visibilidad Mejorada E2 | se reconoce como `idMetVar` |
| `?` | Operador Ternario E2 | es un error léxico |
| `->` | MiniLambdas E2 | se reconoce como `-` seguido de `>` |

Se agregan al vocabulario sin condición, así que la columna de la derecha describe lo que pasaba en
la Etapa 1 y no una configuración alcanzable hoy. `TablaDeLexemas` conoce el vocabulario completo, y
quien reconoce cada uno es el estado que corresponde: `EstadoIdMV` para `for` y `private`,
`EstadoInicial` para el `?`, y `EstadoOperadorConSufijoOpcional` para el `->`. **No toca** el patrón
State, ni el driver, ni el manejo de errores, ni el `SourceManager`, y no introduce estado mutable.

Los tokens `:`, `++` y `--` ya existían desde la Etapa 1; `++` y `--` estaban en el léxico sin
ninguna regla de la gramática que los usara, y el logro de posfijos es el que se los da.

## Ver también

- [`decisiones-de-diseno.md`](decisiones-de-diseno.md) — por qué la gramática está transformada
  como está.
- [`decisiones-de-deteccion-de-errores.md`](decisiones-de-deteccion-de-errores.md) — dónde falla el
  análisis y qué chequea el analizador que la gramática no puede expresar.
- [`../lexico/arquitectura-del-analizador-lexico.md`](../lexico/arquitectura-del-analizador-lexico.md)
  — la etapa anterior.
