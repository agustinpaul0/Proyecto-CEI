# Decisiones de diseño

Cómo quedan en el código las recomendaciones de la cátedra para esta etapa, y las alternativas que
se evaluaron. El criterio general es la simplicidad: la eficiencia no importa en esta etapa.

## 1. Un nodo por construcción con significado, a partir de la gramática original

Clase 09, slides 29-40, y Pautas Etapa 4, slide 5: cada tipo de sentencia y de expresión es una
clase, y la herencia modela el «es un». El diseño sale de la gramática original de la cátedra
([`../sintactico/gramatica-original-de-la-catedra.md`](../sintactico/gramatica-original-de-la-catedra.md)),
no de la LL(1): los no terminales que agregó la transformación a LL(1) no tienen significado y no
tienen nodo ([`esquema-de-traduccion.md`](esquema-de-traduccion.md)).

Hay dos jerarquías, `NodoSentencia` y `NodoExpresion`. La asignación es una expresión, porque
produce un valor (documento §11.9: `return x = 3;`), y la sentencia que la contiene es una
`NodoSentenciaExpresion`. Es la forma de la gramática original, donde `<Asignacion>` y `<Llamada>`
son `<Expresion>`.

## 2. Cada nodo chequea lo suyo y delega en sus hijos

Pautas Etapa 4, slides 7-8, y Clase 10, slides 25-28: `chequear` está en cada nodo, no en un
visitante aparte. Una sentencia no tiene tipo y su `chequear` no devuelve nada; una expresión
devuelve su tipo. Lo que un nodo necesita saber del entorno —la clase, la unidad, las locales
visibles— viaja en un `Contexto`, que se crea por unidad.

**Alternativa descartada:** guardar la clase, la unidad y el bloque actuales en la tabla de
símbolos, como en las slides de la Clase 11 (slide 35, `TS.metodoActual()`). La tabla ya tiene un
estado «actual» que sólo vale mientras se parsea; sumarle otro, que vale mientras se chequea, mezcla
dos momentos. Un `Contexto` pasado por parámetro deja explícito de qué depende cada chequeo y se
prueba solo (`ContextoTest`).

## 3. Los encadenados guardan su receptor

La Clase 10, slides 29-31, propone una lista: `v.x.y` es una variable con un «encadenado» que tiene
otro, y cada eslabón recibe el tipo del anterior en `chequear(Tipo t)`. Acá cada acceso **guarda su
receptor**: `v.x.y` es el acceso a `y` sobre el acceso a `x` sobre `v`.

- Hay un solo `chequear(Contexto)` para todas las expresiones: no hace falta una segunda firma.
- El nodo de afuera es el último acceso, que es el que decide si la expresión es asignable o una
  llamada (documento §11.5). No hace falta recorrer la cadena hasta el final.
- El parser lo arma igual que un binario: `RestoReferencia` recibe el nodo armado hasta acá y lo
  envuelve.

## 4. La información estructural se calcula aparte

Clase 11, slides 26-37: hay dos alternativas para saber si una expresión es asignable o una
llamada. Se eligió la primera, un método en cada nodo (`esAsignable()`, `esLlamada()`,
`esAsignacion()`), que la slide 32 llama la más sencilla de implementar y la más limpia. Su costo,
volver a recorrer los hijos, es mínimo con receptores anidados: cada nodo mira a lo sumo a su
receptor.

La excepción es el `length` de un arreglo: si `v.length` es asignable depende del tipo de `v`, que se
conoce recién al chequear. `NodoAccesoAtributo` lo anota en `chequear` (la Alternativa 2 de la
slide 33), y la asignación chequea su lado izquierdo antes de preguntar si es asignable.

## 5. Un cuerpo se chequea en su clase: cada unidad sabe quién la declara

Clase 10, slide 51, y documento §14: con la herencia por copia, un método aparece en la tabla de cada
descendiente, y su cuerpo se chequea una sola vez, en la clase que lo declara. Cada `Unidad` guarda
su `declarante`, como sugirió el profesor
([`../../CONVENCIONES-CATEDRA.md`](../../CONVENCIONES-CATEDRA.md) §7), y la copia de la
consolidación lo conserva junto con el cuerpo. `Clase.chequearSentencias()` saltea las unidades de
otro declarante.

**Alternativa descartada:** recorrer sólo los métodos propios de cada clase, sin `declarante`. Da el
mismo resultado, pero deja implícito en el recorrido algo que el documento pide registrar.

## 6. El token de cada error es un dato del nodo

`NodoExpresion.token()` devuelve el token donde `javac` ubica un error de la expresión entera, y lo
usan los nodos que la contienen: el `if` cuya condición no es `boolean`, el `return` cuyo valor no
conforma, la sentencia que no es asignación ni llamada. Así la regla de qué token se reporta vive en
un solo lugar por nodo ([`decisiones-de-deteccion-de-errores.md`](decisiones-de-deteccion-de-errores.md)).

## 7. Las construcciones de logros no controlados tienen nodo

El parser las sigue aceptando, porque la Etapa 2 se corrige aparte. Sin nodo, el AST quedaría con
huecos, o habría que aceptarlas sin controlar. `SentenciaNoControlada` y `ExpresionNoControlada`
las representan y fallan si se las chequea: el compilador nunca acepta algo que no controló, y nunca
se cae (`RobustezDelChequeoDeSentenciasTest`).
