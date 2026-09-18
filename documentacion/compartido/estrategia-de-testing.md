# Estrategia de testing

Documento autocontenido: qué garantiza cada suite de tests, cómo están organizadas y cómo correrlas.

En este proyecto **los tests son la prioridad número uno, por encima del código de producción**. La
razón es específica de un compilador: tiene una superficie de entrada infinita y llena de bordes
—literales sin cerrar, escapes raros, caracteres invisibles, archivos truncados, expresiones
anidadas hasta el absurdo— y la única forma de saber que funciona es ejercitarlo sistemáticamente.
Un defecto no se manifiesta como una excepción ruidosa: se manifiesta como un token de menos o un
error reportado en el lugar equivocado, silenciosamente, tres etapas más adelante.

## 1. Las reglas del proyecto

1. **Todo código nuevo lleva tests.** No se escriben tests para subir un número de cobertura; se
   escriben para fijar comportamiento.
2. **Los tests verifican comportamiento, contratos e invariantes — nunca implementación interna.**
   Concretamente: un error se valida por **tipo y localización**, jamás por el texto completo del
   mensaje. Cambiar la redacción de un mensaje no debe romper ningún test.
3. **Todo defecto encontrado se convierte en test de regresión permanente.** El ciclo es: reproducir →
   escribir el test que falla → arreglar → confirmar que pasa → el caso queda en la suite para
   siempre. Los casos de regresión son **append-only**: no se borran ni se renumeran.
4. **Nunca se ajusta un test para acomodar una implementación equivocada.** Si un test falla, se
   arregla el código. Si la especificación cambió legítimamente, se actualiza el test **y se
   documenta el motivo**.
5. **Un error interno del compilador es siempre un defecto.** Un error léxico ante un programa
   inválido es comportamiento **esperado**; cualquier otra cosa —`NullPointerException`,
   `StackOverflowError`, `ClassCastException`— hace fallar el test, sin excepción.
6. Deterministas, independientes entre sí, sin depender del orden de ejecución, sin `Thread.sleep`.
   El fuzzing usa **semilla fija** justamente por esto.

## 2. Los ocho tipos de test y qué aporta cada uno

| Tipo | Qué verifica | Dónde |
|---|---|---|
| **Unitario** | Unidades aisladas y deterministas: el manejador de archivo, los tipos de token, el formateador de errores. | `ManejadorDeArchivoEficienteTest`, `TipoDeTokenTest`, `FormateadorDeErrorTest` |
| **Data-driven** | Muchas entradas evaluadas con la misma lógica: fuente → stream de tokens esperado. | `AnalizadorLexico*Test`, `LimitesNumericosTest`, `PosicionDelCaretTest` |
| **De integración** | Interacción entre piezas: manejador → analizador, analizador → módulo principal. | `ModuloPrincipalLexicoTest`, `ModuloPrincipalSintacticoTest`, `ReporteDeErrorEleganteTest` |
| **End-to-end** | El pipeline completo, entrando por `Main.main()` sobre archivos reales. | los 4 testers de la cátedra, `TesterDeCasosConErroresEstricto`, `TesterDeCasosSintacticosEstricto`, `MainTest` |
| **Negativo** | Un caso por cada tipo de error léxico: que el programa inválido se rechace **correctamente**. | `TipoDeErrorLexicoTest`, `resources/lexico/conErrores/` |
| **Basado en propiedades** | Invariantes que deben valer para **cualquier** entrada, no para casos elegidos. | `AnalizadorLexicoPropiedadesTest`, `PropiedadesDelReporteTest`, `AnalizadorSintacticoPropiedadesTest` |
| **Fuzzing** | Robustez ante entradas sin ninguna estructura: bytes arbitrarios en el léxico, programas derivados de la gramática y después mutados en el sintáctico. | `AnalizadorLexicoFuzzTest`, `CorpusDeRegresionTest`, `resources/{sinErrores,conErrores}/` |
| **Diferencial** | Que dos implementaciones distintas del mismo lenguaje coincidan. | `comparar_con_gramatica.py`, `equivalencia_de_lenguajes.py`, el oráculo JavaCC |

A eso se suma el **análisis de mutación** (PIT), que no es un tipo de test sino una forma de medir
qué tan buena es la suite: introduce mutaciones en el código de producción y comprueba que algún test
las detecte. Un mutante que sobrevive señala un test faltante. Corre en un perfil aparte
(`mvn -Pmutacion verify`) porque es lento; no en cada cambio. Los runners del corpus y las
propiedades quedan afuera de esa corrida: son excelentes para verificar pero pésimos para mutar,
porque cada mutante volvería a correr miles de casos parametrizados.

### El corpus del analizador sintáctico no se escribió a mano del todo

El corpus del sintáctico —`resources/sinErrores/` y `resources/conErrores/`— tiene dos orígenes,
y la distinción importa:

- **Escrito a mano** (~200 casos, 55 de ellos importados de un corpus ajeno): lo que un generador
  al azar no busca. El archivo vacío, el de sólo comentarios, la clase sin miembros, `for(;;)`,
  `new int[]{,}`, cien bloques anidados, el
  error en el primer token, el error en el último, el `$` del EOF. **Cada expectativa se predice
  antes de correr el compilador**: si no coincide, o el caso estaba mal escrito o el compilador
  tiene un defecto, y las dos cosas hay que mirarlas.
- **Generado** (~10.000 casos): Grammarinator deriva programas al azar de la gramática, los vuelve
  a parsear en árboles y los muta; el oráculo JavaCC dice qué veredicto corresponde. Un caso entra
  sólo si todos los oráculos de control coinciden. Son dos lotes, `base` y `fuzz`, con semillas
  separadas y sin casos repetidos entre ellos. Se regenera con `make fuzz` desde una semilla
  versionada.

## 3. Las invariantes, que son lo más valioso de la suite

Un caso concreto prueba que **una** entrada funciona. Una invariante prueba que **todas** funcionan.
Estas son las que sostiene el proyecto:

### 3.1. El análisis siempre termina

Para cualquier entrada, el compilador llega al `EOF` en una cantidad finita de pasos. Está
garantizado estructuralmente: el lexema de un error nunca es vacío, así que cada error consumió al
menos un carácter y la posición de lectura siempre avanza.

### 3.2. Los lexemas reconstruyen el fuente

Sobre un fuente sin blancos ni comentarios, **la concatenación de los lexemas de todos los tokens
emitidos y de todos los errores reconstruye el fuente completo**, carácter por carácter.

Esta es la invariante más fuerte de la suite, porque cierra de un plumazo toda una familia de bugs:
si la recuperación se comiera un carácter, o si un estado consumiera algo que no reporta, la
concatenación no daría igual al original. Es la que distingue el criterio de recuperación de su
alternativa descartada —ver
[`../lexico/decisiones-de-deteccion-de-errores.md`](../lexico/decisiones-de-deteccion-de-errores.md) §4.2.

Tiene una única excepción, y está escrita explícitamente en la propiedad: el resto de un `litString`
descartado tras un escape Unicode mal formado ([`../lexico/decisiones-de-deteccion-de-errores.md`](../lexico/decisiones-de-deteccion-de-errores.md) §4.3) no aparece en
ningún lexema. La propiedad reproduce esa regla de descarte **por su cuenta**, en vez de reusar la del
léxico, justamente para que un cambio accidental en el descarte la haga fallar en vez de pasar
inadvertido.

### 3.3. Las líneas nunca decrecen

El número de línea reportado por tokens y errores sucesivos es monótono no decreciente. Un fuente no
puede "volver" a una línea anterior.

### 3.4. El `^` nunca cae fuera de la línea impresa

En un reporte de error elegante, la columna del `^` siempre corresponde a un carácter o a su
representación visible en la línea mostrada debajo de `Detalle:`. Un caret una posición más allá del
último carácter señala el aire; es una invariante verificada tanto caso por caso
(`PosicionDelCaretTest`) como sobre entradas generadas (`PropiedadesDelReporteTest`).

### 3.5. Ningún fallo es un crash

Sobre bytes UTF-8 arbitrarios, cualquier fallo debe ser una `ExcepcionLexica` controlada. Cualquier
otra excepción es un defecto del compilador y hace fallar el test.

### 3.6. El formato de salida siempre se respeta

Cada línea de la salida es un token `(nombre,lexema,línea)`, un código `[Error:lexema|línea]`, una
línea del reporte elegante, o `[SinErrores]`. **Nunca** una línea partida en dos — que es lo que
pasaría si un lexema de error contuviera un salto de línea crudo.

## 4. Los archivos de caso: `resources/`

Además de los tests en Java, el proyecto mantiene un corpus de archivos fuente MiniJava con su
oráculo escrito en las primeras líneas, en el formato que definen los testers de la cátedra.

```
resources/
├── lexico/
│   ├── sinErrores/    76 archivos   oráculo: //#lexema,nroLinea   (un token esperado, en orden)
│   ├── conErrores/    82 archivos   oráculo: // [Error:lexema|nroLinea]
│   └── regresion/                    entradas que rompieron una corrida de fuzzing
├── sinErrores/                       el corpus de la Etapa 2, en la raíz porque ahí lo buscan
└── conErrores/                       los testers de la cátedra
```

### El tester estricto, y por qué existe

El tester de errores que provee la cátedra sólo verifica que el **primer** código de error declarado
aparezca en algún lugar de la salida. Eso deja dos agujeros grandes:

- un caso con cinco errores queda casi sin verificar;
- **nada comprueba qué tokens salen entre medio** — y ahí es exactamente donde se vería una
  recuperación que se trague caracteres.

Por eso el proyecto agrega `TesterDeCasosConErroresEstricto`, que corre sobre **los mismos archivos**
y verifica la salida **completa**, en orden: todos los códigos de error y todos los tokens. Los
testers de la cátedra quedan intactos al lado, corriendo en la misma pasada.

La cabecera de un caso estricto se ve así:

```java
// [Error:"a\u12|6]        ← primera línea: lo que verifica el tester de la cátedra
// [Completo]              ← activa la verificación estricta de todo lo que sigue
// [Error:"a\u12|6]
//#+,6
//#v1,6
//#$,7
"a\u12G4" + v1
```

### El corpus de regresión de fuzzing

`AnalizadorLexicoFuzzTest` persiste en `resources/lexico/regresion/` cualquier entrada aleatoria que
haya hecho fallar una corrida, y `CorpusDeRegresionTest` los reprocesa todos en cada corrida: sin
él, esos archivos quedarían guardados sin proteger de nada.

No hay oráculo por caso —son entradas aleatorias, nadie sabe qué tokens "deberían" salir—; lo que se
verifica es el contrato que vale para cualquier entrada: que el compilador termine, que no lance nada
que no sea una `ExcepcionLexica`, y que la salida respete el formato.

## 5. Suites, una por una

### Del analizador léxico

| Suite | Qué cubre |
|---|---|
| `AnalizadorLexico{BlancosYEof,Identificadores,Numeros,Operadores,Comentarios,Literales,Unicode,Recuperacion}Test` | El grueso del reconocimiento, una suite por familia léxica sobre `BaseDeAnalisisLexico`. |
| `TesterDeCasosConErroresEstricto` | Los archivos de `resources/lexico/conErrores/`, verificando la salida completa. |
| `catedra.TesterDeCasos{Sin,Con}Errores` | Los testers originales de la cátedra sobre el corpus léxico. |
| `TablaDeLexemasTest`, `TipoDeTokenTest` | Que los tipos de token y sus nombres impresos sean los que fija la cátedra. |
| `PosicionDelCaretTest`, `FormateadorDeErrorTest`, `EscapadorDeNoImprimiblesTest` | El reporte de error: a dónde apunta el `^` y que ningún carácter invisible parta el formato. |
| `CriterioDeRecuperacionTest`, `TipoDeErrorLexicoTest`, `LimitesNumericosTest` | Recuperación, taxonomía de errores y el límite de nueve dígitos. |
| `DivergenciasConJavaTest` | Los puntos donde MiniJava se aparta de Java **a propósito**. |
| `AnalizadorLexicoPropiedadesTest`, `PropiedadesDelReporteTest`, `AnalizadorLexicoFuzzTest`, `CorpusDeRegresionTest` | Invariantes sobre entradas generadas y bytes arbitrarios, con semilla fija. |

### Del analizador sintáctico

| Suite | Qué cubre |
|---|---|
| `AnalizadorSintacticoTest` | Nivel clase e interfaz, el `$` del EOF, el formato del mensaje, genericidad anidada. |
| `MiembrosYSentenciasTest` | Miembros, visibilidad, atributos inicializados, sentencias, variables clásicas, fors. |
| `ExpresionesTest` | Las capas de precedencia, ternario, posfijos, creación, inicializadores, diamante. |
| `LenguajeDeLaBaseYDeLosLogrosTest` | El lenguaje, regla por regla: lo que define la gramática de la cátedra y, donde no define nada, Java. Cada caso nombra la regla que decide su veredicto. |
| `CorpusExternoTest` | 16.512 fuentes generados a partir de la gramática de otra persona; tienen que coincidir todos. |
| `ModoPanicoTest` | Que la recuperación reporte todos los errores, avance siempre y no cambie el primero. |
| `CasosDeLaCatedraTest` | Los ocho fuentes que la cátedra adjunta, cada error en el token exacto. |
| `catedra.TesterDeCasosSintacticos{Sin,Con}Errores` | Los testers de la cátedra sobre el corpus, con su criterio intacto. |
| `TesterDeCasosSintacticosEstricto` | El mismo corpus, exigiendo que el código declarado sea el **primero** y que no haya ninguno de más. |
| `AnalizadorSintacticoPropiedadesTest` | Invariantes sobre sopas de tokens al azar. |
| `primeros.ConjuntoDePrimerosTest`, `errores.ExcepcionSintacticaTest` | El álgebra de los conjuntos de Primeros y el formato del error. |

### Compartidas

| Suite | Qué cubre |
|---|---|
| `ManejadorDeArchivoEficienteTest` | EOF, archivo vacío, los tres convenios de fin de línea, UTF-8 inválido, y el costo de ubicar el error en un fuente de 200.000 líneas. |
| `MainTest`, `OpcionesDeInvocacionTest`, `ModuloPrincipal{Lexico,Sintactico}Test` | El punto de entrada y el contrato de salida de las dos etapas. |

## 6. Cómo correr

```bash
cd compilador

mvn test                 # suite propia + los 4 testers de la cátedra, en la misma pasada
mvn verify               # además exige el umbral de cobertura de JaCoCo
mvn -Pmutacion verify    # análisis de mutación con PIT (verify: es la fase a la que está atado)

mvn test -Dvolcado=resumen   # el volcado por caso, en su forma corta
mvn test -Dvolcado=off       # sin volcado

mvn test -Pfase-lexico       # sólo el léxico (los de comun/ corren en las dos fases)
mvn test -Pfase-sintactico   # sólo el sintáctico
```

No hay forma de elegir con qué logros correr: la suite tiene una sola configuración.

### La verificación diferencial contra un corpus ajeno

El argumento de la sección anterior tiene un límite: las dos implementaciones que se comparan
salieron del mismo archivo de gramática, así que comparten sus decisiones. `CorpusExternoTest` cierra
ese hueco corriendo el compilador contra **16.512 fuentes generados a partir de otra gramática** de
MiniJava (`vendor/tests/`). No comparte con este proyecto ni el código ni los criterios.

> **El corpus y su generador son de Giuliano Taormina, compañero de la materia**, que los compartió para que cada uno pudiera contrastar su analizador. No son material
> de este proyecto: acá sólo se corre el compilador contra ellos.
> La carpeta **no se versiona** —la atribución está en su `CREDITOS.md`— y el test se saltea si no está.

**Coinciden los 16.512**, y el test exige que no aparezca ninguna divergencia.

**El corpus se podó, y cada poda está justificada.** El original traía 30.000 casos:

- **~1.854 por la barra invertida en un literal.** §2.3.3 da una regla posicional —la comilla de
  cierre nunca está antecedida por `\`— y no una de apareamiento, así que acá el literal no cierra;
  su *fuzzer* asume la semántica de Java. Una sola discrepancia repetida miles de veces.
- **11.577 que usan construcciones que la gramática de este proyecto no tiene**: su gramática las
  genera, así que el veredicto de su generador no aplica acá.
- **57 más, revisados uno por uno**: genéricos al instanciar (`new Clase<Arg>()`, que la gramática
  de la cátedra genera y la suya no), el logro de Visibilidad Mejorada (sus corpus `base` y
  `localvar` se generaron sin él), y un puñado que las dos implementaciones rechazan en distinto
  token porque su gramática cubre menos. **Ninguno escondía un defecto propio**: se verificó caso
  por caso qué construcción lo explicaba.

Los **55 casos escritos a mano** de ese corpus sí se importaron —son curados y legibles, y están en
`resources/` con el prefijo `ext`—; los generados no se copian ni se versionan.

### La verificación diferencial contra la gramática

El analizador sintáctico está escrito a mano contra una gramática verificada, lo que deja una
pregunta abierta: ¿reconoce el mismo lenguaje, o uno parecido? `herramientas/comparar_con_gramatica.py`
la responde corriendo el analizador en Java y el simulador del Esquema Simple sobre los mismos
fuentes, y exigiendo el mismo veredicto y el mismo token de rechazo.

Es la prueba más fuerte de la suite del sintáctico: no depende de que a alguien se le ocurra el caso
correcto, sino de que dos implementaciones independientes coincidan. Cualquier divergencia la hace
fallar.

Los testers de la cátedra están escritos con JUnit 4; corren junto a los propios (JUnit 5) gracias al
*vintage engine*, sin necesidad de tocarlos.

**El *working directory* debe ser `compilador/`**, porque los testers de la cátedra usan rutas
relativas duras: `resources/sinErrores/` y `resources/conErrores/` los del sintáctico, y
`resources/lexico/…` las copias del léxico. Es el valor por defecto cuando IntelliJ importa el
proyecto Maven correctamente.

> **Las clases de propiedades etiquetan con el `@Tag` de jqwik, no con el de JUnit.** Los perfiles
> de fase filtran por etiqueta, y jqwik **ignora** `org.junit.jupiter.api.Tag`: una clase de
> propiedades que lo use queda sin etiquetar para su motor y el perfil la **saltea en silencio**,
> sin fallar y sin contarla. Lo único que lo delata es un aviso por consola. `PropiedadesEtiquetadasTest`
> lo verifica por reflexión sobre `target/test-classes`, así que una clase nueva que se equivoque
> rompe la suite en vez de desaparecer.

## 7. Ver también

- [`../lexico/decisiones-de-logica.md`](../lexico/decisiones-de-logica.md) — cada decisión, con el test que la fija.
- [`manejo-de-errores.md`](manejo-de-errores.md) — el detalle de las invariantes de error.
- [`../lexico/arquitectura-del-analizador-lexico.md`](../lexico/arquitectura-del-analizador-lexico.md) — por qué `Salida` es una interfaz, que es la decisión de
  diseño que hace posible casi toda esta suite.

---

# Reglas operativas para quien implementa

Todo lo anterior explica **qué hace** el compilador y **por qué**. Lo que sigue son las **reglas
de trabajo** correspondientes: lo que hay que respetar al escribir o modificar código de esta
parte, en el mismo lugar que el tema que regulan.

### Fases del compilador (marco para las 5 etapas)

```
Fuente → [Léxico] → [Sintáctico] → [AST] → [Tabla de Símbolos / Resolución de Nombres]
       → [Semántico] → [Chequeo de Tipos] → [Generación de Código] → [Ejecución]
```

Las etapas 1 y 2 cubren **Léxico** y **Sintáctico**; el resto se agrega con cada etapa. Cuando falla un test end-to-end, tiene que ser posible aislar en qué fase está el problema
usando los tests de esa fase específica — de ahí que los tests se ubiquen y nombren por fase.

### Tipos de test — cuándo corresponde cada uno (no se aplican todos a todo)

| Tipo | Cuándo usarlo | En Etapa 1 |
|---|---|---|
| **Unit** | unidades aisladas y deterministas | cada estado del AF, `Token`, tabla de palabras clave, clasificador de caracteres, formateadores de error, manejador de archivo |
| **Data-driven** | muchas entradas evaluadas con la misma lógica | fuente → stream de tokens esperado, un archivo de tabla por familia léxica (identificadores, enteros, chars, strings, operadores, comentarios, floats, unicode) |
| **Integration** | interacción entre componentes/fases | `SourceManager → AnalizadorLexico`, `AnalizadorLexico → ModuloPrincipalLexico` |
| **E2E** | pipeline completo | `Main.main()` sobre los archivos de `resources/`, más los testers de la cátedra sin modificar |
| **Negative** | programas inválidos deben rechazarse correctamente | un caso por cada `TipoDeErrorLexico` |
| **Regression** | todo bug relevante encontrado | append-only, nunca se borra ni se renumera un caso |
| **Golden/snapshot** | resultados estructurados grandes | salida completa del compilador por archivo de `resources/`, comparada entera |
| **Property-based** | invariantes que deben sostenerse sobre muchas entradas | terminación garantizada, líneas nunca decrecen, round-trip lexema+blancos = fuente original |
| **Fuzz** | robustez del léxico ante entradas inesperadas | bytes/UTF-8 aleatorios: cualquier resultado que no sea `ExcepcionLexica` (o éxito) es un fallo |
| **Mutation** | medir qué tan buena es la suite, no correr en cada cambio | PIT en un profile Maven aparte (`-Pmutacion`) |

**Prioridad**: Nivel 1 obligatorio siempre (unit, data-driven, integration, negative, regression);
Nivel 2 obligatorio cuando existe pipeline completo (E2E — existe desde la Etapa 1); Nivel 3
complementario (golden, property-based, fuzz); Nivel 4 evaluación periódica, no continua (mutation).
No convertir esto en regla rígida si la arquitectura real de una etapa futura pide otra distribución.

### Reglas generales

- Todo código nuevo lleva tests apropiados; no se escriben tests sólo para subir el número de
  cobertura.
- Los tests verifican comportamiento, contratos e invariantes — no implementación interna.
- Deterministas, independientes entre sí, sin depender del orden de ejecución, sin `Thread.sleep`.
- Nunca ocultar un error para hacer pasar un test, ni modificar un test para acomodar una
  implementación que está mal — si la especificación cambia legítimamente, se actualiza el test y se
  documenta el motivo (mensaje de commit descriptivo).
- Los tests viven cerca de la fase que validan (mismo paquete espejado bajo `src/test/java`), salvo
  los E2E que viven en su propio módulo de tests de integración completa.
- **Todo bug relevante se convierte en test de regresión permanente**: reproducir → escribir el test
  que reproduce el bug → confirmar que falla → arreglar → confirmar que pasa → el caso queda en la
  suite para siempre.

### Taxonomía de errores (severidad distinta para el test)

`error léxico` / `sintáctico` (etapa 2+) / `semántico` (etapa 3+) / `de tipos` (etapa 3+) /
**`interno del compilador`**. Los primeros cuatro son comportamiento **esperado** ante un programa
inválido — nunca deben tratarse como crash. Un **error interno del compilador es siempre un defecto**
(una excepción no controlada, un `NullPointerException`, cualquier cosa que no sea
`ExcepcionLexica` o `ExcepcionSintactica`) y **debe hacer fallar el test**, sin excepción.

Los errores esperados se validan por **tipo y localización** (línea, columna), nunca por el texto
completo del mensaje — salvo el código de contrato de la cátedra `[Error:Lexema|NroLinea]`, que sí
se compara literal porque es lo que verifica el tester.

### Fuzzing reproducible

Tres corridas con semilla fija, cada una tapando un hueco distinto: alfabeto sesgado a caracteres
"interesantes", **bytes crudos** (la primera nunca puede generar UTF-8 inválido porque codifica un
`String`, así que sin la segunda un archivo binario quedaría sin probar), y construcciones sin
cerrar (donde vive la mayor parte de la lógica de error).

Todo input que produzca un fallo se persiste como caso permanente en
`compilador/resources/lexico/regresion/`, y `CorpusDeRegresionTest` **reprocesa ese directorio
entero** en cada corrida.

### Verificación cruzada contra la suite de un compañero

Un compañero compartió su suite (3 testers JUnit 4 + 24 casos). Sirvió como **oráculo
independiente**, que es el uso que vale la pena darle a material ajeno: los 24 casos se corrieron
contra este compilador con un script descartable —no se copió nada al proyecto para probarlos— y
**pasaron los 24**, incluida la secuencia exacta de tokens y errores intercalados. Ninguna
divergencia, ningún bug encontrado.

Qué es cada cosa:

- Sus `TesterDeCasosSinErrores` y `TesterDeCasosConErrores` son los mismos archivos de la cátedra
  que ya están en `src/test/java/catedra/` (idénticos salvo espacios).
- Su `TesterDeCasosConErroresYTokens` cumple la función de nuestro
  `minijava.casos.TesterDeCasosConErroresEstricto` y usa **el mismo formato de anotación**
  (`// [Error:lex|linea]` + `//#lex,linea`) — el nuestro verifica además la ausencia de
  `[SinErrores]` y que ninguna línea de salida lleve un carácter de control. No se integró.
- Sus casos son casi un subconjunto de `resources/`: sólo cuatro huecos reales, que quedaron
  cubiertos con casos propios escritos en el formato del corpus —
  `sinErrores/puntoSeguidoDeSufijoDeFloat.java` (`1.f`: la forma `Digito+ . Digito* Sufijo` con
  parte fraccionaria vacía), `sinErrores/igualdadCuadrupleYBarraAsteriscoSeparados.java` (`a====b`,
  `a!=!b`, y `/` seguido de espacio y `*`, que **no** abre un comentario),
  `sinErrores/charsConDolarYControlesEscapados.java` (`'$'` — adversarial, porque `$` es el lexema
  del `EOF` — más `'\r'`, `'\f'`, `'\b'`) y `conErrores/pipeTriple.java` (`|||`, la simetría que
  faltaba con `ampersandTriple.java`).

**Criterio ante una suite ajena**: su aporte está en la verificación, no en el volumen
de casos. Correrla antes de copiar nada evita ensuciar el corpus con duplicados, y un caso ajeno
cuya expectativa contradice una decisión ya documentada se informa —verificándolo contra `javac`
como manda `AGENTS.md` §0.1— pero no se adopta.

### El oráculo externo: `javac`

Cuando la cátedra no especifica un comportamiento, `AGENTS.md` §0.1 manda hacer lo que hace Java,
**verificado, no de memoria**. En la práctica eso significa escribir el caso en un `.java` y correr
`javac`. Es el fundamento de dos decisiones de diseño:

- el criterio de recuperación (`javac` no sincroniza hasta el delimitador y nunca descarta un
  carácter ajeno al lexema reportado — ver [`manejo-de-errores.md`](manejo-de-errores.md) §2);
- dónde apunta el `^` en cada diagnóstico.

Y de una tercera: los floats **no** tienen límite de dígitos, ni en la cátedra ni en Java; lo que
Java rechaza es un valor fuera de rango, y eso es semántico, no léxico.

**Regla**: todo test cuyo oráculo sea "así hace Java" lleva en su comentario la salida **real** de
`javac`, copiada de la corrida, para que sea rastreable y no una afirmación de memoria.

**Y el límite del oráculo**: `javac` vale mientras la cátedra calle. Donde el profesor fijó un
criterio, ése manda aunque Java haga otra cosa — pasa en un solo lugar, el escape mal formado
dentro de un `litString`
([`../lexico/decisiones-de-deteccion-de-errores.md`](../lexico/decisiones-de-deteccion-de-errores.md)
§4.3), donde `javac` sigue leyendo el
literal y MiniJava lo descarta hasta la comilla de cierre. Los tests de ese caso citan la indicación
del profesor, no a `javac`.

### Los dos testers de casos de `resources/lexico/conErrores/`

Corren sobre los **mismos archivos** y conviven:

- `catedra.TesterDeCasosConErrores` — el de la cátedra, intacto. Lee **sólo la primera línea** y hace
  `containsString` del código declarado.
- `minijava.casos.TesterDeCasosConErroresEstricto` — propio. Con la marca `// [Completo]` en la
  cabecera exige la secuencia **exacta y ordenada** de errores **y de tokens**, sin nada de más;
  verifica además que no aparezca `[SinErrores]` y que ninguna línea lleve un carácter de control.

Declarar los tokens y no sólo los errores es lo que hace verificable que la recuperación no se coma
nada: con la alternativa de recuperación descartada, `v1 &v2` imprimiría `(intLiteral,2,1)` y un
tester que sólo mira errores no lo notaría.

Los casos nuevos se generan con `compilador/herramientas/lote*.py`, que calculan la línea del `EOF` y
el desplazamiento de la cabecera. **Las expectativas se predicen antes de correr**: si un caso falla,
lo primero es decidir si el equivocado es el oráculo o el compilador. Varios casos del corpus salen
de haber predicho mal y haber ido a mirar por qué.

### El volcado de la salida por caso

Con los runners de corpus mudos, la única forma de ver qué reporta el compilador sobre un caso sería
romperle el assert. Por eso los cuatro vuelcan la salida, con un interruptor:
`minijava.casos.VolcadoDeCasos`, `-Dvolcado=completo|resumen|off` (completo por defecto,
declarado en el `pom.xml` para que llegue al JVM forkeado). Cada caso imprime su encabezado, la
salida entera del compilador y un pie con los conteos; cada suite cierra con
`resources/<dir>: N casos procesados, M con errores`.

Los tests **unitarios** del sintáctico trabajan con la `ExcepcionSintactica` y no con la consola,
así que `BaseDeAnalisis` también vuelca, y lo hace corriendo el
**módulo principal** sobre el mismo archivo en vez de reconstruir el texto: lo que se lee en la
consola es exactamente lo que imprimiría `java -jar Compilador.jar`. Sólo lo corre si el volcado
está prendido, así que con `-Dvolcado=off` no se paga nada.

```
----- class C1 { void m(){ a ? b : c = 1; } }
Error Sintactico en linea 3: se esperaba ; se encontro "="

[Error:=|3]
----- 1 error
```

Desde IntelliJ sale sin configurar nada: el modo por defecto es `completo` y está puesto en el
código (`System.getProperty("volcado", "completo")`), no sólo en el `pom.xml`, así que vale también
cuando el IDE lanza JUnit directo sin pasar por Maven.

**Regla para código nuevo**: un runner de corpus nuevo llama a `VolcadoDeCasos.volcar()` con la
salida que ya tiene en la mano. Los que capturan `System.out` —los de la cátedra— tienen que
pasarle el `PrintStream` original, o el volcado se lo comen ellos mismos.

### Mutation testing (PIT) — referencia y por qué ciertos sobrevivientes no se cazan

La última corrida alcanzó **91 % de mutantes matados** (872/962), *test strength* 92 %, con 97 % de
cobertura de línea sobre las clases mutadas. El umbral versionado —`umbral.mutacion` en el
`pom.xml`— es 88, y `mvn -Pmutacion verify` falla por debajo.

**Patrón recurrente de mutante equivalente en este código base**: varios estados hacen
`contexto.avanzar(); return new Continuar(SiguienteEstado);` justo antes de pasarle el control a un
estado cuya PROPIA lógica vuelve a chequear y consumir ese mismo carácter (por ejemplo,
`EstadoInicial` viendo un dígito pasa a `EstadoEntero`, que en su primer chequeo también acepta
dígitos). Si PIT elimina ese `avanzar()` puntual, el carácter se termina consumiendo de todos modos
—un paso después, por el chequeo duplicado del estado siguiente— y el lexema final resulta idéntico.
Esto hace que el mutante **sobreviva sin que sea un hueco real de testing**: es un mutante
equivalente, invisible a los filtros automáticos de PIT porque cruza el límite entre dos clases
distintas. Confirmado así en: `EstadoInicial` (dígito), `EstadoIdGenOClase`, `EstadoCharEscapado` y
`EstadoStringTrasBarra` (marcador `u`), `EstadoFloatExponenteInicio`,
`EstadoFloatExponenteRequiereDigito`, `EstadoBarra` (ambas ramas de comentario). **No intentar
"matarlos" agregando tests** — no hay ningún test posible que distinga el comportamiento mutado del
original, porque no lo hay.

**Sobrevivientes aceptados, no perseguidos**: `SalidaEstandar` toca `System.out` real —
deliberadamente no se testea de forma directa (para eso existe la interfaz `Salida`, que sí está 100%
testeada vía `SalidaEnMemoria`). `Main.main()` **sí** se testea (`MainTest`), pero sólo en sus
modos de falla —sin argumentos, archivo inexistente, extensión rara— capturando `System.out`: son los
casos en los que una excepción sin atrapar mataría la corrida in-process de los testers de la
cátedra, así que valen el costo. `ModuloPrincipalLexico`'s `manejador.close()` no se
puede espiar sin inyectar `ManejadorDeArchivo` por constructor (se instancia adentro del método)
— cambio de diseño no justificado sólo para este hueco, dado que `close()` únicamente libera memoria
en un proceso de corta vida (no hay handle de archivo del SO abierto de por medio). `SourceManagerImpl`
es código original de la cátedra sin tocar, no se le agregan tests propios.
