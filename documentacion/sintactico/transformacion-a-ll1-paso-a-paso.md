# Transformación de la gramática de MiniJava a LL(1)

Paso a paso **atómico** de cada corrección aplicada a la gramática de la cátedra
([`gramatica-original-de-la-catedra.md`](gramatica-original-de-la-catedra.md)) para volverla LL(1) y poder
implementar el Analizador Sintáctico Descendente Recursivo con el Esquema Simple de Clase 5.

Cada transformación se registra con cuatro pasos:

1. **regla original**, tal cual el PDF;
2. **defecto** detectado y por qué rompe LL(1), con los Primeros que se solapan;
3. **transformación** aplicada, citando el esquema de la clase que la justifica;
4. **resultado** y verificación de que los Primeros quedan disjuntos.

La intención de este nivel de detalle es que cualquier error quede localizado en un paso
concreto y no escondido en un salto grande.

## 0. Método y verificación

### 0.1 El criterio

Una gramática es LL(1) si, para todo no terminal `A` con producciones
`A → β₁ | β₂ | … | βₙ`, se cumple para todo `i ≠ j` (Clase 3, slide 68):

```
Primeros(βᵢ) ∩ Primeros(βⱼ) = ∅
```

Sólo con eso un único token de lookahead alcanza para elegir producción sin backtracking.

### 0.2 El orden de las correcciones

Clase 4, slide 37: **primero eliminar la recursión a izquierda, después factorizar**, porque
eliminar la recursión genera producciones nuevas sin factorizar. Este documento sigue ese orden:
las transformaciones `R#` (recursión) van antes que las `F#` (factorización), y la sección
[R3](#r3-referencia) es el caso concreto donde eliminar recursión creó un conflicto nuevo.

Clase 4, slide 40: **no factorizar de más**. Las reglas originales tienen significado y el
analizador semántico de la Etapa 3 lo va a necesitar. Por eso:

- se conservan los nombres originales siempre que la regla sobreviva;
- los no terminales nuevos llevan **nombres semánticos en español**
  (`<RestoExpresionCompuesta>`, no `<ExpresionCompuesta'>`);
- se usa la misma notación BNF-extendida del PDF (`<X> ::= α | β`, `ε`, terminales en negrita);
- cuando dos NT originales se fusionan a la fuerza, se dice explícitamente cuáles y por qué.

### 0.3 Verificación mecánica

`compilador/herramientas/primeros.py` calcula vaciables, Primeros y la condición LL(1) sobre una
gramática en texto plano. Existe para **contrastar** la cuenta a mano, no para reemplazarla: si
el script y la cuenta manual difieren, primero hay que decidir cuál de los dos se equivocó.

```bash
cd compilador
python3 herramientas/primeros.py gramatica/minijava-base.g   # → NO ES LL(1): 16 conflictos
python3 herramientas/primeros.py gramatica/minijava-ll1.g    # → ES LL(1)
python3 herramientas/primeros.py gramatica/minijava-ll1.g --nt Miembro
python3 herramientas/primeros.py gramatica/minijava-ll1.g --markdown   # tabla para el informe
```

### 0.4 Dos defectos que se confunden: ambigüedad ≠ conflicto LL(1)

Vale separarlos antes del diagnóstico, porque casi todos los defectos de esta gramática son del
segundo tipo y sólo dos son del primero.

- Una gramática es **ambigua** cuando existe una cadena con **dos árboles de derivación
  distintos** (Clase 4, slide 46). Es un defecto del lenguaje descripto: ni siquiera queda claro
  qué significa el programa. No siempre se puede arreglar.
- Una gramática tiene un **conflicto LL(1)** cuando dos producciones del mismo NT comparten
  Primeros. La cadena sigue teniendo **un solo** árbol; lo que falla es que el parser no puede
  *predecir* cuál producción usar con un token de lookahead. Se arregla factorizando o
  eliminando recursión, y el lenguaje generado no cambia.

**Todo par de producciones que comparte prefijo izquierdo genera un conflicto LL(1). No todo par
que comparte prefijo izquierdo es ambiguo.** Si los dos lados derivan lenguajes disjuntos, no hay
ninguna cadena con dos árboles.

Un criterio suficiente y barato para probar disyunción: **si los rangos de longitud derivable no
se solapan, los lenguajes son disjuntos.** El script lo calcula:

```bash
$ python3 herramientas/primeros.py gramatica/minijava-base.g --longitudes VarEncadenada MetodoEncadenado
<VarEncadenada>: entre 2 y 2 tokens
<MetodoEncadenado>: entre 4 y ∞ tokens
Los rangos NO se solapan: … NO son ambiguos entre si.
```

Aplicado a los pares sospechosos de esta gramática:

| Par | ¿Rangos disjuntos? | Veredicto |
|---|---|---|
| `<VarEncadenada>` / `<MetodoEncadenado>` | 2..2 vs 4..∞ | **no ambiguo** — sólo prefijo común |
| `<AccesoVar>` / `<LlamadaMetodo>` | 1..1 vs 3..∞ | **no ambiguo** — sólo prefijo común |
| `<CrearArreglo>` / `<LlamadaConstructor>` | se solapan | **no ambiguo** — ver abajo |
| `<Atributo>` / `<Metodo>` | se solapan | **no ambiguo** — ver abajo |
| `<Asignacion>` / `<Llamada>` | se solapan | **AMBIGUO** — cuerpos idénticos |
| las dos producciones de `<If>` | se solapan | **AMBIGUO** — *dangling else* |

`<VarEncadenada>` deriva **exactamente** la cadena `. idMetVar` (dos tokens, sin alternativas),
mientras que `<MetodoEncadenado>` es `. idMetVar <ArgsActuales>` y `<ArgsActuales>` **no es
vaciable** (siempre aporta al menos `(` y `)`, el script lo confirma). Ninguna cadena puede estar
en los dos lenguajes.

Para los dos pares cuyos rangos sí se solapan, el argumento es posicional en vez de por longitud:

- `<CrearArreglo>` / `<LlamadaConstructor>`: comparten `new` y el tipo, pero
  `<DimensionesConTamanio>` siempre arranca con `[` y `<ArgsActuales>` siempre con `(`. Las
  cadenas difieren en el token inmediatamente posterior al tipo.
- `<Atributo>` / `<Metodo>`: comparten `<Tipo> idMetVar`, pero siguen con `;` y con `(`
  respectivamente.

**Conclusión: de los 16 conflictos de la gramática base, sólo 2 son ambigüedades reales**
(`<Asignacion>`/`<Llamada>` y el *dangling else*). Los otros 14 son conflictos de predicción, se
arreglan mecánicamente y el lenguaje reconocido no cambia en absoluto.

## 1. Diagnóstico de la gramática base

El script encuentra **16 conflictos** repartidos en **10 no terminales**. Coinciden uno a uno
con el análisis hecho a mano:

| # | No terminal | Tipo de defecto | Intersección de Primeros |
|---|---|---|---|
| 1 | `<Miembro>` | prefijo común profundo | `{boolean, char, int, idClase, idGen}` |
| 2 | `<ListaArgsFormales>` | recursión a izquierda | `{boolean, char, int, idClase, idGen}` |
| 3 | `<Sentencia>` | **ambigüedad** | todos los Primeros de `<Expresion>` |
| 4 | `<If>` | prefijo común (+ *dangling else*) | `{if}` |
| 5 | `<Expresion>` | prefijo común | todos los Primeros de `<ExpresionCompuesta>` |
| 6 | `<ExpresionCompuesta>` | recursión a izquierda | todos los Primeros de `<ExpresionBasica>` |
| 7-12 | `<Referencia>` | recursión a izquierda (×3 alternativas ⇒ 6 pares) | todos los Primeros de `<Primario>` |
| 13 | `<Primario>` | prefijo común | `{idMetVar}` |
| 14 | `<Primario>` | prefijo común profundo | `{new}` |
| 15 | `<DimensionesConTamanio>` | prefijo común | `{[}` |
| 16 | `<ListaExps>` | prefijo común | todos los Primeros de `<Expresion>` |

## 2. Eliminación de recursión a izquierda

Esquema de Clase 4, slide 33:

```
A → Aα₁ | Aα₂ | … | Aαₙ | β₁ | β₂ | … | βₘ

se transforma en

A → β₁R | β₂R | … | βₘR
R → α₁R | α₂R | … | αₙR | ε
```

### R1. `<ListaArgsFormales>`

**1 — Original**

```
<ListaArgsFormales> ::= <ArgFormal>
<ListaArgsFormales> ::= <ListaArgsFormales> , <ArgFormal>
```

**2 — Defecto.** Recursión a izquierda directa. La segunda producción empieza con el propio
`<ListaArgsFormales>`, así que Primeros de ambas alternativas es `Primeros(<ArgFormal>)` =
`Primeros(<Tipo>)` = `{boolean, char, int, idClase, idGen}`. Viendo `int` el parser no sabe si
está ante el primer argumento o ante el arranque de una lista más larga, y como la recursión es
a izquierda tampoco tiene forma de saber cuándo cortarla (Clase 4, slide 22).

**3 — Transformación.** Esquema directo con `α₁ = , <ArgFormal>` y `β₁ = <ArgFormal>`.

**4 — Resultado**

```
<ListaArgsFormales> ::= <ArgFormal> <RestoListaArgsFormales>
<RestoListaArgsFormales> ::= , <ArgFormal> <RestoListaArgsFormales> | ε
```

`Primeros(, <ArgFormal> <RestoListaArgsFormales>) = {,}` y `Primeros(ε) = {ε}`. Disjuntos. ✔

### R2. `<ExpresionCompuesta>`

**1 — Original**

```
<ExpresionCompuesta> ::= <ExpresionCompuesta> <OperadorBinario> <ExpresionBasica>
<ExpresionCompuesta> ::= <ExpresionBasica>
```

**2 — Defecto.** Recursión a izquierda directa. Es el ejemplo textual de la Clase 4 (slides
22-24) con `E → E + T | T`: ambas alternativas tienen los Primeros de `<ExpresionBasica>`.

**3 — Transformación.** `α₁ = <OperadorBinario> <ExpresionBasica>`, `β₁ = <ExpresionBasica>`.

**4 — Resultado**

```
<ExpresionCompuesta> ::= <ExpresionBasica> <RestoExpresionCompuesta>
<RestoExpresionCompuesta> ::= <OperadorBinario> <ExpresionBasica> <RestoExpresionCompuesta> | ε
```

`Primeros(<OperadorBinario> …)` = los 13 operadores binarios, disjunto de `{ε}`. ✔

> **Nota sobre precedencia.** La gramática original **ya** era plana: `<OperadorBinario>` es una
> alternancia de los 13 operadores sin niveles, así que `a + b * c` y `a * b + c` se derivan con
> la misma forma. La transformación no pierde precedencia porque no había ninguna que perder.
> La precedencia y la asociatividad quedan, como en la gramática original, fuera del sintáctico.

### R3. `<Referencia>`

**1 — Original**

```
<Referencia> ::= <Primario>
<Referencia> ::= <Referencia> <VarEncadenada>
<Referencia> ::= <Referencia> <MetodoEncadenado>
<Referencia> ::= <Referencia> <AccesoArreglo>
```

**2 — Defecto.** Recursión a izquierda directa con **tres** alternativas recursivas: las cuatro
producciones tienen los Primeros de `<Primario>`, lo que da los 6 pares en conflicto (#7 a #12).

**3 — Transformación.** Esquema directo con `β₁ = <Primario>` y
`α₁ = <VarEncadenada>`, `α₂ = <MetodoEncadenado>`, `α₃ = <AccesoArreglo>`.

**4 — Resultado intermedio, todavía con un defecto nuevo**

```
<Referencia> ::= <Primario> <RestoReferencia>
<RestoReferencia> ::= <VarEncadenada> <RestoReferencia>
                    | <MetodoEncadenado> <RestoReferencia>
                    | <AccesoArreglo> <RestoReferencia>
                    | ε
```

`Primeros(<VarEncadenada>) = {.}` y `Primeros(<MetodoEncadenado>) = {.}`: **siguen chocando**.
Es exactamente lo que anticipa la Clase 4, slide 38 — *«al eliminar la recursión a izquierda
podemos generar nuevas producciones no factorizadas»*. Por eso el orden es recursión primero.

**5 — Factorización del defecto nuevo.** `<VarEncadenada> ::= . idMetVar` y
`<MetodoEncadenado> ::= . idMetVar <ArgsActuales>` comparten *todo* el prefijo: sólo se
distinguen por si después viene un `(`. Se fusionan en un único `<Encadenado>`, y la disyuntiva
que queda va a un no terminal dedicado (el porqué de que sea dedicado está en 3.bis.2).

```
<Referencia> ::= <Primario> <RestoReferencia>
<RestoReferencia> ::= <Encadenado> <RestoReferencia>
                    | <AccesoArreglo> <RestoReferencia>
                    | ε
<Encadenado> ::= . idMetVar <EncadenadoAtributoOMetodo>
<EncadenadoAtributoOMetodo> ::= <ArgsActuales> | ε
```

`Primeros(<Encadenado>) = {.}`, `Primeros(<AccesoArreglo>) = {[}`, `{ε}`. Disjuntos. ✔

> **Costo semántico, declarado.** Se pierde la distinción entre `<VarEncadenada>` (acceso a
> atributo) y `<MetodoEncadenado>` (llamada a método encadenada). Es una pérdida **inevitable y
> honesta**: sintácticamente son indistinguibles hasta ver el token siguiente, y el propio
> parser recupera la distinción al decidir si `<EncadenadoAtributoOMetodo>` derivó a vacío o no.

## 3. Factorización a izquierda

Esquema de Clase 4, slide 18:

```
X → αβ₁ | αβ₂ | … | αβₙ | γ₁ | … | γₘ

se transforma en

X → αR | γ₁ | … | γₘ
R → β₁ | β₂ | … | βₙ
```

### F1. `<Expresion>`

**1 — Original**

```
<Expresion> ::= <ExpresionCompuesta> <OperadorAsignacion> <ExpresionCompuesta>
<Expresion> ::= <ExpresionCompuesta>
```

**2 — Defecto.** Prefijo común directo `α = <ExpresionCompuesta>`. Con `x` en la entrada el
parser no sabe si va a ser `x = …` o `x` a secas hasta llegar al `=`, que puede estar
arbitrariamente lejos.

**3 — Transformación.** Factorización directa, con `β₁ = <OperadorAsignacion> <ExpresionCompuesta>`
y `β₂ = ε`.

**4 — Resultado**

```
<Expresion> ::= <ExpresionCompuesta> <RestoExpresion>
<RestoExpresion> ::= <OperadorAsignacion> <ExpresionCompuesta> | ε
```

`{=}` vs `{ε}`. Disjuntos. ✔

### F2. `<Sentencia>` — la ambigüedad de la gramática original

**1 — Original**

```
<Sentencia> ::= <Asignacion> ;
<Sentencia> ::= <Llamada> ;
…
<Asignacion> ::= <Expresion>
<Llamada> ::= <Expresion>
```

**2 — Defecto.** No es un prefijo común: es **la misma producción escrita dos veces**.
`<Asignacion>` y `<Llamada>` tienen cuerpos idénticos, así que toda sentencia-expresión tiene
**dos árboles de derivación distintos** — la gramática de la cátedra es ambigua acá (Clase 4,
slide 46). No hay factorización que lo arregle: hay que elegir una de las dos.

**3 — Transformación.** Se colapsan en una única alternativa. El no terminal resultante se llama
`<SentenciaExpresion>`, que es lo que efectivamente es.

**4 — Resultado**

```
<Sentencia> ::= ;
              | <SentenciaExpresion> ;
              | <VarLocal> ;
              | <Return> ;
              | <If>
              | <While>
              | <Bloque>
<SentenciaExpresion> ::= <Expresion>
```

Primeros: `{;}`, los de `<Expresion>`, `{var}`, `{return}`, `{if}`, `{while}`, `{`{`}`.
Disjuntos. ✔

> **Decisión, para el informe.** Distinguir una asignación de una llamada **no es un problema
> sintáctico**: `x = 5` y `f()` tienen la misma forma de expresión-seguida-de-`;`. Es el
> analizador semántico el que, mirando la expresión ya construida, decide si es una asignación
> válida, una llamada válida, o ninguna de las dos (`5 = x;` pasa el sintáctico). Colapsar las
> dos alternativas no achica el lenguaje reconocido: lo genera exactamente igual, con un solo
> árbol por sentencia en lugar de dos.

### F3. `<If>` — factorización y *dangling else*

**1 — Original**

```
<If> ::= if ( <Expresion> ) <Sentencia>
<If> ::= if ( <Expresion> ) <Sentencia> else <Sentencia>
```

**2 — Defecto.** Prefijo común `α = if ( <Expresion> ) <Sentencia>`, que además es larguísimo:
el parser tendría que atravesar una sentencia entera antes de saber qué producción eligió.

**3 — Transformación.** Factorización directa con `β₁ = else <Sentencia>` y `β₂ = ε`.

**4 — Resultado**

```
<If> ::= if ( <Expresion> ) <Sentencia> <ElseOpcional>
<ElseOpcional> ::= else <Sentencia> | ε
```

`{else}` vs `{ε}`. Disjuntos, y el script lo confirma. ✔

**5 — Lo que la factorización NO arregla: el *dangling else*.** En

```java
if (a) if (b) s1; else s2;
```

el `else` puede ligar al `if` interno o al externo: **dos árboles, misma cadena**. La
factorización volvió los Primeros disjuntos pero la ambigüedad sigue ahí, escondida en que
`<ElseOpcional>` puede derivar a `ε` *y* tener un `else` en lo que le sigue.

**Resolución adoptada:** una **regla de implementación**, que es la segunda de las dos
soluciones que ofrece Clase 4, slide 49 (*«agregar reglas especiales (precedencia /
asociatividad) en la implementación del analizador sintáctico»*). El Esquema Simple toma la rama
`else` apenas ve el token, y como el `if` interno es el que está ejecutándose en ese momento, el
`else` liga al `if` más cercano — **que es exactamente la regla de Java**. No hace falta tocar
la gramática: alcanza con documentar que el orden de las ramas del `if/else if` del método
`<ElseOpcional>` es significativo.

### F4. `<DimensionesConTamanio>`

**1 — Original**

```
<DimensionesConTamanio> ::= [ <Expresion> ] <DimensionesConTamanio> | [ <Expresion> ]
```

**2 — Defecto.** Prefijo común `α = [ <Expresion> ]`, intersección `{[}`.

**3 — Transformación.** Factorización directa. **Cuidado:** la regla original exige *al menos
una* dimensión, así que la parte repetible no puede volverse `<DimensionesConTamanio> ::= … | ε`
— eso agrandaría el lenguaje. Va en un no terminal opcional aparte.

**4 — Resultado**

```
<DimensionesConTamanio> ::= [ <Expresion> ] <DimensionesConTamanioOpcional>
<DimensionesConTamanioOpcional> ::= <DimensionesConTamanio> | ε
```

`{[}` vs `{ε}`. Disjuntos. ✔

### F5. `<ListaExps>`

**1 — Original**

```
<ListaExps> ::= <Expresion>
<ListaExps> ::= <Expresion> , <ListaExps>
```

**2 — Defecto.** Prefijo común `α = <Expresion>`.

**3 — Transformación.** Factorización directa, con la misma forma que se le dio a `R1` para que
las dos listas de la gramática se lean igual.

**4 — Resultado**

```
<ListaExps> ::= <Expresion> <RestoListaExps>
<RestoListaExps> ::= , <Expresion> <RestoListaExps> | ε
```

`{,}` vs `{ε}`. Disjuntos. ✔

### F6. `<Miembro>` — factorización profunda

**1 — Original**

```
<Miembro> ::= <Atributo> | <Metodo> | <Constructor>
<Atributo> ::= <Tipo> idMetVar ;
<Metodo> ::= <ModificadorOpcional> <TipoMetodo> idMetVar <ArgsFormales> <Bloque>
<Constructor> ::= public idClase <ArgsFormales> <Bloque>
<ModificadorOpcional> ::= static | ε
<TipoMetodo> ::= <Tipo> | void
```

**2 — Defecto.** Prefijo común **no directo**: mirando sólo las tres alternativas de
`<Miembro>` no se ve nada compartido, pero al bajar a sus producciones aparece que
`<Atributo>` y `<Metodo>` empiezan los dos con `<Tipo>`.

```
Primeros(<Atributo>)    = Primeros(<Tipo>) = {boolean, char, int, idClase, idGen}
Primeros(<Metodo>)      = {static} ∪ {void} ∪ Primeros(<Tipo>)
Primeros(<Constructor>) = {public}

Primeros(<Atributo>) ∩ Primeros(<Metodo>) = {boolean, char, int, idClase, idGen} ≠ ∅
```

Ante `int x` el parser no sabe si sigue un `;` (atributo) o un `(` (método), y eso está a dos
tokens de distancia.

**3 — Transformación.** Factorización a izquierda **profunda** (Clase 4, slide 20): «subir» las
producciones de `<Atributo>` y `<Metodo>` hasta `<Miembro>`, expandir `<ModificadorOpcional>` y
`<TipoMetodo>` para exponer el prefijo, y recién ahí hacer factor común.

Expandiendo:

```
<Miembro> ::= <Tipo> idMetVar ;                                      (era Atributo)
            | static <TipoMetodo> idMetVar <ArgsFormales> <Bloque>   (Metodo, con static)
            | <Tipo> idMetVar <ArgsFormales> <Bloque>                (Metodo sin static, tipo)
            | void idMetVar <ArgsFormales> <Bloque>                  (Metodo sin static, void)
            | public idClase <ArgsFormales> <Bloque>                 (Constructor)
```

Ahora el prefijo común `α = <Tipo> idMetVar` sí es directo, entre la 1ª y la 3ª.

**4 — Resultado**

```
<Miembro> ::= <Tipo> idMetVar <AtributoOMetodo>
            | static <TipoMetodo> <Metodo>
            | void <Metodo>
            | <Constructor>

<AtributoOMetodo> ::= ;                   (era <Atributo>)
                    | <CuerpoDeMetodo>    (era <Metodo>)

<Metodo> ::= idMetVar <CuerpoDeMetodo>
<CuerpoDeMetodo> ::= <ArgsFormales> <Bloque>
<Constructor> ::= public idClase <CuerpoDeMetodo>
```

Primeros de `<Miembro>`: `{boolean, char, int, idClase, idGen}`, `{static}`, `{void}`,
`{public}`. Disjuntos. ✔
Primeros de `<AtributoOMetodo>`: `{;}` vs `{(}`. Disjuntos. ✔

> `<Constructor>` **sobrevive como no terminal propio** porque `public` lo distingue solo: no
> hizo falta fusionarlo. `<Atributo>`, en cambio, se fusiona con `<Metodo>` hasta el token que los
> separa, y esa decisión queda visible en el nombre `<AtributoOMetodo>`.
>
> Se extraen dos NT compartidos: `<CuerpoDeMetodo>` (argumentos + bloque), que es la misma
> construcción en método, método `static` y constructor; y **`<Metodo>`, que sobrevive** compartido
> por la rama `static` y la `void`, así que el semántico tiene un único lugar donde construir el
> nodo de método para esas dos formas. La tercera forma ya consumió el `idMetVar` al factorizar
> contra el atributo y llega por `<AtributoOMetodo>`.

### F7. `<Primario>` — el prefijo `idMetVar`

**1 — Original**

```
<Primario> ::= <AccesoVar> | <LlamadaMetodo> | …
<AccesoVar> ::= idMetVar
<LlamadaMetodo> ::= idMetVar <ArgsActuales>
```

**2 — Defecto.** Prefijo común no directo, intersección `{idMetVar}`.

**3 — Transformación.** Misma situación que `<VarEncadenada>` / `<MetodoEncadenado>` en R3, y se
resuelve igual: fusionar y decidir por si sigue un `(`. La disyuntiva tiene la misma forma que la
de R3 pero **no** se comparte con ella, porque su `ε` significa otra cosa (3.bis.2).

**4 — Resultado**

```
<AccesoVarOLlamadaMetodo> ::= idMetVar <AccesoOLlamada>
<AccesoOLlamada> ::= <ArgsActuales> | ε
```

Primeros de `<AccesoOLlamada>`: `{(}` vs `{ε}`. Disjuntos. ✔

### F8. `<Primario>` — el prefijo `new`

**1 — Original**

```
<CrearArreglo> ::= new <TipoBase> <DimensionesConTamanio>
<LlamadaConstructor> ::= new <TipoReferencia> <ArgsActuales>
<TipoBase> ::= <TipoPrimitivo> | <TipoReferencia> | idGen
```

**2 — Defecto.** Prefijo común no directo `{new}`, y **a dos niveles**: no alcanza con factorizar
el `new`, porque `<TipoBase>` puede derivar a `<TipoReferencia>`, así que `new C` sigue sin
decidirse. El token que desempata es el que viene *después* del tipo: `[` → arreglo, `(` →
constructor.

**3 — Transformación.** Factorizar `new`, después expandir `<TipoBase>` para separar los casos
que **sí** se deciden con un token (`<TipoPrimitivo>` e `idGen` sólo pueden ser arreglo), y
factorizar una segunda vez el caso `idClase`.

**4 — Resultado**

```
<Creacion> ::= new <ArregloOInstancia>

<ArregloOInstancia> ::= <TipoPrimitivo> <DimensionesConTamanio>
                      | idGen <DimensionesConTamanio>
                      | idClase <TipoGenericoOpcional> <ArregloOInstanciaDeClase>

<ArregloOInstanciaDeClase> ::= <DimensionesConTamanio>   (era <CrearArreglo>)
                             | <ArgsActuales>            (era <LlamadaConstructor>)
```

Primeros de `<ArregloOInstancia>`: `{boolean, char, int}`, `{idGen}`, `{idClase}`. Disjuntos. ✔
Primeros de `<ArregloOInstanciaDeClase>`: `{[}` vs `{(}`. Disjuntos. ✔

> `new int[3]` y `new T[3]` se resuelven en el primer token después de `new`; sólo
> `new C…` necesita el segundo nivel. La factorización se hizo **sólo donde hacía falta**, que
> es lo que pide la slide 40.

### F9. `<Primario>` — el resto

Las alternativas restantes ya eran disjuntas y **no se tocan**: `this` → `{this}`,
`stringLiteral` → `{stringLiteral}`, `<ExpresionParentizada>` → `{(}`,
`<LlamadaMetodoEstatico>` → `{idClase}`. Nótese que `idClase` no choca con `<Creacion>` porque
esta última está toda bajo `new`.

## 3.bis Principio de diseño: insertar el no terminal, no fusionar

Cuando dos producciones comparten prefijo, hay dos formas de factorizar:

- **Fusionar** — meter los cuerpos de los NT involucrados adentro del padre y borrar los NT
  originales. Menos no terminales, pero el punto donde se decide *qué construcción era* deja de
  tener nombre, y el analizador semántico de la Etapa 3 pierde el gancho donde colgar el nodo
  del AST.
- **Insertar un no terminal** — factorizar el prefijo y poner la disyuntiva restante en un NT
  **nuevo y dedicado**, cuyo nombre diga exactamente qué se está decidiendo. Un no terminal más,
  pero ese NT se traduce a un método del parser que **es** el punto de decisión semántica.

**Se adopta siempre la segunda.** Es la lectura operativa de la advertencia de Clase 4, slide 40
(*«al modificarla, al analizador semántico le puede llegar a ser más difícil entender el
programa»*): la gramática se puede transformar sin perder significado si cada decisión que se
posterga queda con nombre propio.

Dos corolarios que se aplicaron:

1. **Un NT de decisión por contexto, aunque la forma coincida.** Dos disyuntivas con la misma
   pinta (`<ArgsActuales> | ε`) pero distinto significado **no** se comparten.
2. **Compartir sí, cuando es literalmente la misma construcción.** `<CuerpoDeMetodo>`
   (`<ArgsFormales> <Bloque>`) se usa desde método, constructor y método `static` porque en los
   tres casos es lo mismo.

### 3.bis.1 Auditoría de puntos de decisión compartidos

Se listaron todos los NT **vaciables** usados desde más de un padre, porque son justamente
aquellos donde el «no hago nada» del Esquema Simple significa algo distinto según el contexto:

| NT vaciable compartido | Padres | Veredicto |
|---|---|---|
| `<ListaClases>`, `<ListaMiembros>`, `<ListaSentencias>`, `<ListaMetodosInterfaz>`, `<Resto…>` | se incluyen a sí mismos | recursión propia, legítimo |
| `<GenericidadOpcional>` | Clase, Interfaz | misma construcción (declaración de parámetro de tipo) |
| `<DimensionesOpcionales>` | Tipo, sí misma | legítimo |
| «`<ArgsActuales>` o nada» | encadenado y acceso a variable | **dos construcciones distintas: van separadas (3.bis.2)** |
| el genérico | posición de tipo y posición de instanciación | **se separan por el diamante (3.bis.3)** |

### 3.bis.2 «`<ArgsActuales>` o nada» son dos no terminales, no uno

La disyuntiva «argumentos actuales, o nada» aparece en dos lugares con la misma forma:

```
<Encadenado> ::= . idMetVar ( <ArgsActuales> | ε )
<AccesoVarOLlamadaMetodo> ::= idMetVar ( <ArgsActuales> | ε )
```

**Alternativa descartada:** un único NT compartido. Parece buena abstracción y es al revés, porque
su `ε` significa dos cosas distintas:

- desde el encadenado: acceso a un **atributo de otro objeto**;
- desde el acceso a variable: **variable local, parámetro o atributo propio**.

Son dos nodos de AST distintos, con reglas de resolución de nombres distintas. Un único método del
parser tendría que preguntarse quién lo llamó para saber qué construir: exactamente el acoplamiento
que la slide 40 advierte. Van dos NT dedicados:

```
<Encadenado> ::= . idMetVar <EncadenadoAtributoOMetodo>
<EncadenadoAtributoOMetodo> ::= <ArgsActuales> | ε

<AccesoVarOLlamadaMetodo> ::= idMetVar <AccesoOLlamada>
<AccesoOLlamada> ::= <ArgsActuales> | ε
```

Cuesta un no terminal más y hace que los cuatro NT de la gramática original sean recuperables sin
ambigüedad desde el nombre del método que decide. Con el mismo criterio, `<ArregloOInstancia>` y
`<ArregloOInstanciaDeClase>` llevan nombres que dicen qué deciden, y `<CuerpoDeMetodo>` se extrajo
porque sí es literalmente la misma construcción en los tres lugares donde aparece.

### 3.bis.3 El genérico en posición de tipo y en posición de instanciación

El genérico se usa desde `<TipoReferencia>` (posición de **tipo**: `List<Persona> x;`) y desde
`<ArregloOInstancia>` (posición de **instanciación**: `new List<Persona>()`). Sin logros es la misma
construcción y compartirlo sería correcto.

**Deja de serlo con Genericidad Avanzada E2**, que dice: *«La notación Diamante solo puede utilizarse
al instanciar una Clase Genérica»*. O sea que `<>` es válido en la segunda posición y no en la
primera. Compartir el NT obligaría a aceptar `List<> x;` y rechazarlo en el semántico; separándolos,
la restricción del logro queda **impuesta por la gramática**. Se aplica en la sección de logros.

### 3.bis.4 Trampa evitada: la transformación «natural» de `<Expresion>` agranda el lenguaje

La factorización F1 dio:

```
<RestoExpresion> ::= <OperadorAsignacion> <ExpresionCompuesta> | ε
```

La versión que sale sola al escribirla —y que además queda más prolija— es:

```
<RestoExpresion> ::= <OperadorAsignacion> <Expresion> | ε        ← INCORRECTA
```

**Cambia el lenguaje.** La regla original es
`<Expresion> ::= <ExpresionCompuesta> <OperadorAsignacion> <ExpresionCompuesta>`: el lado derecho
del `=` es una `<ExpresionCompuesta>`, **no** una `<Expresion>`. Es decir, en MiniJava la
asignación **no es asociativa** y `a = b = c` **no es un programa válido**. Poner `<Expresion>`
a la derecha la volvería asociativa a derecha y aceptaría programas que la cátedra no define.

Una transformación sólo es válida si **preserva exactamente** el lenguaje generado (Clase 3,
slide 42: *«adaptarla sin cambiar el lenguaje que genera»*). Mismo cuidado en F4, donde
`<DimensionesConTamanio>` exige al menos una dimensión y por eso la parte repetible fue a un NT
opcional aparte en vez de volverse `… | ε`.

## 4. Estado del núcleo

Con R1-R3 y F1-F9 el **núcleo de la gramática (sin logros) es LL(1)**, verificado a mano y por
el script:

```
$ python3 herramientas/primeros.py gramatica/minijava-ll1.g
--- 67 no terminales, 135 producciones, 51 terminales ---
ES LL(1): toda produccion de todo no terminal tiene Primeros disjuntos.
```

Contra la base: `60 no terminales, 126 producciones` y `16 conflictos`. El costo total de volver
la gramática LL(1) fue **+7 no terminales y +9 producciones** — barato, y concentrado en los
`<Resto…>` y en los puntos de decisión insertados, que el Esquema Simple traduce a un `if/else`
cada uno.

## 5. Extensiones por logro

Cada logro se transforma con el mismo procedimiento de cuatro pasos. Lo que cambia respecto del
núcleo es que acá los conflictos **no vienen de la gramática de la cátedra: los crea el logro**,
y hay que resolverlos sin romper lo que ya andaba.

### 5.0 Cada logro aterriza en su propio lugar

La gramática es **una sola**: no hay configuraciones ni producciones que aparezcan y
desaparezcan. Lo que sí se mantiene, porque es lo que la
deja legible y lo que va a facilitar la Etapa 3, es que cada logro aterrice en un lugar
identificable:

> **Un logro agrega una capa o unas alternativas propias; no reescribe las reglas de al lado.**

Consecuencia de diseño concreta: **ningún logro se apoya en un no terminal que introdujo otro**.
Cuando dos necesitan una construcción parecida, cada uno lleva la suya (ver 5.2), porque un no
terminal compartido entre dos construcciones distintas obliga al semántico a desambiguar en tiempo
de ejecución lo que la gramática podía separar.

### 5.1 Visibilidad Mejorada E2

**Defecto que crea.** `public`/`private` pasan a poder encabezar cualquier miembro. El `public`
del `<Constructor>` original queda absorbido por `<VisibilidadOpcional>`, y el constructor se
vuelve `idClase <ArgsFormales> <Bloque>` — que ahora **compite con `<Tipo> idMetVar …`** por el
token `idClase`: `C(){}` contra `C x;`.

**Transformación.** Factorizar la visibilidad al frente y, adentro, factorizar el `idClase`. El
desempate es el token que sigue: `(` es constructor, cualquier otro abre un tipo. La visibilidad
puede ir antes o después de `static`, como en Java, pero a **un solo lado**: eso acepta los dos
órdenes sin dejar repetir el modificador.

```
<Miembro> ::= <Visibilidad> <MiembroTrasVisibilidad>
            | static <VisibilidadOpcional> <TipoMetodo> <Metodo>
            | <MiembroSinModificadores>
<MiembroTrasVisibilidad> ::= static <TipoMetodo> <Metodo> | <MiembroSinModificadores>
<Visibilidad> ::= public | private
<VisibilidadOpcional> ::= <Visibilidad> | ε

<MiembroSinModificadores> ::= void <Metodo>
                            | <TipoPrimitivo> <DimensionesOpcionales> idMetVar <AtributoOMetodo>
                            | idGen <DimensionesOpcionales> idMetVar <AtributoOMetodo>
                            | idClase <MiembroDesdeIdClase>

<MiembroDesdeIdClase> ::= <CuerpoDeMetodo>
                        | <TipoGenericoOpcional> <DimensionesOpcionales> idMetVar <AtributoOMetodo>
```

`<Miembro>`: `{public, private}`, `{static}` y los de `<MiembroSinModificadores>` — disjuntos ✔
`<MiembroTrasVisibilidad>`: `{static}` contra `{void, boolean, char, int, float, idGen, idClase}` — disjuntos ✔
`<MiembroSinModificadores>`: `{void}`, `{boolean,char,int,float}`, `{idGen}`, `{idClase}` — disjuntos ✔
`<MiembroDesdeIdClase>`: `{(}` contra `{<, [, idMetVar}` — disjuntos ✔

> **Decisión: en `<MetodoInterfaz>` se admite `public` y no `private`.** El logro habla de
> «Métodos, Atributos y Constructores», y `<MetodoInterfaz>` es un no terminal aparte de
> `<Miembro>`, así que el caso cae bajo la regla general: lo no especificado se comporta como en
> Java. Verificado con `javac`: `interface C { private int m(); }` es un error —*missing method
> body, or declare abstract*—, mientras que `public int m();` compila. Como en MiniJava los métodos
> de interfaz **son siempre abstractos** (terminan en `;` y ningún logro les agrega cuerpo), un
> `private` ahí sería por construcción siempre la forma que Java rechaza.
>
> ```
> <MetodoInterfaz> ::= <PublicOpcional> <TipoMetodo> idMetVar <ArgsFormales> ;
> <PublicOpcional> ::= public | ε
> ```

### 5.2 Atributos Inicializados y Variables locales Clásicas E2

Los dos agregan un `= <ExpresionCompuesta>` opcional. **Tentación:** un único
`<InicializacionOpcional>` compartido. **Está mal**, por 5.0: son construcciones distintas —un
atributo de clase y una variable local— y el semántico las trata distinto. Van dos no terminales
dedicados:

```
<InicializacionDeAtributo>   ::= = <ExpresionCompuesta> | ε
<InicializacionDeDeclarador> ::= = <ExpresionCompuesta> | ε
```

Es el mismo principio de 3.bis.

**Defecto que crea Variables locales Clásicas.** Una declaración local pasa a poder empezar con `idClase`
(`C x;`), y una sentencia-expresión también (`C.m();`, vía `<LlamadaMetodoEstatico>`). Chocan.

**Transformación.** Factorizar el `idClase` en `<Sentencia>`. El desempate es de un token: `.`
significa expresión, cualquier otro significa declaración.

```
<Sentencia> ::= …
              | <ExpresionSinIdClase> ;
              | <TipoPrimitivo> <DimensionesOpcionales> <ListaDeclaradores> ;
              | idGen <DimensionesOpcionales> <ListaDeclaradores> ;
              | idClase <SentenciaDesdeIdClase>

<SentenciaDesdeIdClase> ::= <TipoGenericoOpcional> <DimensionesOpcionales> <ListaDeclaradores> ;
                          | . idMetVar <ArgsActuales> <ColaDeExpresionDesdePrimario> ;
```

La rama de expresión necesita retomar una expresión **ya empezada**. Escribirla a mano
duplicaría media jerarquía de expresiones. En su lugar se insertan **tres** no terminales que
capturan las colas compartidas:

```
<ExpresionSinIdClase> ::= <OperadorUnario> <Operando> <ColaTrasOperando>
                        | <Primitivo> <ColaTrasOperando>
                        | <PrimarioSinIdClase> <ColaDeExpresionDesdePrimario>

<ColaTrasOperando> ::= <RestoExpresionCompuesta> <RestoExpresion>
<ColaDeExpresionDesdePrimario> ::= <RestoReferencia> <PosfijoOpcional> <ColaTrasOperando>
<PrimarioSinIdClase> ::= this | stringLiteral | <ExpresionParentizada>
                       | <AccesoVarOLlamadaMetodo> | <Creacion>
```

y `<Primario> ::= <LlamadaMetodoEstatico> | <PrimarioSinIdClase>`, que es la única forma de
`<Primario>` que empieza con `idClase`. **Sólo se duplican las cabezas**: todas las colas
(`<RestoReferencia>`, `<RestoExpresionCompuesta>`, `<RestoExpresion>`, `<PosfijoOpcional>`) se comparten con la jerarquía normal, así que no hay lógica repetida y un
cambio futuro en las expresiones no hay que espejarlo.

> **Alternativa descartada:** dar al parser **dos** tokens de lookahead sólo en este punto (mirar
> si después del `idClase` viene un `.`). Resuelve lo mismo con cero no terminales nuevos, pero
> despega la implementación del Esquema Simple de Clase 5, que es de un solo token. Se descartó
> por eso: el costo de la solución LL(1) pura son 3 no terminales sin lógica duplicada, que es
> barato.

### 5.3 Fors! E2

**Transformación.** Un `<For>` nuevo como alternativa de `<Sentencia>`. La variable de control se
declara con `var` y nada más: los logros son independientes entre sí, así que el encabezado no
toma el declarador tipado de Variables locales Clásicas —lo respondió el profesor—. El encabezado
empieza entonces con `;`, con `var` o con una expresión, y las tres son disjuntas: ninguna
expresión empieza con `var` ni con `;`.

El desempate entre el `for` estándar y el de iteradores es **de un solo token** y está justo
después de `var x`: `:` es de iteradores, `=` es estándar.

```
<For> ::= for ( <EncabezadoFor> ) <Sentencia>

<EncabezadoFor> ::= ; <ExpresionOpcional> ; <ExpresionOpcional>
                  | var idMetVar <EncabezadoForTrasVar>
                  | <Expresion> <ClausulasFinalesFor>

<EncabezadoForTrasVar> ::= : <Expresion>
                         | = <ExpresionCompuesta> <ClausulasFinalesFor>

<ClausulasFinalesFor> ::= ; <ExpresionOpcional> ; <ExpresionOpcional>
```

`<ClausulasFinalesFor>` con una sola `<ExpresionOpcional>` por separador es la lectura literal de
*«se restringe a una sola sentencia/expresión por separador»*.

### 5.4 Operador Ternario E2

**Transformación.** Una capa nueva entre la asignación y los operadores binarios, con la
precedencia de Java: la asignación es más baja que el ternario, y el ternario más baja que los
binarios.

```
<Expresion> ::= <ExpresionCompuesta> <RestoExpresion>
<RestoExpresion> ::= <OperadorAsignacion> <ExpresionCondicional> | <RestoCondicional>

<ExpresionCondicional> ::= <ExpresionCompuesta> <RestoCondicional>
<RestoCondicional> ::= ? <Expresion> : <ExpresionCondicional> | ε
```

`<Expresion>` conserva la forma de F1: el lado izquierdo del `=` es una `<ExpresionCompuesta>`, como
en la gramática original, así que `a ? b : c = d` no deriva —y `javac` tampoco lo acepta—. Después
del primer operando viene un `=` o el ternario. El lado derecho del `=` es una
`<ExpresionCondicional>`: admite el ternario, que es lo que pide el ejemplo del logro
(`esMayor = (edad >= 18) ? true : false;`), y no admite otro `=`, así que `a = b = c` no deriva.
Los cuatro inicializadores (`<VarLocal>`, `<InicializacionDeAtributo>`, `<InicializacionDeDeclarador>`
y el del `for`) pasan de `<ExpresionCompuesta>` a `<ExpresionCondicional>` por el mismo motivo.

Los operandos del ternario son los del JLS §15.25: el del medio es una `<Expresion>` completa y el de
la derecha una `<ExpresionCondicional>`, así que `a ? b = 1 : c` es válido y `a ? b : c = d` no. Es
asociativo a **derecha** (`a ? b : c ? d : e` es `a ? b : (c ? d : e)`), porque la rama del `:` es
otra `<ExpresionCondicional>`, con su propio `<RestoCondicional>`.

**Por qué la capa no molesta.** Sin el `?`, `<RestoCondicional>` quedaría **sólo con `ε`** y la
gramática sería exactamente la de F1: el logro agrega alternativas, no cambia la estructura (5.0).

### 5.5 Operadores Posifijos E2

```
<Operando> ::= <Primitivo> | <Referencia> <PosfijoOpcional>
<PosfijoOpcional> ::= ++ | -- | ε
```

Va sobre `<Referencia>` y **no** sobre `<Operando>`: `5++` no tiene sentido. Misma técnica de
forma que el ternario: sin sus dos alternativas, `<PosfijoOpcional>` queda en `ε`.

### 5.6 Inicializacion de Arreglos E2

**Transformación.** Las dos formas de crear un arreglo empiezan con `[`, y se factoriza. Con una
expresión adentro es la creación de MiniJava base, con **toda** dimensión con su tamaño
(`DimensionesConTamanio -> [ Expresion ] DimensionesConTamanio | [ Expresion ]`). Con el corchete
vacío es la forma de Java con inicializador (JLS §15.10.1), donde **ninguna** dimensión lleva
tamaño:

```
<CreacionDeArreglo> ::= [ <RestoCreacionDeArreglo>
<RestoCreacionDeArreglo> ::= <Expresion> ] <DimensionesConTamanioOpcional>
                           | ] <DimensionesOpcionales> <InicializadorDeArreglo>
<InicializadorDeArreglo> ::= { <ListaElementosOpcional> }
<ListaElementosOpcional> ::= <ListaElementos> | , | ε
<ListaElementos> ::= <ElementoInicializador> <RestoListaElementos>
<RestoListaElementos> ::= , <ElementosTrasComa> | ε
<ElementosTrasComa> ::= <ElementoInicializador> <RestoListaElementos> | ε
<ElementoInicializador> ::= <Expresion> | <InicializadorDeArreglo>
```

Así `new int[][]{{1,2},{3,4}}` es válido y `new int[2][2]{{1,2},{3,4}}` no, igual que en Java
(`javac`: *«array creation with both dimension expression and initialization is illegal»*). Sin
inicializador rige la base: `new int[2][]` no es válido. La coma final es opcional, como en Java
(JLS §10.6): `{1, 2,}` y `{,}` son válidos; `<ElementosTrasComa>` es lo que la permite sin conflicto
de predicción. `<DimensionesConTamanio>` y `<DimensionesConTamanioOpcional>` son las del núcleo, sin
cambios.

Un elemento puede ser una expresión o, a su vez, otro inicializador: por eso es anidable, y las dos
alternativas son disjuntas porque una expresión nunca empieza con `{`. *«Sólo se permite como parte
de una expresión de creación utilizando `new`»* queda impuesto por la gramática:
`<InicializadorDeArreglo>` sólo es alcanzable desde `<Creacion>`.

### 5.7 Genericidad Avanzada E2

Dos cosas distintas: anidamiento y diamante.

**Anidamiento.** Sale de una sola recursión:

```
<InstanciadoOParametrico> ::= idGen | idClase <TipoGenericoOpcional>
```

Sin esa recursión la segunda alternativa sería `idClase` a secas y el anidamiento desaparecería.
(`List<List<Persona>>` cierra con dos `>` separados: el léxico no tiene token `>>`, así que
maximal munch da `>` `>` y no hay nada que hacer del lado del sintáctico.)

**Diamante, y la restricción de dónde vale.** El logro dice *«la notación Diamante solo puede
utilizarse al instanciar una Clase Genérica»*. Se impone **por gramática**, no por el semántico,
separando el genérico en posición de tipo del genérico bajo `new`:

```
<TipoGenericoOpcional> ::= < <InstanciadoOParametrico> > | ε          (posición de tipo: sin diamante)

<CreacionDesdeIdClase> ::= < <GenericoDeCreacion>
                         | <ArregloOInstanciaDeClase>
<GenericoDeCreacion> ::= > <ArgsActuales>                     ← el diamante, y sólo hacia ArgsActuales
                       | <InstanciadoOParametrico> > <ArregloOInstanciaDeClase>
```

Es la separación que anticipaba 3.bis.3. Resultado verificado:

| Entrada | Veredicto |
|---|---|
| `List<Persona> x;` | acepta |
| `List<List<Persona>> x;` | acepta |
| `new List<>()` | acepta |
| `List<> x;` | **rechaza** (diamante en posición de tipo) |
| `new List<>[3]` | **rechaza** (diamante con arreglo) |
| `new List<Persona>[3]` | acepta |

### 5.8 Floats (carry-over de la Etapa 1)

```
<TipoPrimitivo> ::= boolean | char | int | float
<Primitivo> ::= true | false | intLiteral | charLiteral | floatLiteral | null
```

Dos alternativas nuevas, cero conflictos. Sin esto, el logro de floats de la Etapa 1 queda
inutilizable en la Etapa 2: el léxico emite `pr_float` y `floatLiteral` y el sintáctico los
rechazaría.

## 6. Verificación final

```bash
cd compilador
python3 herramientas/primeros.py gramatica/minijava-ll1-final.g                  # ES LL(1)
python3 herramientas/primeros.py gramatica/minijava-ll1-final.g --ambiguedades   # todas declaradas
bash herramientas/casos-gramatica.sh                                              # 72/72
```

- **97 no terminales, 198 producciones, 59 terminales.**
- **LL(1)**: toda producción de todo no terminal tiene Primeros disjuntos.
- **72 casos** de aceptación y rechazo, cubriendo por cada construcción su propia sintaxis y la que
  debe rechazar. Entre ellos, los que fijan las decisiones difíciles: `a = b = c` y
  `a ? b : c = d` se rechazan, `List<> x;` se rechaza y `new List<>()` se acepta, `static public` y
  `public static` se aceptan los dos y `public private` no, `new int[]{1,2}` se acepta y
  `new int[2]{1,2}` no, `for (;;)` se acepta y `for (int i = 0; ; )` no.

El costo total de la corrección más los logros, respecto de la gramática base de la cátedra:
**de 60 a 97 no terminales y de 126 a 198 producciones**, sin ninguna ambigüedad sin declarar y sin
ningún conflicto de predicción.
