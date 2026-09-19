# Transformación de la gramática a LL(1), paso a paso

Cada paso que lleva la gramática de la cátedra
([`gramatica-original-de-la-catedra.md`](gramatica-original-de-la-catedra.md)) al núcleo LL(1)
(`compilador/gramatica/minijava-ll1.g`), y de ahí, logro por logro, a la gramática final
(`compilador/gramatica/minijava-ll1-final.g`). Cada paso aplica una técnica de Clase 4 a una regla
concreta, con los Primeros que chocan antes y los que quedan después.

## 0. Método

**Criterio LL(1)** (Clase 3, slide 66; Clase 4, slide 12): para todo `A → β₁ | … | βₙ`,
`Primeros(βᵢ) ∩ Primeros(βⱼ) = ∅` si `i ≠ j`.

**Orden** (Clase 4, slides 37-38): primero se elimina la recursión a izquierda y después se
factoriza, porque eliminar la recursión puede dejar producciones sin factorizar (R3).

**Técnicas.**

| Sigla | Técnica | Fuente |
|---|---|---|
| RI | Eliminación de recursión a izquierda directa: `A → Aα₁ \| … \| Aαₙ \| β₁ \| … \| βₘ` pasa a `A → β₁R \| … \| βₘR` y `R → α₁R \| … \| αₙR \| ε` | Clase 4, slide 33 |
| FD | Factorización a izquierda directa: `X → αβ₁ \| … \| αβₙ \| γ₁ \| … \| γₘ` pasa a `X → αR \| γ₁ \| … \| γₘ` y `R → β₁ \| … \| βₙ` | Clase 4, slide 18 |
| SU | Subir: reemplazar un no terminal por sus partes derechas, para exponer el prefijo común | Clase 4, slide 20 |
| FP | Factorización profunda: SU hasta que el prefijo sea directo, y después FD | Clase 4, slide 20 |
| AG | Agrupar: reemplazar alternativas, o una cola repetida, por un no terminal nuevo que deriva exactamente eso | paso auxiliar |

AG es la inversa de SU: no cambia el lenguaje ni hace falta para LL(1). Se usa para que cada punto
de decisión conserve un nombre (Clase 4, slides 39-40: las reglas tienen significado y el semántico
lo necesita), y se marca cada vez que aparece.

**Conjuntos que se repiten.**

- `T = Primeros(<Tipo>) = {boolean, char, int, idClase, idGen}`
- `E = Primeros(<Expresion>) = {!, (, +, -, charLiteral, false, idClase, idMetVar, intLiteral, new, null, stringLiteral, this, true}`
- `P = Primeros(<Primario>) = {(, idClase, idMetVar, new, stringLiteral, this}`

Desde L1, `T` incluye `float` y `E`, `floatLiteral`.

**Verificación.** `compilador/herramientas/primeros.py` recalcula vaciables, Primeros y la condición
LL(1):

```bash
cd compilador
python3 herramientas/primeros.py gramatica/minijava-base.g        # NO ES LL(1): 16 conflictos
python3 herramientas/primeros.py gramatica/minijava-ll1.g         # ES LL(1)
python3 herramientas/primeros.py gramatica/minijava-ll1-final.g   # ES LL(1)
python3 herramientas/equivalencia_de_lenguajes.py                 # núcleo ≡ base; final ⊇ base
```

## 1. Diagnóstico de la base

La base tiene **16 conflictos en 10 no terminales**:

| # | No terminal | Defecto | Intersección | Se corrige en |
|---|---|---|---|---|
| 1 | `<Miembro>` | prefijo común no directo | `T` | F6 |
| 2 | `<ListaArgsFormales>` | recursión a izquierda | `T` | R1 |
| 3 | `<Sentencia>` | `<Asignacion>` y `<Llamada>` idénticos | `E` | F2 |
| 4 | `<If>` | prefijo común | `{if}` | F3 |
| 5 | `<Expresion>` | prefijo común | `E` | F1 |
| 6 | `<ExpresionCompuesta>` | recursión a izquierda | `E` | R2 |
| 7-12 | `<Referencia>` | recursión a izquierda, tres alternativas (seis pares) | `P` | R3 |
| 13 | `<Primario>` | prefijo común no directo | `{idMetVar}` | F7 |
| 14 | `<Primario>` | prefijo común no directo, a dos niveles | `{new}` | F8 |
| 15 | `<DimensionesConTamanio>` | prefijo común | `{[}` | F4 |
| 16 | `<ListaExps>` | prefijo común | `E` | F5 |

**Ambigüedades.** Un conflicto LL(1) no implica ambigüedad, que es tener dos árboles para una misma
cadena (Clase 4, slide 46). La base tiene tres:

1. `<Asignacion>` y `<Llamada>` tienen el mismo cuerpo, así que toda sentencia-expresión tiene dos
   árboles. Se elimina en F2.
2. *Dangling else*: en `if (a) if (b) s1; else s2;` el `else` puede ligar a cualquiera de los dos
   `if`. Sobrevive a F3.
3. `new int[3][4]`: el segundo `[` puede ser otra dimensión de `<DimensionesConTamanio>` o un
   `<AccesoArreglo>` sobre el arreglo creado. No es un conflicto de Primeros de la base: aparece en
   F4.

En los demás conflictos las alternativas derivan lenguajes disjuntos: sólo comparten prefijo.

## 2. Eliminación de recursión a izquierda

### R1. `<ListaArgsFormales>`

**Original**

```
<ListaArgsFormales> ::= <ArgFormal>
                      | <ListaArgsFormales> , <ArgFormal>
```

**Conflicto.** Las dos alternativas tienen Primeros `T`.

**Paso.** RI con `β₁ = <ArgFormal>`, `α₁ = , <ArgFormal>` y `R = <RestoListaArgsFormales>`.

**Resultado**

```
<ListaArgsFormales> ::= <ArgFormal> <RestoListaArgsFormales>
<RestoListaArgsFormales> ::= , <ArgFormal> <RestoListaArgsFormales> | ε
```

`{,}` contra `{ε}`. ✔

### R2. `<ExpresionCompuesta>`

**Original**

```
<ExpresionCompuesta> ::= <ExpresionCompuesta> <OperadorBinario> <ExpresionBasica>
                       | <ExpresionBasica>
```

**Conflicto.** Las dos alternativas tienen Primeros `E`.

**Paso.** RI con `β₁ = <ExpresionBasica>`, `α₁ = <OperadorBinario> <ExpresionBasica>` y
`R = <RestoExpresionCompuesta>`.

**Resultado**

```
<ExpresionCompuesta> ::= <ExpresionBasica> <RestoExpresionCompuesta>
<RestoExpresionCompuesta> ::= <OperadorBinario> <ExpresionBasica> <RestoExpresionCompuesta> | ε
```

Los trece operadores binarios contra `{ε}`. ✔ Los operadores siguen en un solo nivel, como en la base.

### R3. `<Referencia>`

**Original**

```
<Referencia> ::= <Primario>
               | <Referencia> <VarEncadenada>
               | <Referencia> <MetodoEncadenado>
               | <Referencia> <AccesoArreglo>
<VarEncadenada> ::= . idMetVar
<MetodoEncadenado> ::= . idMetVar <ArgsActuales>
```

**Conflicto.** Las cuatro alternativas tienen Primeros `P`: seis pares.

**Paso 1.** RI con `β₁ = <Primario>`, `α₁ = <VarEncadenada>`, `α₂ = <MetodoEncadenado>`,
`α₃ = <AccesoArreglo>` y `R = <RestoReferencia>`:

```
<Referencia> ::= <Primario> <RestoReferencia>
<RestoReferencia> ::= <VarEncadenada> <RestoReferencia>
                    | <MetodoEncadenado> <RestoReferencia>
                    | <AccesoArreglo> <RestoReferencia>
                    | ε
```

Conflicto nuevo, el que anticipa Clase 4, slide 38: las dos primeras alternativas tienen Primeros
`{.}`.

**Paso 2 (AG).** Las dos comparten la cola `<RestoReferencia>`:

```
<RestoReferencia> ::= <Encadenado> <RestoReferencia> | <AccesoArreglo> <RestoReferencia> | ε
<Encadenado> ::= <VarEncadenada> | <MetodoEncadenado>
```

El conflicto pasa a `<Encadenado>`: `{.}` contra `{.}`.

**Paso 3.** FP: SU de `<VarEncadenada>` y `<MetodoEncadenado>`,

```
<Encadenado> ::= . idMetVar | . idMetVar <ArgsActuales>
```

y FD con `α = . idMetVar`, `β₁ = ε`, `β₂ = <ArgsActuales>` y `R = <EncadenadoAtributoOMetodo>`.

**Resultado**

```
<Referencia> ::= <Primario> <RestoReferencia>
<RestoReferencia> ::= <Encadenado> <RestoReferencia> | <AccesoArreglo> <RestoReferencia> | ε
<Encadenado> ::= . idMetVar <EncadenadoAtributoOMetodo>
<EncadenadoAtributoOMetodo> ::= <ArgsActuales> | ε
```

`<RestoReferencia>`: `{.}`, `{[}` y `{ε}`. `<EncadenadoAtributoOMetodo>`: `{(}` contra `{ε}`. ✔

Se pierden `<VarEncadenada>` y `<MetodoEncadenado>`: son alternativas con el mismo prefijo y no se
pueden conservar. La distinción queda en `<EncadenadoAtributoOMetodo>`: vacío es el atributo, con
argumentos el método.

## 3. Factorización

### F1. `<Expresion>`

**Original**

```
<Expresion> ::= <ExpresionCompuesta> <OperadorAsignacion> <ExpresionCompuesta>
              | <ExpresionCompuesta>
```

**Conflicto.** Las dos alternativas tienen Primeros `E`.

**Paso.** FD con `α = <ExpresionCompuesta>`, `β₁ = <OperadorAsignacion> <ExpresionCompuesta>`,
`β₂ = ε` y `R = <RestoExpresion>`.

**Resultado**

```
<Expresion> ::= <ExpresionCompuesta> <RestoExpresion>
<RestoExpresion> ::= <OperadorAsignacion> <ExpresionCompuesta> | ε
```

`{=}` contra `{ε}`. ✔ El lado derecho del `=` sigue siendo una `<ExpresionCompuesta>`, así que
`a = b = c` no pertenece al lenguaje, igual que en la base. Escribir `<Expresion>` ahí lo agrandaría.

### F2. `<Sentencia>`

**Original**

```
<Sentencia> ::= ; | <Asignacion> ; | <Llamada> ; | <VarLocal> ; | <Return> ; | <If> | <While> | <Bloque>
<Asignacion> ::= <Expresion>
<Llamada> ::= <Expresion>
```

**Conflicto.** `<Asignacion> ;` y `<Llamada> ;` tienen Primeros `E`, y además son la misma
producción: la ambigüedad 1.

**Paso 1.** SU de `<Asignacion>` y `<Llamada>`: las dos alternativas quedan `<Expresion> ;`, la misma
producción dos veces. Como conjunto de producciones es una sola, y con ella hay un solo árbol.

**Paso 2 (AG).** `<SentenciaExpresion> ::= <Expresion>`.

**Resultado**

```
<Sentencia> ::= ; | <SentenciaExpresion> ; | <VarLocal> ; | <Return> ; | <If> | <While> | <Bloque>
<SentenciaExpresion> ::= <Expresion>
```

`{;}`, `E`, `{var}`, `{return}`, `{if}`, `{while}` y `{{}`. ✔ Distinguir una asignación de una
llamada exige conocer el significado de la expresión: queda para el semántico.

### F3. `<If>`

**Original**

```
<If> ::= if ( <Expresion> ) <Sentencia>
       | if ( <Expresion> ) <Sentencia> else <Sentencia>
```

**Conflicto.** `{if}` contra `{if}`.

**Paso.** FD con `α = if ( <Expresion> ) <Sentencia>`, `β₁ = ε`, `β₂ = else <Sentencia>` y
`R = <ElseOpcional>`.

**Resultado**

```
<If> ::= if ( <Expresion> ) <Sentencia> <ElseOpcional>
<ElseOpcional> ::= else <Sentencia> | ε
```

`{else}` contra `{ε}`. ✔ Queda la ambigüedad 2: `<ElseOpcional>` puede derivar a ε y lo que le
sigue puede empezar con `else`. Se resuelve en la implementación (§6).

### F4. `<DimensionesConTamanio>`

**Original**

```
<DimensionesConTamanio> ::= [ <Expresion> ] <DimensionesConTamanio> | [ <Expresion> ]
```

**Conflicto.** `{[}` contra `{[}`.

**Paso.** FD con `α = [ <Expresion> ]`, `β₁ = <DimensionesConTamanio>`, `β₂ = ε` y
`R = <DimensionesConTamanioOpcional>`.

**Resultado**

```
<DimensionesConTamanio> ::= [ <Expresion> ] <DimensionesConTamanioOpcional>
<DimensionesConTamanioOpcional> ::= <DimensionesConTamanio> | ε
```

`{[}` contra `{ε}`. ✔ Acá aparece la ambigüedad 3: lo que sigue a
`<DimensionesConTamanioOpcional>`, por `<Primario>` y `<RestoReferencia>`, puede empezar con `[`.
Se resuelve en la implementación (§6).

### F5. `<ListaExps>`

**Original**

```
<ListaExps> ::= <Expresion>
              | <Expresion> , <ListaExps>
```

**Conflicto.** Las dos alternativas tienen Primeros `E`.

**Paso.** FD con `α = <Expresion>`, `β₁ = ε`, `β₂ = , <ListaExps>` y `R = <RestoListaExps>`.

**Resultado**

```
<ListaExps> ::= <Expresion> <RestoListaExps>
<RestoListaExps> ::= , <ListaExps> | ε
```

`{,}` contra `{ε}`. ✔

### F6. `<Miembro>`

**Original**

```
<Miembro> ::= <Atributo> | <Metodo> | <Constructor>
<Atributo> ::= <Tipo> idMetVar ;
<Metodo> ::= <ModificadorOpcional> <TipoMetodo> idMetVar <ArgsFormales> <Bloque>
<Constructor> ::= public idClase <ArgsFormales> <Bloque>
<ModificadorOpcional> ::= static | ε
<TipoMetodo> ::= <Tipo> | void
```

**Conflicto.** `Primeros(<Atributo>) = T`, `Primeros(<Metodo>) = {static, void} ∪ T` y
`Primeros(<Constructor>) = {public}`: `<Atributo>` y `<Metodo>` chocan en `T`. El prefijo no es
directo.

**Paso 1.** SU de `<Atributo>` y `<Metodo>`:

```
<Miembro> ::= <Tipo> idMetVar ;
            | <ModificadorOpcional> <TipoMetodo> idMetVar <ArgsFormales> <Bloque>
            | <Constructor>
```

**Paso 2.** SU de `<ModificadorOpcional>`:

```
<Miembro> ::= <Tipo> idMetVar ;
            | static <TipoMetodo> idMetVar <ArgsFormales> <Bloque>
            | <TipoMetodo> idMetVar <ArgsFormales> <Bloque>
            | <Constructor>
```

**Paso 3.** SU de `<TipoMetodo>` en la tercera alternativa:

```
<Miembro> ::= <Tipo> idMetVar ;
            | static <TipoMetodo> idMetVar <ArgsFormales> <Bloque>
            | <Tipo> idMetVar <ArgsFormales> <Bloque>
            | void idMetVar <ArgsFormales> <Bloque>
            | <Constructor>
```

**Paso 4.** FD con `α = <Tipo> idMetVar`, `β₁ = ;`, `β₂ = <ArgsFormales> <Bloque>` y
`R = <AtributoOMetodo>`:

```
<Miembro> ::= <Tipo> idMetVar <AtributoOMetodo>
            | static <TipoMetodo> idMetVar <ArgsFormales> <Bloque>
            | void idMetVar <ArgsFormales> <Bloque>
            | <Constructor>
<AtributoOMetodo> ::= ; | <ArgsFormales> <Bloque>
```

`<Miembro>`: `T`, `{static}`, `{void}` y `{public}`. `<AtributoOMetodo>`: `{;}` contra `{(}`. ✔

**Paso 5 (AG).** `<CuerpoDeMetodo> ::= <ArgsFormales> <Bloque>` en sus cuatro apariciones, incluida
la de `<Constructor>`, y `<Metodo> ::= idMetVar <CuerpoDeMetodo>` en las ramas `static` y `void`.

**Resultado**

```
<Miembro> ::= <Tipo> idMetVar <AtributoOMetodo>
            | static <TipoMetodo> <Metodo>
            | void <Metodo>
            | <Constructor>
<AtributoOMetodo> ::= ; | <CuerpoDeMetodo>
<Metodo> ::= idMetVar <CuerpoDeMetodo>
<CuerpoDeMetodo> ::= <ArgsFormales> <Bloque>
<Constructor> ::= public idClase <CuerpoDeMetodo>
```

Se pierden `<Atributo>` y `<ModificadorOpcional>`, por la SU. `<Metodo>` conserva el nombre pero
deriva la cola que comparten las ramas `static` y `void`; el método con tipo y sin `static` llega
por `<AtributoOMetodo>`, porque su `idMetVar` ya se consumió al factorizar contra el atributo.

### F7. `<Primario>`, prefijo `idMetVar`

**Original**

```
<Primario> ::= this | stringLiteral | <AccesoVar> | <CrearArreglo> | <LlamadaConstructor>
             | <LlamadaMetodo> | <LlamadaMetodoEstatico> | <ExpresionParentizada>
<AccesoVar> ::= idMetVar
<LlamadaMetodo> ::= idMetVar <ArgsActuales>
```

**Conflicto.** `<AccesoVar>` y `<LlamadaMetodo>` en `{idMetVar}`. El otro conflicto de `<Primario>`
se trata en F8.

**Paso 1 (AG).** `<AccesoVarOLlamadaMetodo> ::= <AccesoVar> | <LlamadaMetodo>` reemplaza a las dos en
`<Primario>`.

**Paso 2.** FP: SU de `<AccesoVar>` y `<LlamadaMetodo>`,

```
<AccesoVarOLlamadaMetodo> ::= idMetVar | idMetVar <ArgsActuales>
```

y FD con `α = idMetVar`, `β₁ = ε`, `β₂ = <ArgsActuales>` y `R = <AccesoOLlamada>`.

**Resultado**

```
<AccesoVarOLlamadaMetodo> ::= idMetVar <AccesoOLlamada>
<AccesoOLlamada> ::= <ArgsActuales> | ε
```

`{(}` contra `{ε}`. ✔ `<AccesoOLlamada>` no se comparte con `<EncadenadoAtributoOMetodo>` aunque
tengan la misma forma: su ε es una variable propia, y el de aquél un atributo de otro objeto.

### F8. `<Primario>`, prefijo `new`

**Original**

```
<CrearArreglo> ::= new <TipoBase> <DimensionesConTamanio>
<LlamadaConstructor> ::= new <TipoReferencia> <ArgsActuales>
<TipoBase> ::= <TipoPrimitivo> | <TipoReferencia> | idGen
```

**Conflicto.** `{new}` contra `{new}`.

**Paso 1 (AG).** `<Creacion> ::= <CrearArreglo> | <LlamadaConstructor>` reemplaza a las dos en
`<Primario>`.

**Paso 2.** FP: SU de las dos y FD con `α = new` y `R = <ArregloOInstancia>`:

```
<Creacion> ::= new <ArregloOInstancia>
<ArregloOInstancia> ::= <TipoBase> <DimensionesConTamanio> | <TipoReferencia> <ArgsActuales>
```

Conflicto nuevo: `T` contra `{idClase}`, en `{idClase}`.

**Paso 3.** FP: SU de `<TipoBase>`,

```
<ArregloOInstancia> ::= <TipoPrimitivo> <DimensionesConTamanio>
                      | <TipoReferencia> <DimensionesConTamanio>
                      | idGen <DimensionesConTamanio>
                      | <TipoReferencia> <ArgsActuales>
```

y FD con `α = <TipoReferencia>`, `β₁ = <DimensionesConTamanio>`, `β₂ = <ArgsActuales>` y
`R = <ArregloOInstanciaDeClase>`. `<TipoReferencia>` no se sube: no hace falta.

**Resultado**

```
<Primario> ::= this | stringLiteral | <ExpresionParentizada> | <AccesoVarOLlamadaMetodo>
             | <LlamadaMetodoEstatico> | <Creacion>
<Creacion> ::= new <ArregloOInstancia>
<ArregloOInstancia> ::= <TipoPrimitivo> <DimensionesConTamanio>
                      | idGen <DimensionesConTamanio>
                      | <TipoReferencia> <ArregloOInstanciaDeClase>
<ArregloOInstanciaDeClase> ::= <DimensionesConTamanio> | <ArgsActuales>
```

`<Primario>`: `{this}`, `{stringLiteral}`, `{(}`, `{idMetVar}`, `{idClase}` y `{new}`.
`<ArregloOInstancia>`: `{boolean, char, int}`, `{idGen}` y `{idClase}`.
`<ArregloOInstanciaDeClase>`: `{[}` contra `{(}`. ✔

Se pierden `<AccesoVar>`, `<LlamadaMetodo>`, `<CrearArreglo>` y `<LlamadaConstructor>`.

## 4. El núcleo

Con R1-R3 y F1-F8 el núcleo es LL(1): de 60 a **67 no terminales** y de 126 a **135
producciones**. De la base se pierden diez no terminales, todos por SU: `<Asignacion>`, `<Llamada>`,
`<Atributo>`, `<ModificadorOpcional>`, `<VarEncadenada>`, `<MetodoEncadenado>`, `<AccesoVar>`,
`<LlamadaMetodo>`, `<CrearArreglo>` y `<LlamadaConstructor>`.

## 5. Logros

Cada logro parte de la gramática que dejó el anterior. Primero se escribe su extensión en BNF y
después se la lleva a LL(1) con las mismas técnicas.

### L1. Floats (Etapa 1)

**Extensión**

```
<TipoPrimitivo> ::= boolean | char | int | float
<Primitivo> ::= true | false | intLiteral | charLiteral | null | floatLiteral
```

Sin conflictos.

### L2. Operadores Posfijos

**Extensión**

```
<Operando> ::= <Primitivo> | <Referencia> <PosfijoOpcional>
<PosfijoOpcional> ::= ++ | -- | ε
```

`<Operando>`: `{charLiteral, false, floatLiteral, intLiteral, null, true}` contra `P`.
`<PosfijoOpcional>`: `{++}`, `{--}` y `{ε}`. Sin conflictos. El posfijo va sobre `<Referencia>`
porque un `<Primitivo>` es un literal y nunca una variable; que la `<Referencia>` sea una variable
lo verifica el semántico.

### L3. Operador Ternario

**Extensión.** Sobre las reglas de la base, el lado derecho del `=` y el de `<VarLocal>` pasan a ser
una `<ExpresionCondicional>`, con los operandos del JLS §15.25:

```
<Expresion> ::= <ExpresionCompuesta> <OperadorAsignacion> <ExpresionCondicional>
              | <ExpresionCondicional>
<ExpresionCondicional> ::= <ExpresionCompuesta> ? <Expresion> : <ExpresionCondicional>
                         | <ExpresionCompuesta>
<VarLocal> ::= var idMetVar = <ExpresionCondicional>
```

**Conflictos.** En `<ExpresionCondicional>` y en `<Expresion>` las dos alternativas tienen Primeros
`E`.

**Paso 1.** FD en `<ExpresionCondicional>` con `α = <ExpresionCompuesta>` y `R = <RestoCondicional>`.

**Paso 2.** FP en `<Expresion>`: SU de `<ExpresionCondicional>` en la segunda alternativa, que queda
`<ExpresionCompuesta> <RestoCondicional>`, y FD con `α = <ExpresionCompuesta>`,
`β₁ = <OperadorAsignacion> <ExpresionCondicional>`, `β₂ = <RestoCondicional>` y
`R = <RestoExpresion>`, que reemplaza al de F1.

**Resultado**

```
<Expresion> ::= <ExpresionCompuesta> <RestoExpresion>
<RestoExpresion> ::= <OperadorAsignacion> <ExpresionCondicional> | <RestoCondicional>
<ExpresionCondicional> ::= <ExpresionCompuesta> <RestoCondicional>
<RestoCondicional> ::= ? <Expresion> : <ExpresionCondicional> | ε
<VarLocal> ::= var idMetVar = <ExpresionCondicional>
```

`<RestoExpresion>`: `{=}` contra `{?, ε}`. `<RestoCondicional>`: `{?}` contra `{ε}`. ✔ El lado
izquierdo del `=` sigue siendo una `<ExpresionCompuesta>` y el derecho no admite otro `=`. El
ternario es asociativo a derecha.

### L4. Variables Locales Clásicas

**Extensión**

```
<Sentencia> ::= … | <VarLocalClasica> ;
<VarLocalClasica> ::= <Tipo> <ListaDeclaradores>
<ListaDeclaradores> ::= idMetVar <InicializacionDeDeclarador>
                      | <ListaDeclaradores> , idMetVar <InicializacionDeDeclarador>
<InicializacionDeDeclarador> ::= = <ExpresionCondicional> | ε
```

**Conflictos.** `<ListaDeclaradores>` tiene recursión a izquierda. En `<Sentencia>`,
`<SentenciaExpresion> ;` (Primeros `E`) y `<VarLocalClasica> ;` (Primeros `T`) chocan en `{idClase}`:
`C.m();` contra `C x;`.

**Paso 1.** RI en `<ListaDeclaradores>` con `β₁ = idMetVar <InicializacionDeDeclarador>`,
`α₁ = , idMetVar <InicializacionDeDeclarador>` y `R = <RestoListaDeclaradores>`.

**Paso 2.** FP en `<Sentencia>`, SU hasta exponer `idClase` en las dos alternativas:

- la declaración, por `<VarLocalClasica>`, `<Tipo>`, `<TipoBase>` y `<TipoReferencia>`:

  ```
  <TipoPrimitivo> <DimensionesOpcionales> <ListaDeclaradores> ;
  idGen <DimensionesOpcionales> <ListaDeclaradores> ;
  idClase <TipoGenericoOpcional> <DimensionesOpcionales> <ListaDeclaradores> ;
  ```

- la expresión, por `<SentenciaExpresion>`, `<Expresion>`, `<ExpresionCompuesta>`,
  `<ExpresionBasica>`, `<Operando>`, `<Referencia>`, `<Primario>` y `<LlamadaMetodoEstatico>`:

  ```
  <OperadorUnario> <Operando> <RestoExpresionCompuesta> <RestoExpresion> ;
  <Primitivo> <RestoExpresionCompuesta> <RestoExpresion> ;
  X <RestoReferencia> <PosfijoOpcional> <RestoExpresionCompuesta> <RestoExpresion> ;
  idClase . idMetVar <ArgsActuales> <RestoReferencia> <PosfijoOpcional> <RestoExpresionCompuesta> <RestoExpresion> ;
  ```

  con `X` cada uno de `this`, `stringLiteral`, `<ExpresionParentizada>`,
  `<AccesoVarOLlamadaMetodo>` y `<Creacion>`.

**Paso 3.** FD con `α = idClase` y `R = <SentenciaDesdeIdClase>`.

**Paso 4 (AG).** Para no duplicar las colas:

```
<ColaTrasOperando> ::= <RestoExpresionCompuesta> <RestoExpresion>
<ColaDeExpresionDesdePrimario> ::= <RestoReferencia> <PosfijoOpcional> <ColaTrasOperando>
<PrimarioSinIdClase> ::= this | stringLiteral | <ExpresionParentizada> | <AccesoVarOLlamadaMetodo> | <Creacion>
<Primario> ::= <LlamadaMetodoEstatico> | <PrimarioSinIdClase>
<ExpresionSinIdClase> ::= <OperadorUnario> <Operando> <ColaTrasOperando>
                        | <Primitivo> <ColaTrasOperando>
                        | <PrimarioSinIdClase> <ColaDeExpresionDesdePrimario>
```

**Resultado**

```
<Sentencia> ::= ; | <VarLocal> ; | <Return> ; | <If> | <While> | <Bloque>
              | <ExpresionSinIdClase> ;
              | <TipoPrimitivo> <DimensionesOpcionales> <ListaDeclaradores> ;
              | idGen <DimensionesOpcionales> <ListaDeclaradores> ;
              | idClase <SentenciaDesdeIdClase>
<SentenciaDesdeIdClase> ::= <TipoGenericoOpcional> <DimensionesOpcionales> <ListaDeclaradores> ;
                          | . idMetVar <ArgsActuales> <ColaDeExpresionDesdePrimario> ;
<ListaDeclaradores> ::= idMetVar <InicializacionDeDeclarador> <RestoListaDeclaradores>
<RestoListaDeclaradores> ::= , idMetVar <InicializacionDeDeclarador> <RestoListaDeclaradores> | ε
```

más los cinco no terminales del paso 4. `<Sentencia>`: `{;}`, `{var}`, `{return}`, `{if}`,
`{while}`, `{{}`, `E` sin `idClase`, `{boolean, char, float, int}`, `{idGen}` y `{idClase}`.
`<SentenciaDesdeIdClase>`: `{<, [, idMetVar}` contra `{.}`. `<ExpresionSinIdClase>`: `{!, +, -}`,
los literales y `{(, idMetVar, new, stringLiteral, this}`. `<Primario>`: `{idClase}` contra el resto
de `P`. ✔

Se disuelven `<SentenciaExpresion>` y `<VarLocalClasica>`. Mirar dos tokens después del `idClase`
resolvería lo mismo sin no terminales nuevos, pero se aparta del Esquema Simple, que usa uno.

### L5. Atributos Inicializados

**Extensión.** La cola del atributo, en `<AtributoOMetodo>`, admite la inicialización:

```
<AtributoOMetodo> ::= <InicializacionDeAtributo> ; | <CuerpoDeMetodo>
<InicializacionDeAtributo> ::= = <ExpresionCondicional> | ε
```

`{=, ;}` contra `{(}`. Sin conflictos. No se comparte con `<InicializacionDeDeclarador>`: son
construcciones distintas para el semántico.

### L6. Visibilidad Mejorada

**Extensión.** Visibilidad opcional en atributos, métodos y constructores; `static` antes o después
de ella, sin repetir ninguno de los dos; `public` opcional en los métodos de interfaz:

```
<Miembro> ::= <VisibilidadOpcional> <Tipo> idMetVar <AtributoOMetodo>
            | <Visibilidad> static <TipoMetodo> <Metodo>
            | static <VisibilidadOpcional> <TipoMetodo> <Metodo>
            | <VisibilidadOpcional> void <Metodo>
            | <Constructor>
<Constructor> ::= <VisibilidadOpcional> idClase <CuerpoDeMetodo>
<Visibilidad> ::= public | private
<VisibilidadOpcional> ::= <Visibilidad> | ε
<MetodoInterfaz> ::= <PublicOpcional> <TipoMetodo> idMetVar <ArgsFormales> ;
<PublicOpcional> ::= public | ε
```

**Conflictos.** En `<Miembro>`, `{public, private}` en la 1ª, 2ª, 4ª y 5ª alternativa, e `{idClase}`
en la 1ª y la 5ª.

**Paso 1.** SU de `<VisibilidadOpcional>` en la 1ª y la 4ª, y de `<Constructor>`:

```
<Miembro> ::= <Visibilidad> <Tipo> idMetVar <AtributoOMetodo> | <Tipo> idMetVar <AtributoOMetodo>
            | <Visibilidad> static <TipoMetodo> <Metodo>
            | static <VisibilidadOpcional> <TipoMetodo> <Metodo>
            | <Visibilidad> void <Metodo> | void <Metodo>
            | <Visibilidad> idClase <CuerpoDeMetodo> | idClase <CuerpoDeMetodo>
```

**Paso 2 (AG).** Las tres construcciones que aparecen con y sin visibilidad:

```
<MiembroSinModificadores> ::= void <Metodo> | <Tipo> idMetVar <AtributoOMetodo> | idClase <CuerpoDeMetodo>
<Miembro> ::= <Visibilidad> <MiembroSinModificadores> | <Visibilidad> static <TipoMetodo> <Metodo>
            | static <VisibilidadOpcional> <TipoMetodo> <Metodo> | <MiembroSinModificadores>
```

**Paso 3.** FD en `<Miembro>` con `α = <Visibilidad>` y `R = <MiembroTrasVisibilidad>`.

**Paso 4.** En `<MiembroSinModificadores>` chocan `T` e `{idClase}`: FP con SU de `<Tipo>`,
`<TipoBase>` y `<TipoReferencia>`, y FD con `α = idClase` y `R = <MiembroDesdeIdClase>`.

**Resultado**

```
<Miembro> ::= <Visibilidad> <MiembroTrasVisibilidad>
            | static <VisibilidadOpcional> <TipoMetodo> <Metodo>
            | <MiembroSinModificadores>
<MiembroTrasVisibilidad> ::= static <TipoMetodo> <Metodo> | <MiembroSinModificadores>
<MiembroSinModificadores> ::= void <Metodo>
                            | <TipoPrimitivo> <DimensionesOpcionales> idMetVar <AtributoOMetodo>
                            | idGen <DimensionesOpcionales> idMetVar <AtributoOMetodo>
                            | idClase <MiembroDesdeIdClase>
<MiembroDesdeIdClase> ::= <CuerpoDeMetodo>
                        | <TipoGenericoOpcional> <DimensionesOpcionales> idMetVar <AtributoOMetodo>
```

`<Miembro>`: `{public, private}`, `{static}` y `{boolean, char, float, int, idClase, idGen, void}`.
`<MiembroTrasVisibilidad>`: `{static}` contra el último. `<MiembroSinModificadores>`: `{void}`,
`{boolean, char, float, int}`, `{idGen}` y `{idClase}`. `<MiembroDesdeIdClase>`: `{(}` contra
`{<, [, idMetVar}`. ✔

Se disuelve `<Constructor>`: sin un `public` obligatorio, compite con el tipo por `idClase`.

### L7. Fors!

**Extensión**

```
<Sentencia> ::= … | <For>
<For> ::= for ( <EncabezadoFor> ) <Sentencia>
<EncabezadoFor> ::= <VarLocal> <ClausulasFinalesFor>
                  | <Expresion> <ClausulasFinalesFor>
                  | var idMetVar : <Expresion>
<ClausulasFinalesFor> ::= ; <Expresion> ; <Expresion>
```

**Conflicto.** En `<EncabezadoFor>`, `{var}` contra `{var}`.

**Paso.** FP: SU de `<VarLocal>`, que da `var idMetVar = <ExpresionCondicional> <ClausulasFinalesFor>`,
y FD con `α = var idMetVar` y `R = <EncabezadoForTrasVar>`.

**Resultado**

```
<EncabezadoFor> ::= var idMetVar <EncabezadoForTrasVar> | <Expresion> <ClausulasFinalesFor>
<EncabezadoForTrasVar> ::= : <Expresion> | = <ExpresionCondicional> <ClausulasFinalesFor>
```

`<EncabezadoFor>`: `{var}` contra `E`. `<EncabezadoForTrasVar>`: `{:}` contra `{=}`. ✔

### L8. Inicialización de Arreglos

**Extensión.** En los tres lugares donde F8 dejó `<DimensionesConTamanio>` va una
`<CreacionDeArreglo>`, que admite además la forma de Java con inicializador (JLS §15.10.1, §10.6):

```
<ArregloOInstancia> ::= <TipoPrimitivo> <CreacionDeArreglo> | idGen <CreacionDeArreglo>
                      | <TipoReferencia> <ArregloOInstanciaDeClase>
<ArregloOInstanciaDeClase> ::= <CreacionDeArreglo> | <ArgsActuales>
<CreacionDeArreglo> ::= <DimensionesConTamanio> | [ ] <DimensionesOpcionales> <InicializadorDeArreglo>
<InicializadorDeArreglo> ::= { <ListaElementosOpcional> }
<ListaElementosOpcional> ::= <ListaElementos> | , | ε
<ListaElementos> ::= <ElementoInicializador>
                   | <ElementoInicializador> ,
                   | <ElementoInicializador> , <ListaElementos>
<ElementoInicializador> ::= <Expresion> | <InicializadorDeArreglo>
```

**Conflictos.** En `<CreacionDeArreglo>`, `{[}` contra `{[}`. En `<ListaElementos>`, las tres
alternativas tienen Primeros `E ∪ {{}`.

**Paso 1.** FP en `<CreacionDeArreglo>`: SU de `<DimensionesConTamanio>`, que da
`[ <Expresion> ] <DimensionesConTamanioOpcional>`, y FD con `α = [` y `R = <RestoCreacionDeArreglo>`.

**Paso 2.** FD en `<ListaElementos>` con `α = <ElementoInicializador>` y `R = <RestoListaElementos>`,
que queda `<RestoListaElementos> ::= ε | , | , <ListaElementos>`: conflicto `{,}` contra `{,}`.

**Paso 3.** FD en `<RestoListaElementos>` con `α = ,`, `β₁ = ε`, `β₂ = <ListaElementos>` y
`R = <ElementosTrasComa>`.

**Resultado**

```
<CreacionDeArreglo> ::= [ <RestoCreacionDeArreglo>
<RestoCreacionDeArreglo> ::= <Expresion> ] <DimensionesConTamanioOpcional>
                           | ] <DimensionesOpcionales> <InicializadorDeArreglo>
<ListaElementos> ::= <ElementoInicializador> <RestoListaElementos>
<RestoListaElementos> ::= , <ElementosTrasComa> | ε
<ElementosTrasComa> ::= <ListaElementos> | ε
```

`<RestoCreacionDeArreglo>`: `E` contra `{]}`. `<ListaElementosOpcional>`: `E ∪ {{}`, `{,}` y `{ε}`.
`<RestoListaElementos>`: `{,}` contra `{ε}`. `<ElementosTrasComa>`: `E ∪ {{}` contra `{ε}`.
`<ElementoInicializador>`: `E` contra `{{}`. ✔

Sin inicializador sigue la creación de la base, con toda dimensión con su tamaño.
`<InicializadorDeArreglo>` sólo es alcanzable desde un `new`.

### L9. Genericidad Avanzada

**Extensión, anidamiento.** `<InstanciadoOParametrico> ::= idGen | idClase <TipoGenericoOpcional>`.
`{idGen}` contra `{idClase}`. Sin conflictos.

**Extensión, diamante.** Sólo al instanciar: `<ArregloOInstancia> ::= … | idClase < > <ArgsActuales>`.

**Conflicto.** `<TipoReferencia> <ArregloOInstanciaDeClase>` contra `idClase < > <ArgsActuales>`, en
`{idClase}`.

**Paso 1.** FP: SU de `<TipoReferencia>`, que da `idClase <TipoGenericoOpcional> <ArregloOInstanciaDeClase>`,
y FD con `α = idClase` y `R = <CreacionDesdeIdClase>`, que queda
`<TipoGenericoOpcional> <ArregloOInstanciaDeClase> | < > <ArgsActuales>`: conflicto en `{<}`.

**Paso 2.** FP: SU de `<TipoGenericoOpcional>`, que da
`< <InstanciadoOParametrico> > <ArregloOInstanciaDeClase> | <ArregloOInstanciaDeClase>`, y FD con
`α = <` y `R = <GenericoDeCreacion>`.

**Resultado**

```
<ArregloOInstancia> ::= <TipoPrimitivo> <CreacionDeArreglo> | idGen <CreacionDeArreglo> | idClase <CreacionDesdeIdClase>
<CreacionDesdeIdClase> ::= < <GenericoDeCreacion> | <ArregloOInstanciaDeClase>
<GenericoDeCreacion> ::= > <ArgsActuales> | <InstanciadoOParametrico> > <ArregloOInstanciaDeClase>
```

`<CreacionDesdeIdClase>`: `{<}` contra `{[, (}`. `<GenericoDeCreacion>`: `{>}` contra
`{idClase, idGen}`. ✔ `List<> x;` y `new C<>[3]` no derivan: el diamante sólo lleva a
`<ArgsActuales>`.

## 6. Ambigüedades que quedan

Las dos que sobreviven se resuelven por regla de implementación (Clase 4, slide 49), y las dos del
mismo modo: el Esquema Simple toma la alternativa no vacía apenas ve su token.

| No terminal | Caso | Resolución |
|---|---|---|
| `<ElseOpcional>` | `if (a) if (b) s1; else s2;` | el `else` liga al `if` más cercano, como en Java |
| `<DimensionesConTamanioOpcional>` | `new int[3][4]` | el `[` es otra dimensión de la creación, como en Java |

Detectarlas exige mirar qué sigue a un no terminal anulable. Ese cálculo lo hace
`primeros.py --ambiguedades`, como auditoría: el analizador decide sólo con Primeros.

## 7. Resultado

**97 no terminales, 197 producciones y 59 terminales**; LL(1), con esas dos ambigüedades declaradas.
Frente a la base, se pierden además `<Constructor>` (L6); los once no terminales perdidos lo son por
SU.
