# Arquitectura del Analizador Sintáctico

Cómo está construido el analizador de la Etapa 2, cómo se conecta con el léxico de la Etapa 1 y
cómo queda organizado el código para que las cinco etapas conviyan.

> **Estado.** La gramática está terminada y verificada, y el analizador está **completo**: los 104
> no terminales implementados, los 8 casos de la cátedra pasando con el token exacto, y las tres
> restricciones de MiniLambdas que la gramática no puede expresar. Falta el Modo Pánico, que es la
> Sesión 4. El plan está en
> [`../proyecto/plan-de-accion-etapa-2.md`](../proyecto/plan-de-accion-etapa-2.md).

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
- **La recursión a derecha se traduce con un `while`.** Clase 5 slide 59 muestra el método
  llamándose a sí mismo; acá se usa un bucle, que deriva la misma cadena y no hace depender de la
  pila de la JVM una cadena larga de operadores o una lista larga de argumentos.

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
python3 herramientas/primeros_a_java.py gramatica/minijava-ll1-logros.g
```

Generarlas es lo único que garantiza que lo que el analizador usa para elegir producción sea
exactamente lo que el verificador calcula sobre la gramática. Las clases `PrimerosDeX` —una por
sección de la gramática, la misma división que `partes/`— no se editan a mano; si
la gramática cambia, se regenera.

`ConjuntoDePrimeros` además **filtra por logro** antes de describir lo esperado en un mensaje de
error: prometer un token que un logro apagado sacó del lenguaje sería ofrecer una entrada que el
compilador igual rechazaría.

## 5.1. Cómo se divide el analizador

Un analizador descendente recursivo tiene un método por no terminal, y con 111 no terminales una
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
**mismo veredicto** y, cuando los dos rechazan, el **mismo token**. Sobre las **1879 fuentes** del
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

## 7. Los logros son interruptores independientes

El compilador tiene que poder ejecutarse y testearse con **0, 1 o N logros en cualquier
combinación**, y la de cero logros debe reproducir exactamente la especificación base de la
cátedra.

`ConfiguracionDelCompilador` envuelve un `EnumSet<Logro>` con los 14 logros conmutables de las dos
etapas, configurable por CLI (`--logros=ninguno`, `--logros=ternario,for`, `--logros=etapa1`) y por
la propiedad de sistema `-Dlogros` para los tests. El nombre de cada logro es el mismo flag con el
que la gramática marca sus producciones, para que el enum y `minijava-ll1-logros.g` se lean uno
contra el otro.

Los tests llevan `@RequiereLogro` / `@RequiereLogroApagado`, implementadas como `ExecutionCondition`
de JUnit 5: **saltean** el test cuando el logro no corresponde, no lo fallan — un test que falla por
un logro apagado estaría fallando por algo que el compilador no tiene que hacer. El corpus declara
lo mismo en `resources/lexico/requisitos-de-logros.txt`, y un test verifica que cada requisito
declarado corresponda a una dependencia real.

A nivel gramática esa condición está verificada en las 1024 combinaciones; a nivel código, en 16
configuraciones de la suite. Por cada logro hay el par completo de tests: el caso que pasa con el
logro prendido y el que verifica el comportamiento base con el logro apagado.

## 8. Impacto sobre el Analizador Léxico

Cuatro logros exigen tokens que **no figuran en «Aspectos Léxicos de MiniJava»**:

| Elemento | Logro | Estado en la Etapa 1 |
|---|---|---|
| `for` | Fors! E2 | se reconoce como `idMetVar` |
| `private` | Visibilidad Mejorada E2 | se reconoce como `idMetVar` |
| `?` | Operador Ternario E2 | es un error léxico |
| `->` | MiniLambdas E2 | se reconoce como `-` seguido de `>` |

Se agregan **condicionados al interruptor de su logro**, de modo que con el logro apagado el
comportamiento léxico de la Etapa 1 queda intacto y verificable. `TablaDeLexemas` conoce el
vocabulario completo y declara qué logro habilita cada token (`logroQueLoHabilita`); quien decide es
el punto de reconocimiento: `EstadoIdMV` para `for` y `private`, `EstadoInicial` para el `?`, y
`EstadoOperadorConSufijoOpcional` para el `->`. **No toca** el patrón State, ni el driver, ni el
manejo de errores, ni el `SourceManager`, y no introduce estado mutable.

Los tokens de floats **no** entran en ese mapa: el logro Floats I gobierna la forma del literal
desde los estados numéricos, y `float` es palabra reservada aun con el logro apagado.

Los tokens `:`, `++` y `--` ya existían desde la Etapa 1; `++` y `--` estaban en el léxico sin
ninguna regla de la gramática que los usara, y el logro de posfijos es el que se los da.

## Ver también

- [`decisiones-de-diseno.md`](decisiones-de-diseno.md) — por qué la gramática está transformada
  como está.
- [`decisiones-de-deteccion-de-errores.md`](decisiones-de-deteccion-de-errores.md) — dónde falla el
  análisis y qué chequea el analizador que la gramática no puede expresar.
- [`../lexico/arquitectura-del-analizador-lexico.md`](../lexico/arquitectura-del-analizador-lexico.md)
  — la etapa anterior.
