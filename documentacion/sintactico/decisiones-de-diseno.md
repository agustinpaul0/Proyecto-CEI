# Decisiones de diseño del Analizador Sintáctico

Por qué la gramática transformada quedó como quedó. No *qué* se transformó —eso está paso a paso
en [`transformacion-a-ll1-paso-a-paso.md`](transformacion-a-ll1-paso-a-paso.md)— sino los criterios
que gobernaron todas las transformaciones.

## 1. El orden: recursión primero, factorización después

Clase 4, slide 37. Eliminar la recursión a izquierda **genera producciones nuevas sin factorizar**,
así que hacerlo al revés obliga a factorizar dos veces.

No es teórico: pasó. Al eliminar la recursión de `<Referencia>`, las producciones resultantes
dejaron a `<VarEncadenada>` y `<MetodoEncadenado>` compartiendo el prefijo `.`, un conflicto que en
la gramática original no existía como tal porque las dos vivían en alternativas separadas.

## 2. No factorizar de más

Clase 4, slide 40: *«las reglas de la gramática original tienen un significado; al modificarla, al
analizador semántico le puede llegar a ser más difícil entender el programa»*.

Se tradujo en cuatro criterios operativos:

- se conserva el nombre original **siempre que la regla sobreviva**;
- los no terminales nuevos llevan **nombres semánticos en español** (`<RestoExpresionCompuesta>`,
  no `<ExpresionCompuesta'>`);
- cuando dos no terminales originales se fusionan a la fuerza, se dice **cuáles y por qué**;
- se factoriza **sólo donde hace falta**: en `<Creacion>`, por ejemplo, `new int[3]` y `new T[3]` se
  deciden en el primer token después de `new`, y sólo el caso `new C…` necesitó un segundo nivel.

## 3. Insertar el no terminal, no fusionar

El criterio que más decisiones concretas produjo.

Al factorizar un prefijo hay dos caminos:

- **Fusionar** — meter los cuerpos de los no terminales involucrados dentro del padre y borrarlos.
  Quedan menos no terminales, pero el punto donde se decide *qué construcción era* pierde el
  nombre, y el analizador semántico de la Etapa 3 pierde el lugar donde colgar el nodo del árbol.
- **Insertar un no terminal** — factorizar el prefijo y poner la disyuntiva restante en un no
  terminal **nuevo y dedicado**, cuyo nombre dice qué se está decidiendo. Un no terminal más, pero
  se traduce a un método del analizador que **es** el punto de decisión semántica.

**Se adopta siempre la segunda**, con dos corolarios.

### 3.1. Un no terminal de decisión por contexto, aunque la forma coincida

Dos disyuntivas con la misma pinta pero distinto significado **no** se comparten.

`<EncadenadoAtributoOMetodo>` y `<AccesoLlamada>` terminan los dos en «`<ArgsActuales>` o nada».
Podría ser el mismo no terminal. No lo es, porque su `ε` significa cosas distintas:

| Desde | `ε` significa | Nodo del AST |
|---|---|---|
| `<EncadenadoAtributoOMetodo>` | era un acceso encadenado | atributo de **otro** objeto |
| `<AccesoLlamada>` | era un acceso a variable | variable local, parámetro o atributo propio |

Son dos resoluciones de nombre distintas. Compartir el no terminal obligaría a que un único método
del analizador se preguntara quién lo llamó para saber qué construir.

Lo mismo con las inicializaciones: `<InicializacionDeAtributo>` e `<InicializacionDeDeclarador>`
tienen cuerpos idénticos y son no terminales distintos, porque pertenecen a **logros distintos** y
tienen que poder prenderse por separado.

### 3.2. Compartir sí, cuando es literalmente la misma construcción

`<CuerpoDeMetodo>` (`<ArgsFormales> <Bloque>`) se usa desde método, método `static` y constructor
porque en los tres casos es lo mismo. `<Metodo>` (`idMetVar <CuerpoDeMetodo>`) lo comparten la rama
`static` y la `void`, de modo que el semántico tiene un único lugar donde construir el nodo de
método para esas dos formas.

## 4. Una transformación sólo vale si preserva el lenguaje exacto

Clase 3, slide 42: *«adaptarla sin cambiar el lenguaje que genera»*. Dos casos donde la
transformación «natural» lo habría violado y hubo que frenarse.

**El lado derecho de la asignación.** La regla original es
`<Expresion> ::= <ExpresionCompuesta> <OperadorAsignacion> <ExpresionCompuesta>`: a la derecha del
`=` hay una `<ExpresionCompuesta>`, no una `<Expresion>`. O sea que **`a = b = c` no es un programa
MiniJava válido**. Factorizar poniendo `<Expresion>` a la derecha —que es lo que sale solo al
escribirlo, y queda más prolijo— la volvería asociativa a derecha y aceptaría programas que la
cátedra no define.

**Las dimensiones con tamaño.** `<DimensionesConTamanio>` exige **al menos una** dimensión. La
factorización directa a `… | ε` permitiría cero. La parte repetible fue a un no terminal opcional
aparte.

## 5. Dónde vive cada logro

**Los logros no son configurables.** La gramática es una sola y describe MiniJava con todos los de
la Etapa 1 y la Etapa 2 incorporados: no hay producciones que aparezcan y desaparezcan según nada.
Lo dictaminó el profesor, y revirtió la decisión anterior —que cada logro fuera un interruptor
independiente, con la gramática llevando las dos formas marcadas `@+logro`/`@-logro`—. El motivo:
complejiza la gramática sin necesidad, y cada etapa nueva obligaría a rehacer el trabajo con más
chance de error.

Lo que sí sobrevive de aquella decisión es **dónde** aterriza cada logro, porque es lo que hace la
gramática legible: cada uno agrega una capa o unas alternativas identificables, y no reescribe las
reglas de al lado. El ternario es `<RestoCondicional>` y la capa `<ExpresionCondicional>`; los
posfijos, `<PosfijoOpcional>`; las lambdas, el bloque de `<ParentizadaOLambda>`. Dos logros que
necesitan una construcción parecida llevan cada uno la suya.

El núcleo sin logros existe todavía, pero **fuera del compilador**: es
`compilador/gramatica/minijava-ll1.g`, y está sólo para que `equivalencia_de_lenguajes.py`
demuestre que corregir la gramática de la cátedra a LL(1) no cambió el lenguaje.

## 6. Nombres y notación

Notación BNF-extendida idéntica a la del documento de la cátedra: `<X> ::= α | β`, `ε`, terminales
destacados. El objetivo es que las reglas del informe se puedan comparar renglón a renglón con las
del PDF original.

Los no terminales nuevos llevan nombres que dicen qué deciden, no cómo se generaron:
`<AtributoOMetodo>`, `<ArregloOInstanciaDeClase>`, `<SentenciaDesdeIdClase>`,
`<EncadenadoAtributoOMetodo>`. La notación prima (`X'`) se descartó a propósito: dice de dónde
salió la regla pero no qué representa, y el semántico necesita lo segundo.

## 7. La gramática es la fuente de verdad, los documentos se generan

`compilador/gramatica/minijava-ll1-final.g` es el único lugar donde se edita la gramática. La
gramática final del informe y de
[`gramatica-ll1-final.md`](gramatica-ll1-final.md) **se generan** desde ese archivo, que es el
mismo que verifica el script. Si divergieran, el informe podría afirmar algo que la verificación no
comprueba.

```bash
cd compilador
python3 herramientas/gramatica_a_markdown.py gramatica/minijava-ll1-final.g --sin-cabecera
python3 herramientas/gramatica_a_latex.py    gramatica/minijava-ll1-final.g producciones
python3 herramientas/gramatica_a_latex.py    gramatica/minijava-ll1-final.g primeros
```

## Ver también

- [`transformacion-a-ll1-paso-a-paso.md`](transformacion-a-ll1-paso-a-paso.md) — cada
  transformación, en cuatro pasos.
- [`decisiones-de-logica.md`](decisiones-de-logica.md) — qué lenguaje reconoce exactamente.
- [`arquitectura-del-analizador-sintactico.md`](arquitectura-del-analizador-sintactico.md) — cómo
  se traduce a código.

---

## Decisiones cerradas: lo que no se vuelve a discutir

Resumen de las decisiones que la etapa dio por zanjadas. Cada una está desarrollada en el
documento que le corresponde; esta lista existe para que no se relitiguen.

- Gramática LL(1) cerrada, con nombres de no terminal semánticos en español y notación BNF del PDF
  de la cátedra. **No se rediseña**; si aparece un defecto se corrige puntualmente y se regeneran
  los documentos.
- Ante factorizar fusionando o insertando un no terminal, **se inserta**: el NT insertado es el
  punto de decisión que el semántico de la Etapa 3 necesita. Un NT de decisión por contexto,
  aunque dos contextos tengan la misma forma.
- Un solo `Main` con selector de etapa, no dos jars.
- Los cuatro tokens léxicos nuevos de la Etapa 2 —`for`, `private`, `?` y `->`— existen siempre.
- La ambigüedad `<Asignacion>`/`<Llamada>` se colapsa en una sentencia-expresión; distinguirlas es
  trabajo del semántico. El *dangling else* se resuelve por regla de implementación: la rama
  `else` va primero, y liga al `if` más cercano.
- Los métodos de interfaz admiten `public` y no `private`, y **son siempre abstractos**: ningún
  logro de esta etapa les agrega cuerpo.
- El `this` dentro de una lambda —explícito e implícito—, la captura de variables locales y de
  parámetros, y que los parámetros de una lambda no se repitan **quedan para el analizador
  semántico**. Lo indicó el profesor: son controles de significado, ahí se resuelven más fácil, y
  el logro de esta etapa no los pide. El sintáctico los acepta todos. Se borró con ellos el módulo
  `RestriccionesDeLambda`, que era la lógica más compleja del analizador: la pila de capturas en
  duda existía sólo para diferir ese chequeo.
- De las restricciones de lambda **sobrevive una**, porque es de forma y no de significado: que el
  primer parámetro sea un identificador sin tipo (`(a+1) -> a` se rechaza). No es expresable en una
  gramática libre de contexto y se decide con lo que el analizador acaba de leer.
- **Una lambda entre paréntesis SÍ es un operando**: `(x -> y).m()` y `(x -> y) + 1` se aceptan,
  aunque Java los rechace. Sale de la gramática base, que define `ExpresionParentizada -> ( Expresion )`
  y `Primario -> ExpresionParentizada`. Sin paréntesis sigue sin serlo: `1 + x -> x` no deriva.
- Las tres ambigüedades de la gramática se resuelven **voraz**, por regla de implementación, y
  están declaradas en el verificador. Ninguna se resuelve reescribiendo la gramática.
- **La lambda se produce en el nivel de `<Expresion>`, nunca en `<Primario>`**, como el JLS. No es
  un operando: `1 + x -> x`, `- x -> x`, `(x -> x) + 1` y `(x -> x).m()` se rechazan, igual que en
  Java. No se vuelve a discutir.
- **Ningún método de no terminal omite el `else` final de su cadena de Primeros.** Si el no terminal
  no es anulable, ese `else` es el error (Clase 5, slide 39); si alguna parte derecha deriva a ε, es
  un `else` vacío con la producción citada al lado —el «no hago nada :)» de la slide 52—, que es
  donde la Etapa 3 va a colgar la traducción de la producción vacía. Pedido explícito del profesor.
- **`public static` y `static public` son los dos válidos**, y lo mismo `private static` /
  `static private`. Que la visibilidad aparezca a un solo lado del `static` es lo que impide repetir
  el modificador, sin ningún chequeo fuera de la gramática. `protected` no se implementa: no está en
  MiniJava y el profesor indicó no agregarlo.
- **Toda dimensión de una creación de arreglo lleva su tamaño**, como en MiniJava base
  (`CrearArreglo -> new TipoBase DimensionesConTamanio`), y el inicializador se le cuelga al final:
  `new int[2][2]{{1,2},{3,4}}` es válido y `new int[]{1,2,3}` no. El inicializador es anidable.
  Esto **diverge de Java**, que prohíbe tamaño e inicializador juntos (JLS §15.10.1, verificado con
  `javac`); es deliberado, para no ampliar la forma de las dimensiones que MiniJava ya tenía
  definida. Queda aislado en `<CreacionDeArreglo>` por si hay que revertirlo.
- `int x[]` **no existe**: las dimensiones van pegadas al tipo base y nunca al nombre, que es lo que
  define MiniJava base. No se amplía.
- Una clase hereda de **una** clase o implementa **una** interfaz, nunca las dos ni varias: es
  `<HerenciaOpcional> ::= extends <TipoReferencia> | implements <TipoReferencia> | ε`, tal cual la
  gramática de la cátedra.
- **La recursión a derecha de la gramática se traduce como recursión en el código**, no como un
  `while`. Es el Esquema Simple de Clase 5, slide 59, sin desvíos. Pedido del profesor: el `while`
  aleja el código del esquema que se extiende en el semántico y es más propenso a errores.
